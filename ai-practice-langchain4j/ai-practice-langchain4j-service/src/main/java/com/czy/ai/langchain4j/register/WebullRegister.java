package com.czy.ai.langchain4j.register;

import com.czy.ai.langchain4j.AiType;
import com.czy.ai.langchain4j.ChatModelFactory;
import com.czy.ai.langchain4j.config.WebullChatConfig;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * webull LangChain4j 配置类
 *
 * @author nober
 */
@Configuration
public class WebullRegister implements InitializingBean {

    @Autowired
    private WebullChatConfig webullChatConfig;

    @Autowired
    private ChatModelFactory chatModelFactory;

    /**
     * 注入通义千问对话模型
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        OpenAiChatModel build = OpenAiChatModel.builder()
                .apiKey(webullChatConfig.getApiKey())
                .baseUrl(webullChatConfig.getApiUrl())
                .modelName(webullChatConfig.getModel())
                .temperature(webullChatConfig.getTemperature())
                .maxTokens(webullChatConfig.getMaxTokens())
                .build();
        chatModelFactory.registerChatModel(AiType.WEBULL, build);
        // 注册流式模型
        OpenAiStreamingChatModel buildStreaming = OpenAiStreamingChatModel.builder()
                .apiKey(webullChatConfig.getApiKey())
                .baseUrl(webullChatConfig.getApiUrl())
                .modelName(webullChatConfig.getModel())
                .temperature(webullChatConfig.getTemperature())
                .maxTokens(webullChatConfig.getMaxTokens())
                .build();
        chatModelFactory.registerStreamingChatModel(AiType.WEBULL, buildStreaming);
    }
}
