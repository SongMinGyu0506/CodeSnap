package com.bi_side.CodeSnap.config.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;

/**
 * @author 송민규
 * 로그인 처리 프로세스
 */
@RequiredArgsConstructor
public class CustomDaoAuthenticationProvider extends DaoAuthenticationProvider {
    public CustomDaoAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        setUserDetailsService(userDetailsService);
        setPasswordEncoder(passwordEncoder);
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
        saveLoginLog(username);
    }

    private void saveLoginLog(String username) {

    }
}

