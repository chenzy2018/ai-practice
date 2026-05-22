package com.czy.ai.langchain4j.register;

import com.czy.ai.langchain4j.AiType;
import com.czy.ai.langchain4j.ChatLanguageModelFactory;
import com.czy.ai.langchain4j.config.QwenChatConfig;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * qwen LangChain4j 配置类
 *
 * @author nober
 */
@Configuration
public class QwenRegister implements InitializingBean {

    @Autowired
    private QwenChatConfig qwenChatConfig;

    @Autowired
    private ChatLanguageModelFactory chatLanguageModelFactory;

    /**
     * 注入对话模型和流式模型
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(qwenChatConfig.getApiKey())
                .baseUrl(qwenChatConfig.getApiUrl())
                .modelName(qwenChatConfig.getModel())
                .temperature(qwenChatConfig.getTemperature())
                .maxTokens(qwenChatConfig.getMaxTokens())
                .build();
        chatLanguageModelFactory.registerAiChatProvider(AiType.QWEN, chatModel);

        OpenAiStreamingChatModel streamingModel = OpenAiStreamingChatModel.builder()
                .apiKey(qwenChatConfig.getApiKey())
                .baseUrl(qwenChatConfig.getApiUrl())
                .modelName(qwenChatConfig.getModel())
                .temperature(qwenChatConfig.getTemperature())
                .maxTokens(qwenChatConfig.getMaxTokens())
                .build();
        chatLanguageModelFactory.registerStreamingChatProvider(AiType.QWEN, streamingModel);
    }
}
