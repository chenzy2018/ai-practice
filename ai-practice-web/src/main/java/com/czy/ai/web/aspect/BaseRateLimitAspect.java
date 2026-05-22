package com.czy.ai.web.aspect;

import com.czy.ai.common.annotation.RateLimit;
import com.czy.ai.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
        String userId = getUserId(request, joinPoint);
        String sessionId = getSessionId(joinPoint);

        if (!tryAcquireSession(userId, sessionId)) {
            throw new BusinessException(429, "请求过于频繁，请稍后再试");
        }

        return joinPoint.proceed();
    }

    protected abstract boolean tryAcquireSession(String userId, String sessionId);

    protected abstract String getSessionId(ProceedingJoinPoint joinPoint);

    protected HttpServletRequest getRequest(ProceedingJoinPoint joinPoint) {
        // 首先从方法参数中获取
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof HttpServletRequest) {
                return (HttpServletRequest) arg;
            }
        }
        // 如果参数中没有，从 RequestContextHolder 获取
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return attributes.getRequest();
        }
        return null;
    }

    /**
     * 获取用户ID，按以下优先级查找：
     * 1. 请求头 X-User-Id
     * 2. 请求参数 userId
     * 3. 请求体中的 userId 字段（通过反射）
     * 4. IP地址
     * 5. 默认值 anonymous
     *
     * @param request   HTTP请求
     * @param joinPoint 连接点
     * @return 用户ID
     */
    protected String getUserId(HttpServletRequest request, ProceedingJoinPoint joinPoint) {
        String userId = null;

        // 1. 从请求头获取
        if (request != null) {
            userId = request.getHeader("X-User-Id");
            if (userId != null && !userId.isEmpty()) {
                log.debug("从请求头获取 userId: {}", userId);
                return userId;
            }
        }

        // 2. 从请求参数（query params）获取
        if (request != null) {
            userId = request.getParameter("userId");
            if (userId != null && !userId.isEmpty()) {
                log.debug("从请求参数获取 userId: {}", userId);
                return userId;
            }
        }

        // 3. 从请求体（方法参数对象）中获取
        userId = getUserIdFromArgs(joinPoint);
        if (userId != null && !userId.isEmpty()) {
            log.debug("从请求体获取 userId: {}", userId);
            return userId;
        }

        // 4. 从IP地址获取
        if (request != null) {
            userId = request.getRemoteAddr();
            log.debug("从IP地址获取 userId: {}", userId);
            return userId;
        }

        // 5. 返回默认值
        log.warn("无法获取 userId，使用默认值");
        return "anonymous";
    }

    /**
     * 从方法参数对象中通过反射获取 userId
     *
     * @param joinPoint 连接点
     * @return 用户ID，如果找不到返回 null
     */
    private String getUserIdFromArgs(ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg == null) {
                    continue;
                }

                // 检查参数名是否为 userId
                if (paramNames != null && paramNames.length > i) {
                    if ("userId".equals(paramNames[i]) || "userId".equalsIgnoreCase(paramNames[i])) {
                        return String.valueOf(arg);
                    }
                }

                // 通过反射获取对象的 userId 字段
                String userId = getUserIdFromObject(arg);
                if (userId != null) {
                    return userId;
                }
            }
        } catch (Exception e) {
            log.warn("从方法参数获取 userId 失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 通过反射从对象中获取 userId 字段值
     *
     * @param obj 对象
     * @return 用户ID，如果找不到返回 null
     */
    private String getUserIdFromObject(Object obj) {
        if (obj == null) {
            return null;
        }

        Class<?> clazz = obj.getClass();

        // 尝试获取字段
        try {
            Field userIdField = clazz.getDeclaredField("userId");
            userIdField.setAccessible(true);
            Object value = userIdField.get(obj);
            if (value != null) {
                return String.valueOf(value);
            }
        } catch (NoSuchFieldException e) {
            // 忽略，继续尝试其他方式
        } catch (Exception e) {
            log.warn("获取字段 userId 失败: {}", e.getMessage());
        }

        // 尝试通过 getter 方法获取
        try {
            Method getter = clazz.getMethod("getUserId");
            Object value = getter.invoke(obj);
            if (value != null) {
                return String.valueOf(value);
            }
        } catch (NoSuchMethodException e) {
            // 忽略，继续尝试父类
        } catch (Exception e) {
            log.warn("调用 getUserId() 方法失败: {}", e.getMessage());
        }

        // 尝试获取 user_id 字段（下划线风格）
        try {
            Field userIdField = clazz.getDeclaredField("user_id");
            userIdField.setAccessible(true);
            Object value = userIdField.get(obj);
            if (value != null) {
                return String.valueOf(value);
            }
        } catch (NoSuchFieldException e) {
            // 忽略
        } catch (Exception e) {
            log.warn("获取字段 user_id 失败: {}", e.getMessage());
        }

        // 如果是包装类型或基本类型，直接返回
        if (obj instanceof String) {
            return (String) obj;
        } else if (obj instanceof Number) {
            return obj.toString();
        }

        return null;
    }
}
