package com.czy.ai.langchain4j.register;

import com.czy.ai.langchain4j.AiType;
import com.czy.ai.langchain4j.ChatLanguageModelFactory;
import com.czy.ai.langchain4j.config.QwenChatConfig;
import dev.langchain4j.model.openai.OpenAiChatModel;
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
     * 注入通义千问对话模型
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        OpenAiChatModel build = OpenAiChatModel.builder()
                .apiKey(qwenChatConfig.getApiKey())
                .baseUrl(qwenChatConfig.getApiUrl())
                .modelName(qwenChatConfig.getModel())
                .temperature(qwenChatConfig.getTemperature())
                .maxTokens(qwenChatConfig.getMaxTokens())
                .build();
        chatLanguageModelFactory.registerAiChatProvider(AiType.QWEN, build);
    }
}
