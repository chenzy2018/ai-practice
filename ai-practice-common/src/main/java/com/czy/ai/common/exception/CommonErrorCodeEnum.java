package com.czy.ai.common.exception;

public enum CommonErrorCodeEnum implements BaseErrorCodeEnum {
    SUCCESS(200, "success"),
    SYSTEM_ERROR(500, "系统内部错误"),
    PARAM_ERROR(400, "参数校验失败"),
    UNAUTHORIZED(401, "未授权访问"),
    RATE_LIMIT(429, "请求过于频繁，请稍后再试");

    private final int code;
    private final String message;

    CommonErrorCodeEnum(int code, String message) {
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
