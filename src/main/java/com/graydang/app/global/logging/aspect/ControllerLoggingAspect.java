package com.graydang.app.global.logging.aspect;

import com.graydang.app.global.logging.config.LoggingProperties;
import com.graydang.app.global.logging.util.LoggingSupport;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ControllerLoggingAspect extends LoggingSupport {

    public ControllerLoggingAspect(LoggingProperties loggingProperties) {
        super(loggingProperties);
    }

    @Pointcut("execution(* com.graydang..controller..*(..))")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            logMethodEntry(joinPoint, "CONTROLLER"); // 진입 로그 + depth 증가
            Object result = joinPoint.proceed();     // 실제 메서드 실행
            logMethodExit(joinPoint, result, "CONTROLLER"); // 반환 로그 + depth 감소
            return result;
        } finally {
            clearDepth(); // depth 초기화 (스레드 누수 방지)
        }
    }

    @AfterThrowing(pointcut = "controllerMethods()", throwing = "ex")
    public void logException(JoinPoint joinPoint, Throwable ex) {
        logException(joinPoint, ex, "CONTROLLER");
    }
}
