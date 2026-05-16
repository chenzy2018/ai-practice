package com.czy.ai.qwen.web.aspect;

import com.czy.ai.qwen.common.dto.ChatRequest;
import com.czy.ai.qwen.service.RateLimitService;
import com.czy.ai.web.aspect.BaseRateLimitAspect;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class QwenRateLimitAspect extends BaseRateLimitAspect {

    @Resource
    private RateLimitService rateLimitService;

    @Override
    protected boolean tryAcquireSession(String userId, String sessionId) {
        return rateLimitService.tryAcquireSession(userId, sessionId);
    }

    @Override
    protected String getSessionId(ProceedingJoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof ChatRequest) {
                return ((ChatRequest) arg).getSessionId();
            }
        }
        return null;
    }
}
