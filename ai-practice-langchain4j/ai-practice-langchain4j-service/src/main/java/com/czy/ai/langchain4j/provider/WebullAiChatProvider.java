package com.czy.ai.langchain4j.provider;

import com.czy.ai.common.dto.Result;
import com.czy.ai.common.session.SessionContext;
import com.czy.ai.langchain4j.AiType;
import com.czy.ai.langchain4j.chatrequest.ChatRequest;
import com.czy.ai.langchain4j.chatrequest.WebullChatRequest;
import com.czy.ai.langchain4j.config.QwenChatConfig;
import com.czy.ai.langchain4j.config.WebullChatConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author nober
 */
@Slf4j
@Service
public class WebullAiChatProvider extends AbstractAiChatProvider<WebullChatRequest> {

    @Autowired
    private WebullChatConfig webullChatConfig;

    @Override
    public AiType supportAiModel() {
        return AiType.WEBULL;
    }

    @Override
    protected String getApiUrl() {
        return webullChatConfig.getApiUrl();
    }

    @Override
    protected Map<String, Object> getBody(ChatRequest request) {
        return Map.of();
    }

    @Override
    public Result<String> customChat(WebullChatRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", request.getUserId());

        SessionContext session = sessionManage.getOrCreate(request.getSessionId(), request.getUserId(), request.getSystemPrompt());
        sessionManage.updateLastActiveTime(request.getSessionId());

        Map<String, Object> body = Map.of(
                "stream", request.getStream() == null ? webullChatConfig.getStream() : request.getStream(),
                "model", StringUtils.isEmpty(request.getModel()) ? webullChatConfig.getModel() : request.getModel(),
                "temperature", request.getTemperature() == null ? webullChatConfig.getTemperature() : request.getTemperature(),
                "top_p", request.getTopP() == null ? webullChatConfig.getTopP() : request.getTopP(),
                "max_tokens", request.getMaxTokens() == null ? webullChatConfig.getMaxTokens() : request.getMaxTokens(),
                "thinking", Map.of(
                        "type", webullChatConfig.getThinkingType(),
                        "budget_tokens", webullChatConfig.getThinkingBudgetTokens()
                )
        );
        body.put("messages", session.getMessages());

        return getResult(body, headers, session);
    }
}
