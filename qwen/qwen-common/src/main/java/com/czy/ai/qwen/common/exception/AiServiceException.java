package com.czy.ai.qwen.common.exception;

public class AiServiceException extends RuntimeException {
    private final int code;

    public AiServiceException(String message) {
        super(message);
        this.code = 500;
    }

    public AiServiceException(int code, String message) {
        super(message);
        this.code = code;
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }

    public int getCode() {
        return code;
    }
}