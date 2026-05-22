package com.czy.ai.langchain4j.aspect;

import com.czy.ai.langchain4j.chatrequest.ChatRequest;
import com.czy.ai.web.RateLimitService;
import com.czy.ai.web.aspect.BaseRateLimitAspect;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

/**
 * @author chenzhenyu 2026年05月21日 上午09:28:37
 */
@Component
public class LangChainRateLimitAspect  extends BaseRateLimitAspect {

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

