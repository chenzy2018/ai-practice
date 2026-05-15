package com.czy.ai.qwen.web.aspect;

import com.czy.ai.qwen.common.annotation.RateLimit;
import com.czy.ai.qwen.common.dto.ChatRequest;
import com.czy.ai.qwen.common.dto.Result;
import com.czy.ai.qwen.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 限流切面
 * 实现规则：同一会话内多次对话不限制，新增会话每分钟最多10个
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    @Resource
    private RateLimitService rateLimitService;

    @Around("@annotation(com.czy.ai.qwen.common.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = getRequest(joinPoint);
        assert request != null;
        String userId = getUserId(request);
        String sessionId = getSessionId(joinPoint);

        if (!rateLimitService.tryAcquireSession(userId, sessionId)) {
            return Result.fail(429, "请求过于频繁，请稍后再试");
        }

        return joinPoint.proceed();
    }

    private HttpServletRequest getRequest(ProceedingJoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof HttpServletRequest) {
                return (HttpServletRequest) arg;
            }
        }
        return null;
    }

    private String getUserId(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isEmpty()) {
            userId = request.getRemoteAddr();
        }
        return userId;
    }

    private String getSessionId(ProceedingJoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof ChatRequest) {
                return ((ChatRequest) arg).getSessionId();
            }
        }
        return null;
    }
}
