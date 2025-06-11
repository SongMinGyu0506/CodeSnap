package com.bi_side.CodeSnap.config.security;

import com.bi_side.CodeSnap.config.security.mapper.SecurityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SecurityMapper securityMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CustomUserDetails userInfo = securityMapper.getUserInfo(username);
        if(userInfo == null) {
            log.error("사용자 쿼리 조회되는 데이터가 없습니다. EMAIL : {}", username);
            throw new UsernameNotFoundException("Username "+username+" not found");
        }

        Set<GrantedAuthority> authoritySet = new HashSet<>();
        authoritySet.add(new SimpleGrantedAuthority("ROLE_NONE"));
        authoritySet.add(new SimpleGrantedAuthority("ROLE_LGIN"));
        if(userInfo.getRole() != null) {
            authoritySet.add(new SimpleGrantedAuthority(userInfo.getRole()));
        }
        authoritySet.add(new SimpleGrantedAuthority("ROLE_USER"));

        userInfo.setAuthorities(authoritySet);
        return userInfo;
    }
}
