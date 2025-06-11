package com.bi_side.CodeSnap.config.security.jwt;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT token 인증 필터
 * @author USER
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter{

    private final JwtProvider jwtProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/h2-console");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String jwtToken = jwtProvider.resolveToken(request); //Http Authentication 양식 'Bearer [토큰]'에서 Bearer 제거

        //검증 성공
        if(jwtToken != null && jwtProvider.validateToken(jwtToken)) {
            Authentication auth = jwtProvider.getAuthentication(jwtToken);//Spring Security 'UsernamePassword' 인증토큰으로 변환
            SecurityContextHolder.getContext().setAuthentication(auth); //SecurityContext에 해당 인증토큰 저장
        } else {
            //검증 실패
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            HashMap<String,Object> returnObj = new HashMap<>();
            returnObj.put("httpStatus",HttpServletResponse.SC_UNAUTHORIZED);
            returnObj.put("errorMsg","Invalid or Expired JWT token");
            returnObj.put("responseTime",new Timestamp(System.currentTimeMillis()));
            returnObj.put("data",null);

            ObjectMapper objectMapper = new ObjectMapper();
            response.getWriter().write(objectMapper.writeValueAsString(returnObj));

            return ;
        }
        filterChain.doFilter(request, response);
    }

}

