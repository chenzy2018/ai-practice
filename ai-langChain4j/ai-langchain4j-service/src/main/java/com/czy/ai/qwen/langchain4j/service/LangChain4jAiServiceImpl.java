package com.czy.ai.qwen.langchain4j.service;

import com.czy.ai.common.dto.Result;
import com.czy.ai.qwen.langchain4j.ILangChain4jAiService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * LangChain4j AI 服务实现
 * 
 * 当前使用 MockChatModel 模拟响应，完全不涉及任何 HTTP 请求
 * 
 * @author nober
 */
@Slf4j
@Service
public class LangChain4jAiServiceImpl implements ILangChain4jAiService {

    @Resource
    private ChatLanguageModel chatLanguageModel;

    @Override
    public Result<String> chat(String content){
        String answer = chatLanguageModel.generate(content);
        return Result.success(answer);
    }

}
