package com.bi_side.CodeSnap.config.security;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author 송민규
 * @deprecated 테스트용 클래스로 추후 삭제 예정
 * 테스트용 UserDetailsService<br/>
 * 실제 개발 할 때는 loadUserByUsername에 사용자 정보를 DB에 가져오는 작업 필요
 */
//@Service
@Deprecated(since = "25-06-01", forRemoval = true) //테스트용 UserDetailsService
public class FakeUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 비밀번호는 아무거나 OK → 일단 암호화된 값 고정
        String fakeEncodedPdw = "$2a$10$A9bcqLApSphW5M8I8UMkDuyjGvNvYe0Woz2nkZ5JasUcF16tL0c/G"; // "1234"

        return new User(username, fakeEncodedPdw,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

}

