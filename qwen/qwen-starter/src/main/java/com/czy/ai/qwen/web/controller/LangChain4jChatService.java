package com.czy.ai.qwen.web.controller;

import com.czy.ai.common.dto.Result;
import com.czy.ai.qwen.langchain4j.ILangChain4jAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenzhenyu 2026年05月18日 下午21:35:06
 */
@RestController
@RequestMapping("/ai/LangChain4j/qwen")
public class LangChain4jChatService {

    @Autowired
    private ILangChain4jAiService langChain4jAiService;

    @GetMapping("/chat")
    public Result<String> chat(String content) {
        return langChain4jAiService.chat(content);
    }
}
