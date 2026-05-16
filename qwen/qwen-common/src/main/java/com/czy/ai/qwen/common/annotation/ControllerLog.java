package com.czy.ai.qwen.common.annotation;

import java.lang.annotation.*;

/**
 * Controller 日志注解
 * 标注在 Controller 方法上，自动打印入参和出参日志
 *
 * @author chenzhenyu 2026年05月15日
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ControllerLog {
    /**
     * 方法描述
     *
     * @return 方法描述
     */
    String desc() default "";
}
