package com.czy.ai.langchain4j.provider;

import com.czy.ai.langchain4j.AiType;
import com.czy.ai.langchain4j.chatrequest.ChatRequest;
import com.czy.ai.langchain4j.config.QwenChatConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author nober
 */
@Slf4j
@Service
public class QwenAiChatProvider extends AbstractAiChatProvider<ChatRequest> {

    @Autowired
    private QwenChatConfig qwenChatConfig;

    @Override
    public AiType supportAiModel() {
        return AiType.QWEN;
    }

    @Override
    protected String getApiUrl() {
        return qwenChatConfig.getApiUrl();
    }

    @Override
    protected Map<String, Object> getBody(ChatRequest request) {
        return Map.of(
                "stream", false,
                "model", request.getModel(),
                "temperature", request.getTemperature(),
                "top_p", request.getTopP(),
                "max_tokens", request.getMaxTokens()
        );
    }
}
