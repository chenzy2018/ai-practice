package com.czy.qwen.controller;

import com.czy.qwen.resp.Result;
import com.czy.qwen.server.IQwenAiService;
import com.czy.qwen.server.IQwenAiService.ChatResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import javax.annotation.Resource;

/**
 * Qwen AI 聊天控制器 - Facade 接口
 * 支持基于sessionId的会话隔离
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
    public Result<ChatResponse> chatWithContext(@RequestParam String question,
                                                @RequestParam(required = false) String sessionId,
                                                @RequestParam(required = false) String model,
                                                @RequestParam(required = false) Double temperature) {
        return qwenAiService.chatWithContext(sessionId, question, model, temperature);
    }

    @GetMapping("/ai/clear")
    public Result<String> clear(@RequestParam(required = false) String sessionId) {
        return qwenAiService.clearContext(sessionId);
    }

    @GetMapping("/ai/session/new")
    public Result<String> createSession() {
        String sessionId = qwenAiService.generateSessionId();
        return Result.success(sessionId);
    }

    @GetMapping("/ai/session/list")
    public Result<Set<String>> listSessions() {
        return qwenAiService.listSessions();
    }

    @DeleteMapping("/ai/session/{sessionId}")
    public Result<String> removeSession(@PathVariable String sessionId) {
        return qwenAiService.removeSession(sessionId);
    }
}