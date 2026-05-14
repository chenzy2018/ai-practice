package com.czy.qwen.controller;

import com.czy.qwen.dto.ChatRequest;
import com.czy.qwen.resp.Result;
import com.czy.qwen.server.IQwenAiService;
import com.czy.qwen.server.IQwenAiService.ChatResponse;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;

/**
 * Qwen AI 聊天控制器
 *
 * @author chenzhenyu 2026年05月14日
 */
@RestController
public class QwenAiChatController {

    @Resource
    private IQwenAiService qwenAiService;

    @GetMapping("/ai/chat")
    public Result<String> chat(@RequestParam String question,
                               @RequestParam(required = false) String model,
                               @RequestParam(required = false) Double temperature) {
        return qwenAiService.chat(question, model, temperature);
    }

    @GetMapping("/ai/chatWithContext")
    public Result<ChatResponse> chatWithContext(ChatRequest request) {
        return qwenAiService.chatWithContext(request);
    }

    @GetMapping("/ai/clear")
    public Result<String> clear(@RequestParam(required = false) String sessionId) {
        return qwenAiService.clearContext(sessionId);
    }

    @GetMapping("/ai/session/new")
    public Result<String> createSession() {
        return Result.success(qwenAiService.generateSessionId());
    }

    @GetMapping("/ai/session/list")
    public Result<Set<String>> listSessions() {
        return qwenAiService.listSessions();
    }

    @GetMapping("/ai/session/info")
    public Result<Map<String, Object>> getSessionInfo(@RequestParam String sessionId) {
        return qwenAiService.getSessionInfo(sessionId);
    }

    @DeleteMapping("/ai/session/{sessionId}")
    public Result<String> removeSession(@PathVariable String sessionId) {
        return qwenAiService.removeSession(sessionId);
    }
}