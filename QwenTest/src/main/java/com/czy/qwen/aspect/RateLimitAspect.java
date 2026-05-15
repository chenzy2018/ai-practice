package com.czy.qwen.aspect;

import com.czy.qwen.resp.Result;
import com.czy.qwen.server.RateLimitService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 限流切面
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    @Resource
    private RateLimitService rateLimitService;

    @Around("@annotation(com.czy.qwen.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = getRequest(joinPoint);
        assert request != null;
        String userId = getUserId(request);

        if (!rateLimitService.tryAcquireSession(userId)) {
            return Result.fail("请求过于频繁，请稍后再试");
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
}