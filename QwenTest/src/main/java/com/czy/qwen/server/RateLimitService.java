package com.czy.qwen.server;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 限流服务
 * 基于 Guava RateLimiter 实现
 */
@Slf4j
@Service
public class RateLimitService {

    private static final int MAX_SESSIONS_PER_MINUTE = 10;

    private final Map<String, RateLimiter> userRateLimiters = new ConcurrentHashMap<>();

    /**
     * 检查用户是否允许创建新会话
     * 每分钟最多创建 MAX_SESSIONS_PER_MINUTE 个会话
     *
     * @param userId 用户ID
     * @return true-允许, false-限流
     */
    public boolean tryAcquireSession(String userId) {
        // 每秒产生 (10/60) ≈ 0.167 个令牌，即每 6 秒补充 1 个令牌
        // 一分钟内可产生 10 个令牌，满足"一分钟最多 10 次"的需求
        RateLimiter limiter = userRateLimiters.computeIfAbsent(userId,
                k -> RateLimiter.create(MAX_SESSIONS_PER_MINUTE / 60.0));

        boolean acquired = limiter.tryAcquire();
        if (!acquired) {
            log.warn("用户限流 - userId: {}, 1分钟内创建会话数已达上限", userId);
        }
        return acquired;
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