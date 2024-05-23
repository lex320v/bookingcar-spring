package com.bookingcar.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LogAspect {

    @Pointcut("within(com.bookingcar.service.*Service)")
    public void isServiceLayer() {
    }

    @Before("isServiceLayer()")
    public void addLogging(JoinPoint joinPoint) {
        var className = joinPoint.getTarget().getClass().getName();
        var methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info("BEFORE: Class name: {}, Method name: {}, Passed parameters: {}",
                className, methodName, args);
    }

    @AfterReturning(value = "isServiceLayer()", returning = "result")
    public void addLoggingAfterReturning(JoinPoint joinPoint, Object result) {
        var className = joinPoint.getTarget().getClass().getName();
        var methodName = joinPoint.getSignature().getName();

        log.info("AFTER_RETURNING: Class name: {}, Method name: {}, Returning value: {}",
                className, methodName, result);
    }

    @AfterThrowing(value = "isServiceLayer()", throwing = "exception")
    public void addLoggingAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        var className = joinPoint.getTarget().getClass().getName();
        var methodName = joinPoint.getSignature().getName();

        log.info("AFTER_THROWING: Class name: {}, Method name: {}, Exception {}: {}",
                className, methodName, exception.getClass(), exception.getMessage());
    }
}
