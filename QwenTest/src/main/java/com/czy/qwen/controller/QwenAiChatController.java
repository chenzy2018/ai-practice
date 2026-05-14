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
}