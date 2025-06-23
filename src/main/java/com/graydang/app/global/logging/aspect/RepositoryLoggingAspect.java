package com.graydang.app.global.logging.aspect;

import com.graydang.app.global.logging.config.LoggingProperties;
import com.graydang.app.global.logging.util.LoggingSupport;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RepositoryLoggingAspect extends LoggingSupport {


    public RepositoryLoggingAspect(LoggingProperties loggingProperties) {
        super(loggingProperties);
    }

    @Pointcut("execution(* com.graydang..repository..*(..))")
    public void repositoryMethods() {}

    @Before("repositoryMethods()")
    public void logBefore(JoinPoint joinPoint) {
        logMethodEntry(joinPoint, "REPOSITORY");
    }

    @AfterReturning(pointcut = "repositoryMethods()", returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result) {
        logMethodExit(joinPoint, result, "REPOSITORY");
    }

    @AfterThrowing(pointcut = "repositoryMethods()", throwing = "ex")
    public void logException(JoinPoint joinPoint, Throwable ex) {
        logException(joinPoint, ex, "REPOSITORY");
    }
}
