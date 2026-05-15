package com.czy.qwen.controller;

import com.czy.qwen.req.ChatRequest;
import com.czy.qwen.server.IQwenStreamService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Qwen AI 流式聊天控制器
 *
 * @author chenzhenyu 2026年05月15日
 */
@RestController
@RequestMapping("/ai/stream")
@CrossOrigin(origins = "*")
public class QwenAiStreamController {

    @Resource
    private IQwenStreamService qwenStreamService;

    /**
     * 流式多轮对话（带上下文）
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody ChatRequest request, HttpServletResponse response) {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("X-Accel-Buffering", "no");
        
        return qwenStreamService.chatStreamWithContext(request);
    }
}