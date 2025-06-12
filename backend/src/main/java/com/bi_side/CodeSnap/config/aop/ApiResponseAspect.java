package com.bi_side.CodeSnap.config.aop;

import com.bi_side.CodeSnap.config.aop.impl.AopMapper;
import com.bi_side.CodeSnap.config.security.CustomUserDetails;
import com.bi_side.CodeSnap.config.util.ApiResponse;
import com.bi_side.CodeSnap.config.util.ErrorCode;
import com.bi_side.CodeSnap.config.util.RequestLogUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Aspect
@Component
public class ApiResponseAspect {

    @Autowired
    private AopMapper aopMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("execution(* com.bi_side.CodeSnap..web..*(..))")
    public Object wrapResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        HttpServletRequest request = getCurrentHttpServletRequest();
        String requestJson = RequestLogUtil.buildRequestJson(joinPoint.getArgs());

        ApiResponse<Object> apiResponse = null;
        int httpStatus = 200;

        if(method.isAnnotationPresent(SkipApiResponse.class) || method.getDeclaringClass().isAnnotationPresent(SkipApiResponse.class)) {
            Object proceed = joinPoint.proceed();
            apiResponse = ApiResponse.OK(null);
            return proceed;
        }

        try {
            Object result = joinPoint.proceed();
            if(result instanceof ApiResponse) {
                apiResponse = (ApiResponse<Object>) result;
            } else {
                apiResponse = ApiResponse.OK(result);
            }
        } catch (DataAccessException dae) {
            apiResponse = ApiResponse.fail(null, ErrorCode.DATA_ACCESS_ERROR);
            httpStatus = 500;
        } catch (Exception e) {
            apiResponse = ApiResponse.fail(null, ErrorCode.FAIL);
            httpStatus = 500;
        }
        saveLogApiAccess(request, apiResponse, auth, httpStatus, requestJson);
        return apiResponse;
    }
    // HttpServletRequest 가져오는 유틸
    private HttpServletRequest getCurrentHttpServletRequest() {
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return (sra != null) ? sra.getRequest() : null;
    }
    private void saveLogApiAccess(HttpServletRequest request, ApiResponse<Object> apiResponse, Authentication auth, int httpStatus, String requestJson) throws JsonProcessingException {
        String url = request.getRequestURI();
        String email = Optional.ofNullable(auth)
                .map(Authentication::getPrincipal)
                .filter(CustomUserDetails.class::isInstance)
                .map(CustomUserDetails.class::cast)
                .map(CustomUserDetails::getUsername)
                .orElse("미로그인 사용자");
        String responseJson = objectMapper.writeValueAsString(apiResponse);

        Map<String, Object> dto = new HashMap<>();
        dto.put("url",url);
        dto.put("email",email);
        dto.put("httpStatus",httpStatus);
        dto.put("requestJson",requestJson);
        dto.put("responseJson",responseJson);
        dto.put("clientIp",request.getRemoteAddr());
        dto.put("httpMethod",request.getMethod());

        aopMapper.insertApiLog(dto);
    }
}
