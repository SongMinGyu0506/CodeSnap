package com.bi_side.CodeSnap.config.security;

import com.bi_side.CodeSnap.config.security.mapper.SecurityMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component("reloadableFilterInvocationSecurityMetadataSource")
@Deprecated //Spring Security 6.0 버전 미사용 DynamicAuthorizationManager로 이전
public class ReloadableFilterInvocationSecurityMetadataSourceImpl implements FilterInvocationSecurityMetadataSource {

    private Map<RequestMatcher,Collection<ConfigAttribute>> requestMap = null;
    @Getter
    private Date lastUpdated = null;
    private Date lastChecked = null;
    private static final int RELOAD_CHECK_SECONDS_THRESHOLD = 10;

    //private String sysprofile;

    @Autowired
    private SecurityMapper securityMapper;

    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        this.loadWithCheck(RELOAD_CHECK_SECONDS_THRESHOLD);// 데이터 갱신 필요 확인

        HttpServletRequest request = ((FilterInvocation) object).getRequest();
        Collection<ConfigAttribute> result = null;
        for(Map.Entry<RequestMatcher, Collection<ConfigAttribute>> entry : requestMap.entrySet()) {
            if(entry.getKey().matches(request)) {
                result = entry.getValue();
                break;
            }
        }
        return result;
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        this.loadWithCheck(RELOAD_CHECK_SECONDS_THRESHOLD);

        Set<ConfigAttribute> allAttributes = new HashSet<>();
        for(Map.Entry<RequestMatcher, Collection<ConfigAttribute>> entry : requestMap.entrySet()) {
            allAttributes.addAll(entry.getValue());
        }
        return allAttributes;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }

    public void loadWithCheck() {
        this.loadWithCheck(0);
    }

    public void loadWithCheck(int secondsThreshold) {
        this.synchronizedDatabaseUpdateTimeCheck(getTargetTime(secondsThreshold));
    }

    public synchronized void load() {
        LinkedHashMap<RequestMatcher, List<ConfigAttribute>> loadedMap = this.getRolesAndUrl();
        this.requestMap = new LinkedHashMap<>(loadedMap);
        this.lastUpdated = Calendar.getInstance().getTime();
        this.lastChecked = Calendar.getInstance().getTime();
    }


    /**
     * URL,권한 획득
     * @return resourceMap
     * 연관 메소드 this.load()
     */
    private LinkedHashMap<RequestMatcher, List<ConfigAttribute>> getRolesAndUrl() {
        LinkedHashMap<RequestMatcher, List<ConfigAttribute>> resourcesMap = getPrefixRolesAndUrl();
        this.DatabaseProcessingResourcesMap(resourcesMap);

        resourcesMap.put(new AntPathRequestMatcher("/**"), List.of(new SecurityConfig("ROLE_BLCK")));
        return resourcesMap;
    }

    /**
     * DB조회하여 권한 확인 메소드
     * @param resourcesMap
     * 연관 메소드: this.getRolesAndUrl()
     */
    private void DatabaseProcessingResourcesMap(LinkedHashMap<RequestMatcher, List<ConfigAttribute>> resourcesMap) {
        List<Map<String,String>> rawResourcesMap = securityMapper.loadRoles(); //TODO: 송민규 테스트 필요
        for(Map<String,String> rawResource : rawResourcesMap) {
            String resourceStr = Optional.ofNullable(rawResource.get("RES")).map(s -> s.endsWith("/") ? s.substring(0, s.length() - 1) : s).orElse(null);
            Boolean resourceReadable = "Y".equals(rawResource.get("READABLE"));
            Boolean resourceWritable = "Y".equals(rawResource.get("WRITABLE"));
            String resourceMethod = null;
            if(!resourceReadable) continue;

            getAuthoritiesResourceMap(resourceStr,resourceMethod,null,resourcesMap,rawResource);
            getAuthoritiesResourceMap(resourceStr,resourceMethod,"api",resourcesMap,rawResource);

        }
    }

    /**
     * 하위 URL Path 허용처리 메소드
     * @param resourceStr url
     * @param resourceMethod urlMethod
     * @param type null or 'api'
     * @param resourcesMap url+Method List
     * @param rawResource
     * 연관메소드 this.DatabaseProcessingResourcesMap(LinkedHashMap<RequestMatcher, List<ConfigAttribute>> resourcesMap)
     */
    private void getAuthoritiesResourceMap(String resourceStr, String resourceMethod, String type, LinkedHashMap<RequestMatcher, List<ConfigAttribute>> resourcesMap, Map<String, String> rawResource) {
        String url = type.equals("api") ? "/api"+resourceStr+"/**" : resourceStr+"/**";
        RequestMatcher resource = new AntPathRequestMatcher(url,resourceMethod);
        resourcesMap.computeIfAbsent(resource,key -> new LinkedList<>()).add(new SecurityConfig(rawResource.get("AUTH")));
    }

    /**
     * URL 권한 예외처리 함수
     * @return Spring Security 형식에 맞는 URL,ROLE
     * 연관 메소드 this.getRolesAndUrl()
     */
    private LinkedHashMap<RequestMatcher, List<ConfigAttribute>> getPrefixRolesAndUrl() {
        LinkedHashMap<RequestMatcher,List<ConfigAttribute>> resourcesMap = new LinkedHashMap<>();
                                            //권한 prefix 추가 시 asAntPathRequestMatcher(url,role,resourcesMap) 형식 유지할 것
                                            /*****************************************************
                                             *       ROLE_NONE   :   항상 접근 가능                  *
                                             *       ROLE_LGIN   :   로그인 사용자는 접근 가능        *
                                             *       ROLE_BLCK   :   접근 불가                       *
                                             *       이외 권한은 roles 테이블 참고                    *
                                             ****************************************************/

        asAntPathRequestMatcher("/**","ROLE_NONE",resourcesMap);
        
        return resourcesMap;
    }

    private void asAntPathRequestMatcher(String pattern, String role, LinkedHashMap<RequestMatcher, List<ConfigAttribute>> resourcesMap) {
        resourcesMap.put(new AntPathRequestMatcher(pattern),Arrays.asList(new SecurityConfig(role)));
    }

    /*
    * DB 메뉴 업데이트 시간 확인 함수
    * @return 현재 시간(초)에서 -10초
    * 연관 메소드: this.loadWithCheck(int secondsThreshold)
    * */
    private Date getTargetTime(int secondsThreshold) {
        if(secondsThreshold > 0) {
            Calendar time = Calendar.getInstance();
            time.add(Calendar.SECOND,-1*RELOAD_CHECK_SECONDS_THRESHOLD);
            return time.getTime();
        }
        return null;
    }

    /**
     * (동기화 모드) DB에서 메뉴 업데이트 시간 조회 후 권한 매핑 호출
     * @param targetTime 업데이트 기준 시간
     * 연관 메소드: this.loadWithCheck(int secondsThreshold)
     */
    private void synchronizedDatabaseUpdateTimeCheck(Date targetTime) {
        synchronized (this.lastChecked) {
            boolean shoudCheck = (targetTime == null || lastChecked == null || lastChecked.before(targetTime));
            if(!shoudCheck) return;

            Date dbLastUpdated = securityMapper.getRolesLastUpdate(); //[TODO] 송민규 : 테스트 필요
            if(dbLastUpdated != null && (lastUpdated == null || lastUpdated.before(dbLastUpdated))) {
                this.load();
            } else {
                this.lastChecked = Calendar.getInstance().getTime();
            }
        }
    }
}
