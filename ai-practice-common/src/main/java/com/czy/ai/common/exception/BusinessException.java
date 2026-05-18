package com.czy.ai.common.exception;

import lombok.Getter;

/**
 * 业务异常类
 * 业务层统一使用此类抛出异常
 *
 * @author nober 2026年05月16日
 */
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    @Getter
    private final int code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * 使用错误码枚举创建异常
     *
     * @param errorCode 错误码枚举
     */
    public BusinessException(BaseErrorCodeEnum errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    /**
     * 使用错误码枚举和自定义消息创建异常
     *
     * @param errorCode 错误码枚举
     * @param message   自定义消息
     */
    public BusinessException(BaseErrorCodeEnum errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.message = message;
    }

    /**
     * 使用自定义消息创建异常（默认错误码500）
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = CommonErrorCodeEnum.SYSTEM_ERROR.getCode();
        this.message = message;
    }

    /**
     * 使用自定义错误码和消息创建异常
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 使用自定义消息和异常原因创建异常（默认错误码500）
     *
     * @param message 错误消息
     * @param cause   异常原因
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = CommonErrorCodeEnum.SYSTEM_ERROR.getCode();
        this.message = message;
    }

    /**
     * 使用错误码枚举和异常原因创建异常
     *
     * @param errorCode 错误码枚举
     * @param cause     异常原因
     */
    public BusinessException(BaseErrorCodeEnum errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    @Override
    public String getMessage() {
        return message;
    }
}
