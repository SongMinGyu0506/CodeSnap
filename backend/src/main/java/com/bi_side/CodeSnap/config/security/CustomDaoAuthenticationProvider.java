package com.bi_side.CodeSnap.config.security;

import com.bi_side.CodeSnap.config.security.mapper.SecurityMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;

/**
 * @author 송민규
 * 로그인 처리 프로세스
 */
@RequiredArgsConstructor
public class CustomDaoAuthenticationProvider extends DaoAuthenticationProvider {

    private SecurityMapper securityMapper;

    public CustomDaoAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder, SecurityMapper securityMapper) {
        setUserDetailsService(userDetailsService);
        setPasswordEncoder(passwordEncoder);
        this.securityMapper = securityMapper;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        //가장 기본적인 로그인 방법(ID,Password 매칭 확인)
        String username = userDetails.getUsername();
        String rawPassword = authentication.getCredentials().toString();
        String encodedPassword = new BCryptPasswordEncoder().encode(rawPassword);
        if(!getPasswordEncoder().matches(rawPassword, encodedPassword)) {
            throw new BadCredentialsException("비밀번호가 틀렸습니다.");
        }

        //추가 로직 작성 가능
        saveLoginLog(username, getCurrentHttpRequest());
    }

    private HttpServletRequest getCurrentHttpRequest() {
        RequestAttributes requestAttribute = RequestContextHolder.getRequestAttributes();
        if(requestAttribute instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttribute).getRequest();
        }
        throw new IllegalStateException("현재 요청이 존재하지 않음");
    }

    private void saveLoginLog(String username, HttpServletRequest request) {
        HashMap<String, String> dto = new HashMap<>();
        dto.put("email",username);
        dto.put("clientIp",request.getRemoteAddr());
        dto.put("userAgent",request.getHeader("User-Agent"));

        int result = securityMapper.saveLog(dto);
        if(result <= 0) {
            throw new IllegalStateException("로그 저장 중 에러");
        }
    }
}

