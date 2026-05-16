package com.czy.ai.web.aspect;

import com.czy.ai.common.annotation.RateLimit;
import com.czy.ai.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 限流切面接口 - 定义获取 userId 和 sessionId 的方法
 * 具体实现由各AI模块提供
 */
@Slf4j
@Aspect
@Component
public abstract class BaseRateLimitAspect {

    @Around("@annotation(com.czy.ai.common.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = getRequest(joinPoint);
        assert request != null;
        String userId = getUserId(request);
        String sessionId = getSessionId(joinPoint);

        if (!tryAcquireSession(userId, sessionId)) {
            throw new BusinessException(429, "请求过于频繁，请稍后再试");
        }

        return joinPoint.proceed();
    }

    protected abstract boolean tryAcquireSession(String userId, String sessionId);

    protected abstract String getSessionId(ProceedingJoinPoint joinPoint);

    protected HttpServletRequest getRequest(ProceedingJoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof HttpServletRequest) {
                return (HttpServletRequest) arg;
            }
        }
        return null;
    }

    protected String getUserId(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isEmpty()) {
            userId = request.getRemoteAddr();
        }
        return userId;
    }
}
