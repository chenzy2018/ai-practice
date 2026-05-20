package com.czy.ai.langchain4j.register;

import com.czy.ai.langchain4j.AiType;
import com.czy.ai.langchain4j.ChatLanguageModelFactory;
import com.czy.ai.langchain4j.config.WebullChatConfig;
import dev.langchain4j.model.openai.OpenAiChatModel;
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
    private ChatLanguageModelFactory chatLanguageModelFactory;

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
        chatLanguageModelFactory.registerAiChatProvider(AiType.WEBULL, build);
    }
}
