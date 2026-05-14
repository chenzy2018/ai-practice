package com.czy.qwen.controller;

import com.czy.qwen.resp.Result;
import com.czy.qwen.server.QwenAiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author chenzhenyu 2026年05月14日 上午10:52:37
 */
@RestController
public class QwenAiChatController {

    @Resource
    private QwenAiService qwenAiService;

    @GetMapping("/ai/chat")
    public Result<String> aiChat(@RequestParam String question) {
        return qwenAiService.chat(question);
    }
}
