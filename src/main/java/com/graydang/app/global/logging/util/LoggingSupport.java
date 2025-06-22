package com.graydang.app.global.logging.util;

import com.graydang.app.global.logging.config.LoggingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;

@Slf4j
@RequiredArgsConstructor
public abstract class LoggingSupport {

    private static final String LOG_DEPTH_KEY = "logDepth";
    private final LoggingProperties loggingProperties;

    protected String getDepthIndent() {
        String depthStr = MDC.get(LOG_DEPTH_KEY);
        int depth = depthStr != null ? Integer.parseInt(depthStr) : 0;
        return "  ".repeat(depth);
    }

    protected void increaseDepth() {
        String depthStr = MDC.get(LOG_DEPTH_KEY);
        int depth = depthStr != null ? Integer.parseInt(depthStr) : 0;
        MDC.put(LOG_DEPTH_KEY, String.valueOf(depth + 1));
    }

    protected void decreaseDepth() {
        String depthStr = MDC.get(LOG_DEPTH_KEY);
        int depth = depthStr != null ? Integer.parseInt(depthStr) : 1;
        MDC.put(LOG_DEPTH_KEY, String.valueOf(Math.max(0, depth - 1)));
    }

    protected void clearDepth() {
        MDC.remove(LOG_DEPTH_KEY);
    }

    private String getUserId() {
        String userId = MDC.get("userId");
        return userId != null ? "[userId=" + userId + "] " : "";
    }

    private String getRequestUUID() {
        String requestUUID = MDC.get("requestUUID");
        return requestUUID != null ? "[requestUUID=" + requestUUID + "] " : "";
    }

    protected void logMethodEntry(JoinPoint joinPoint, String layer) {
        if (!loggingProperties.getEnabled())
            return;

        increaseDepth(); // 깊이 증가
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        String methodName = methodSignature.getDeclaringType().getSimpleName() + "." + methodSignature.getName();
        String indent = getDepthIndent();
        String userId = getUserId();
        String requestUUID = getRequestUUID();
        log.info("{}{}{}[{}] ENTER  => {}({})", requestUUID, userId, indent, layer, methodName, argsToString(joinPoint.getArgs()));
    }

    protected void logMethodExit(JoinPoint joinPoint, Object result, String layer) {
        if (!loggingProperties.getEnabled())
            return;

        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        String methodName = methodSignature.getDeclaringType().getSimpleName() + "." + methodSignature.getName();
        String indent = getDepthIndent();
        String userId = getUserId();
        String requestUUID = getRequestUUID();
        log.info("{}{}{}[{}] RETURN <= {} => {}", requestUUID, userId, indent, layer, methodName, result);
        decreaseDepth();
    }

    protected void logException(JoinPoint joinPoint, Throwable ex, String layer) {
        if (!loggingProperties.getEnabled())
            return;

        String indent = getDepthIndent();
        String methodName = joinPoint.getSignature().toShortString();
        String userId = getUserId();
        String requestUUID = getRequestUUID();
        log.error("{}{}{}[{}] EXCEPTION <= {} => {}", requestUUID, userId, indent, layer, methodName, ex.toString());
        decreaseDepth();
    }

    private String argsToString(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Object arg : args) {
            builder.append(arg).append(", ");
        }
        return builder.substring(0, builder.length() - 2);
    }
}
