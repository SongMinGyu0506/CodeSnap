package com.bi_side.CodeSnap.config.security;

import com.bi_side.CodeSnap.config.security.mapper.SecurityMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.*;
import java.util.function.Supplier;

@Slf4j
public class DynamicAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final SecurityMapper securityMapper;
    private Map<RequestMatcher, Collection<String>> requestMap = new LinkedHashMap<>();
    private Date lastUpdated = null;
    private Date lastChecked = null;
    private static final int RELOAD_CHECK_SECONDS_THRESHOLD = 10;

    public DynamicAuthorizationManager(SecurityMapper securityMapper) {
        this.securityMapper = securityMapper;
        load();
    }

    /**
     * 권한 판단을 위한 인증 객체와 요청 정보를 바탕으로 권한 부여 여부를 판단한다.
     * @param authentication 인증 정보 Supplier
     * @param context RequestAuthorizationContext (요청 + 메타 정보 포함)
     * @return 권한 부여 여부를 나타내는 AuthorizationDecision 객체
     */
    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        loadWithCheck(RELOAD_CHECK_SECONDS_THRESHOLD);

        HttpServletRequest request = context.getRequest();

        Collection<String> requiredRoles = requestMap.entrySet().stream()
                .filter(entry -> entry.getKey().matches(request))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);

        Authentication auth = authentication.get();
        boolean granted = requiredRoles != null && auth != null && auth.isAuthenticated() && auth.getAuthorities().stream()
                .anyMatch(a -> requiredRoles.contains(a.getAuthority()));

        return new AuthorizationDecision(granted);
    }

    /**
     * 권한이 부여되지 않은 경우 예외를 던지는 인증 검증 메서드
     * @param authentication 인증 정보 Supplier
     * @param context 요청 컨텍스트
     * @throws AccessDeniedException 권한이 없을 경우 발생
     */
    @Override
    public void verify(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        AuthorizationDecision decision = check(authentication, context);
        if (!decision.isGranted()) {
            throw new AccessDeniedException("Access Denied: " + context.getRequest().getRequestURI());
        }
    }

    /**
     * DB에서 권한 설정 정보를 불러와 requestMap을 초기화한다.
     * DB 권한 정보 외에도 예외 URL들을 먼저 등록하며, 기본 차단 정책도 포함된다.
     * @see #getPrefixRolesAndUrl(Map)
     * @see #addPathWithVariants(Map, String, String)
     */
    private synchronized void load() {
        LinkedHashMap<RequestMatcher, Collection<String>> loadedMap = new LinkedHashMap<>();
        getPrefixRolesAndUrl(loadedMap);

        List<Map<String, String>> rawResourcesMap = securityMapper.loadRoles();
        for (Map<String, String> rawResource : rawResourcesMap) {
            String res = Optional.ofNullable(rawResource.get("RES")).map(s -> s.endsWith("/") ? s.substring(0, s.length() - 1) : s).orElse(null);
            String auth = rawResource.get("AUTH");
            boolean readable = "Y".equals(rawResource.get("READABLE"));
            if (res == null || auth == null || !readable) continue;
            addPathWithVariants(loadedMap, res, auth);
        }

        loadedMap.putIfAbsent(new AntPathRequestMatcher("/**"), List.of("ROLE_BLCK"));
        this.requestMap = loadedMap;
        this.lastUpdated = new Date();
        this.lastChecked = new Date();
    }

    /**
     * 예외 URL 패턴들을 미리 등록하여 인증 없이 접근 가능하도록 설정한다.
     * 현재는 개발 중이므로 전체 경로("/**")에 대해 ROLE_NONE으로 허용한다.
     * 운영 시에는 필요한 URL만 등록하도록 수정 필요.
     * @param resourcesMap 권한 매핑 맵
     * @see #addSinglePath(Map, String, String)
     */
    private void getPrefixRolesAndUrl(Map<RequestMatcher, Collection<String>> resourcesMap) {
        addSinglePath(resourcesMap, "/**", "ROLE_NONE");
    }

    /**
     * 주어진 URL 경로에 대해 일반 경로 및 /api 경로 2가지를 matcher로 추가한다.
     * @param map 권한 맵
     * @param basePath URL 경로
     * @param role 권한 문자열
     * @see #addSinglePath(Map, String, String)
     */
    private void addPathWithVariants(Map<RequestMatcher, Collection<String>> map, String basePath, String role) {
        List<String> paths = List.of(basePath + "/**", "/api" + basePath + "/**");
        for (String path : paths) {
            RequestMatcher matcher = new AntPathRequestMatcher(path);
            map.computeIfAbsent(matcher, k -> new LinkedList<>()).add(role);
        }
    }

    /**
     * 단일 URL 패턴과 권한을 매핑한다.
     * @param map 권한 맵
     * @param path 단일 URL 경로
     * @param role 권한 문자열
     */
    private void addSinglePath(Map<RequestMatcher, Collection<String>> map, String path, String role) {
        RequestMatcher matcher = new AntPathRequestMatcher(path);
        map.computeIfAbsent(matcher, k -> new LinkedList<>()).add(role);
    }

    /**
     * 일정 주기마다 DB 변경 여부를 확인하고 필요한 경우 load()를 호출해 권한 정보를 재로딩한다.
     * @param secondsThreshold 재확인까지의 주기 (초)
     * @see #getTargetTime(int)
     * @see #load()
     */
    private void loadWithCheck(int secondsThreshold) {
        Date targetTime = getTargetTime(secondsThreshold);
        synchronized (this) {
            boolean shouldCheck = targetTime == null || lastChecked == null || lastChecked.before(targetTime);
            if (!shouldCheck) return;

            Date dbLastUpdated = securityMapper.getRolesLastUpdate();
            if (dbLastUpdated != null && (lastUpdated == null || lastUpdated.before(dbLastUpdated))) {
                load();
            } else {
                lastChecked = new Date();
            }
        }
    }

    /**
     * 현재 시간 기준으로 지정된 초 수 이전의 시간을 반환한다.
     * @param secondsThreshold 과거 시점 기준 (초)
     * @return 기준 시간 객체 (Date)
     */
    private Date getTargetTime(int secondsThreshold) {
        if (secondsThreshold > 0) {
            Calendar time = Calendar.getInstance();
            time.add(Calendar.SECOND, -1 * secondsThreshold);
            return time.getTime();
        }
        return null;
    }
}
