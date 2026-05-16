package com.czy.ai.common.exception;

/**
 * 基础错误码接口
 * 所有AI模块的错误码枚举都应实现此接口
 */
public interface BaseErrorCodeEnum {
    int getCode();
    String getMessage();
}
