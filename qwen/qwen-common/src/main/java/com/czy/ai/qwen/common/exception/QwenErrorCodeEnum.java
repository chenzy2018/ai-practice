package com.czy.ai.qwen.common.exception;

import com.czy.ai.common.exception.BaseErrorCodeEnum;

/**
 * Qwen 错误码枚举
 *
 * @author chenzhenyu 2026年05月16日
 */
public enum QwenErrorCodeEnum implements BaseErrorCodeEnum {
    /**
     * 成功
     */
    SUCCESS(200, "success"),

    /**
     * 系统错误（默认）
     */
    SYSTEM_ERROR(500, "系统内部错误"),

    /**
     * 参数校验失败
     */
    PARAM_ERROR(400, "参数校验失败"),

    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权访问"),

    /**
     * 限流
     */
    RATE_LIMIT(429, "请求过于频繁，请稍后再试"),

    /**
     * 会话不存在
     */
    SESSION_NOT_FOUND(1001, "会话不存在"),

    /**
     * 会话已过期
     */
    SESSION_EXPIRED(1002, "会话已过期"),

    /**
     * API调用失败
     */
    API_CALL_FAILED(2001, "API调用失败"),

    /**
     * API密钥无效
     */
    API_KEY_INVALID(2002, "API密钥无效"),

    /**
     * 请求格式错误
     */
    REQUEST_FORMAT_ERROR(2003, "请求格式错误"),

    /**
     * 响应解析失败
     */
    RESPONSE_PARSE_ERROR(2004, "响应解析失败");

    private final int code;
    private final String message;

    QwenErrorCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
