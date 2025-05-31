package com.bi_side.CodeSnap.config.aop;

import com.bi_side.CodeSnap.config.util.ApiResponse;
import com.bi_side.CodeSnap.config.util.ErrorCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class ApiResponseAspect {

    @Around("execution(* com.bi_side.CodeSnap..web..*(..))")
    public Object wrapResponse(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        if(method.isAnnotationPresent(SkipApiResponse.class) || method.getDeclaringClass().isAnnotationPresent(SkipApiResponse.class)) {
            return joinPoint.proceed();
        }

        try {
            Object result = joinPoint.proceed();

            if (result instanceof ApiResponse) {
                return result;
            }

            return ApiResponse.OK(result);
        } catch (DataAccessException dae) {
            return ApiResponse.fail(null, ErrorCode.DATA_ACCESS_ERROR);
        } catch (Exception e) {
            return ApiResponse.fail(null,ErrorCode.FAIL);
        }
    }
}
