package com.czy.qwen.controller;

import com.czy.qwen.resp.Result;
import com.czy.qwen.server.IQwenAiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Qwen AI 聊天控制器 - Facade 接口
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
    public Result<String> chatWithContext(@RequestParam String question,
                                          @RequestParam(required = false) String model,
                                          @RequestParam(required = false) Double temperature) {
        return qwenAiService.chatWithContext(question, model, temperature);
    }

    @GetMapping("/ai/clear")
    public Result<String> clear() {
        return qwenAiService.clearContext();
    }

    @PostMapping("/api/ai/chat")
    public Result<String> chatPost(@RequestBody Map<String, Object> request) {
        String question = (String) request.get("question");
        String model = (String) request.get("model");
        Double temperature = request.get("temperature") != null 
                ? ((Number) request.get("temperature")).doubleValue() 
                : null;
        
        if (question == null || question.trim().isEmpty()) {
            return Result.fail("问题不能为空");
        }
        return qwenAiService.chat(question, model, temperature);
    }

    @PostMapping("/api/ai/chatWithContext")
    public Result<String> chatWithContextPost(@RequestBody Map<String, Object> request) {
        String question = (String) request.get("question");
        String model = (String) request.get("model");
        Double temperature = request.get("temperature") != null 
                ? ((Number) request.get("temperature")).doubleValue() 
                : null;
        
        if (question == null || question.trim().isEmpty()) {
            return Result.fail("问题不能为空");
        }
        return qwenAiService.chatWithContext(question, model, temperature);
    }

    @PostMapping("/api/ai/clear")
    public Result<String> clearPost() {
        return qwenAiService.clearContext();
    }
}