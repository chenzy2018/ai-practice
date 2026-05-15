package com.czy.ai.qwen.web.advice;

import com.czy.ai.qwen.common.dto.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e, HttpServletRequest request) {
        log.error("全局异常处理: {}", e.getMessage(), e);
        
        String accept = request.getHeader("Accept");
        String uri = request.getRequestURI();
        
        if (accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE) || 
            uri != null && uri.contains("/stream/")) {
            return handleSseException(e);
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(500, "服务器内部错误"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<String>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数异常: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.fail(400, e.getMessage()));
    }

    private SseEmitter handleSseException(Exception e) {
        SseEmitter emitter = new SseEmitter(0L);
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data("{\"code\":500,\"msg\":\"" + e.getMessage() + "\"}"));
            emitter.complete();
        } catch (IOException ex) {
            log.error("发送SSE错误消息失败", ex);
            emitter.completeWithError(ex);
        }
        return emitter;
    }
}