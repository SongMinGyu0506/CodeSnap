package com.bi_side.CodeSnap.config.log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Aspect
@Component
public class StructuredLoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(StructuredLoggingAspect.class);

    @Around("execution(* com.bi_side.CodeSnap.api..*(..))")
    public Object logWithDepth(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String signature = className + "." + methodName;

        CallDepth.increase();
        log.info("{}[{}]", CallDepth.enterPrefix(), signature);

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("{}[{}] : {}ms", CallDepth.returnPrefix(), signature, elapsed);
            return result;
        } catch (Exception e) {
            log.info("{}[{}] ({})", CallDepth.exceptionPrefix(), signature, e.getClass().getSimpleName());
            log.error(e.getMessage());
            throw e;
        } finally {
            CallDepth.decrease();
            if (CallDepth.get() == 0) CallDepth.clear();
        }
    }
}
