package com.czy.ai.qwen.web.aspect;

import com.czy.ai.qwen.common.annotation.ControllerLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller 日志切面
 * 拦截带 @ControllerLog 注解的方法，打印入参和出参日志
 *
 * @author chenzhenyu 2026年05月15日
 */
@Aspect
@Component
@Slf4j
public class ControllerLogAspect {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 定义切点：拦截所有带 @ControllerLog 注解的方法
     */
    @Pointcut("@annotation(com.czy.ai.qwen.common.annotation.ControllerLog)")
    public void controllerLogPointcut() {
    }

    /**
     * 环绕通知，打印入参和出参
     *
     * @param joinPoint 连接点
     * @return 方法返回值
     * @throws Throwable 方法执行异常
     */
    @Around("controllerLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        ControllerLog controllerLog = method.getAnnotation(ControllerLog.class);
        String methodDesc = controllerLog != null ? controllerLog.desc() : method.getName();

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        // 打印入参
        printRequestLog(methodDesc, method, joinPoint.getArgs(), request);

        // 执行目标方法
        Object result = joinPoint.proceed();

        // 打印出参
        long costTime = System.currentTimeMillis() - startTime;
        printResponseLog(methodDesc, result, costTime);

        return result;
    }

    /**
     * 打印请求日志
     *
     * @param methodDesc 方法描述
     * @param method     方法对象
     * @param args       方法参数
     * @param request    HTTP请求
     */
    private void printRequestLog(String methodDesc, Method method, Object[] args, HttpServletRequest request) {
        log.info("========================================");
        log.info("[Controller 入参] {}", methodDesc);
        log.info("请求路径: {} {}", request != null ? request.getMethod() : "N/A", request != null ? request.getRequestURI() : "N/A");

        // 打印方法参数
        Parameter[] parameters = method.getParameters();
        Map<String, Object> paramMap = new HashMap<>();
        if (parameters.length > 0 && args.length == parameters.length) {
            for (int i = 0; i < parameters.length; i++) {
                // 跳过 HttpServletRequest 和 HttpServletResponse
                String paramType = parameters[i].getType().getSimpleName();
                if ("HttpServletRequest".equals(paramType) || "HttpServletResponse".equals(paramType)) {
                    continue;
                }
                paramMap.put(parameters[i].getName(), args[i]);
            }
        }
        try {
            log.info("方法参数: {}", objectMapper.writeValueAsString(paramMap));
        } catch (Exception e) {
            log.info("方法参数: {}", paramMap);
        }
        log.info("========================================");
    }

    /**
     * 打印响应日志
     *
     * @param methodDesc 方法描述
     * @param result     方法返回值
     * @param costTime   执行耗时
     */
    private void printResponseLog(String methodDesc, Object result, long costTime) {
        log.info("========================================");
        log.info("[Controller 出参] {}", methodDesc);
        log.info("耗时: {}ms", costTime);
        try {
            log.info("出参: {}", objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            log.info("出参: {}", result);
        }
        log.info("========================================");
    }
}
