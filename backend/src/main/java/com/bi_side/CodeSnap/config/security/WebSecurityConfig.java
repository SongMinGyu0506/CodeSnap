package com.bi_side.CodeSnap.config.security;


import com.bi_side.CodeSnap.config.security.jwt.JwtAuthenticationFilter;
import com.bi_side.CodeSnap.config.security.jwt.JwtAuthorizationFilter;
import com.bi_side.CodeSnap.config.security.jwt.JwtProvider;
import com.bi_side.CodeSnap.config.security.mapper.SecurityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final UserDetailsService userDetailService;
    private final JwtProvider jwtProvider;
    //private final DynamicAuthorizationManager dynamicAuthorizationManager;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        CustomDaoAuthenticationProvider provider = new CustomDaoAuthenticationProvider(userDetailService,passwordEncoder());
        return new ProviderManager(provider);
    }
    @Bean
    public DynamicAuthorizationManager dynamicAuthorizationManager(SecurityMapper securityMapper) {
        return new DynamicAuthorizationManager(securityMapper);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            DynamicAuthorizationManager dynamicAuthorizationManager) throws Exception {
        //HTTP Basic authentication 비활성화
        http.httpBasic(AbstractHttpConfigurer::disable);

        //CSRF 비활성화
        http.csrf(AbstractHttpConfigurer::disable);

        //Security Headers :: iFrame 관련 차단 비활성화 (h2-console)
        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

        //JWT 토큰 사용으로 세션 관리방식 STATELESS
        http.sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        //미로그인 사용자 권한 'ROLE_NONE' 매핑
        http.anonymous(auth -> auth.authorities("ROLE_NONE"));

        //DB 동적 권한 체크
        http.authorizeHttpRequests(auth -> auth
                .anyRequest().access(dynamicAuthorizationManager)
        );

        // JWT 필터는 현재 주석 처리(백엔드 개발 완료시 활성화)
        //http.addFilterBefore(new JwtAuthorizationFilter(authenticationManager(http), jwtProvider), UsernamePasswordAuthenticationFilter.class);
        //http.addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
