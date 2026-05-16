package com.czy.ai.web.advice;

import com.czy.ai.common.dto.Result;
import com.czy.ai.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * 全局异常处理器
 * 统一处理系统中的异常，返回标准化的错误响应
 *
 * @author chenzhenyu 2026年05月16日
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e       业务异常
     * @param request HTTP请求
     * @return 响应实体或 SseEmitter
     */
    @ExceptionHandler(BusinessException.class)
    public Object handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("业务异常: code={}, message={}", e.getCode(), e.getMessage());

        String accept = request.getHeader("Accept");
        String uri = request.getRequestURI();

        if (isSseRequest(accept, uri)) {
            return handleSseBusinessException(e);
        }

        return ResponseEntity.status(getHttpStatus(e.getCode()))
                .body(Result.fail(e.getCode(), e.getMessage()));
    }

    /**
     * 处理非法参数异常
     *
     * @param e 非法参数异常
     * @return 响应实体
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<String>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数异常: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.fail(400, e.getMessage()));
    }

    /**
     * 处理其他未捕获的异常
     *
     * @param e       异常
     * @param request HTTP请求
     * @return 响应实体或 SseEmitter
     */
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e, HttpServletRequest request) {
        log.error("全局异常处理: {}", e.getMessage(), e);

        String accept = request.getHeader("Accept");
        String uri = request.getRequestURI();

        if (isSseRequest(accept, uri)) {
            return handleSseException(e);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(500, "服务器内部错误"));
    }

    /**
     * 判断是否为 SSE 请求
     * 优先根据 URI 判断（/stream/ 路径一定是 SSE 请求）
     * 再根据 Accept header 判断
     */
    private boolean isSseRequest(String accept, String uri) {
        if (uri != null && uri.contains("/stream/")) {
            return true;
        }
        return accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE);
    }

    /**
     * 根据错误码获取 HTTP 状态码
     *
     * @param code 错误码
     * @return HTTP 状态码
     */
    private HttpStatus getHttpStatus(int code) {
        if (code >= 400 && code < 500) {
            return HttpStatus.valueOf(code);
        } else if (code >= 500) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        } else {
            return HttpStatus.OK;
        }
    }

    /**
     * 处理 SSE 业务异常
     *
     * @param e 业务异常
     * @return SseEmitter
     */
    private SseEmitter handleSseBusinessException(BusinessException e) {
        SseEmitter emitter = new SseEmitter(0L);
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data("{\"code\":" + e.getCode() + ",\"msg\":\"" + e.getMessage() + "\"}"));
            emitter.complete();
        } catch (IOException ex) {
            log.error("发送SSE错误消息失败", ex);
            emitter.completeWithError(ex);
        }
        return emitter;
    }

    /**
     * 处理 SSE 通用异常
     *
     * @param e 异常
     * @return SseEmitter
     */
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
