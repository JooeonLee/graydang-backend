package com.graydang.app.global.logging.aspect;

import com.graydang.app.global.logging.config.LoggingProperties;
import com.graydang.app.global.logging.util.LoggingSupport;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceLoggingAspect extends LoggingSupport {

    public ServiceLoggingAspect(LoggingProperties loggingProperties) {
        super(loggingProperties);
    }

    @Pointcut("execution(* com.graydang..service..*(..))")
    public void serviceMethods() {}

    @Before("serviceMethods()")
    public void logBefore(JoinPoint joinPoint) {
        logMethodEntry(joinPoint, "SERVICE");
    }

    @AfterReturning(pointcut = "serviceMethods()", returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result) {
        logMethodExit(joinPoint, result, "SERVICE");
    }

    @AfterThrowing(pointcut = "serviceMethods()", throwing = "ex")
    public void logException(JoinPoint joinPoint, Throwable ex) {
        logException(joinPoint, ex, "SERVICE");
    }
}
