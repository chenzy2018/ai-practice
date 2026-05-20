package com.czy.ai.langchain4j.controller;

import com.czy.ai.common.annotation.ControllerLog;
import com.czy.ai.common.dto.Result;
import com.czy.ai.langchain4j.AiChatProviderFactory;
import com.czy.ai.langchain4j.AiType;
import com.czy.ai.langchain4j.IAiChatProvider;
import com.czy.ai.langchain4j.chatrequest.ChatRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author chenzhenyu 2026年05月19日 上午10:24:38
 */
@RestController
@RequestMapping("/ai/langchain")
public class LangChainChatController {

    @Autowired
    private AiChatProviderFactory aiChatProviderFactory;

    /**
     * langchain对话
     *
     * @param question    问题
     * @param aiType   ai类型
     * @return AI 回复
     */
    @GetMapping("/chat")
    @ControllerLog(desc = "langchain 对话")
    public Result<String> chat(@RequestParam String question,
                               @RequestParam String aiType) {
        return aiChatProviderFactory.getAiChatProvider(AiType.valueOf(aiType)).chat(question);
    }

    /**
     * 自定义对话 - 支持完整的聊天参数配置
     *
     * @param chatRequest 自定义聊天请求
     * @return AI 回复
     */
    @PostMapping("/customChat")
    @ControllerLog(desc = "langchain 自定义对话")
    public Result<String> customChat(@RequestBody ChatRequest chatRequest) {
        if (chatRequest == null || chatRequest.getQuestion() == null) {
            return Result.fail("请求参数不能为空");
        }
        
        AiType aiType = AiType.valueOf(chatRequest.getAiType());
        return aiChatProviderFactory.getAiChatProvider(aiType).customChat(chatRequest);
    }
}
