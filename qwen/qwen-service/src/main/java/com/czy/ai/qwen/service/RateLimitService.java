package com.czy.ai.qwen.service;

import com.google.common.util.concurrent.RateLimiter;
import com.czy.ai.qwen.common.config.QwenConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流服务
 * 基于 Guava RateLimiter 实现
 * 限流规则：同一会话内多次对话不限制，新增会话每分钟最多10个
 */
@Slf4j
@Service
public class RateLimitService {

    private final Map<String, RateLimiter> userRateLimiters = new ConcurrentHashMap<>();

    @Resource
    private QwenConfig qwenConfig;

    /**
     * 检查用户是否允许创建新会话
     * 同一会话内多次对话不限制，新增会话每分钟最多创建 MAX_SESSIONS_PER_MINUTE 个
     *
     * @param userId 用户ID
     * @param sessionId 会话ID（为空时表示新增会话）
     * @return true-允许, false-限流
     */
    public boolean tryAcquireSession(String userId, String sessionId) {
        if (sessionId != null && !sessionId.isEmpty()) {
            return true;
        }

        int maxSessionsPerMinute = qwenConfig.getSession().getMaxSessionsPerMinute();
        RateLimiter limiter = userRateLimiters.computeIfAbsent(userId,
                k -> RateLimiter.create(maxSessionsPerMinute / 60.0));

        boolean acquired = limiter.tryAcquire();
        if (!acquired) {
            log.warn("用户限流 - userId: {}, 1分钟内创建会话数已达上限", userId);
        }
        return acquired;
    }

    /**
     * 检查用户是否允许创建新会话（兼容旧接口）
     * 每分钟最多创建 MAX_SESSIONS_PER_MINUTE 个会话
     *
     * @param userId 用户ID
     * @return true-允许, false-限流
     */
    public boolean tryAcquireSession(String userId) {
        return tryAcquireSession(userId, null);
    }

    /**
     * 清理用户限流记录
     *
     * @param userId 用户ID
     */
    public void removeUserLimiter(String userId) {
        userRateLimiters.remove(userId);
    }
}
