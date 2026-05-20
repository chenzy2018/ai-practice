package com.czy.ai.langchain4j.provider;

import com.czy.ai.common.dto.Result;
import com.czy.ai.common.session.SessionContext;
import com.czy.ai.common.session.SessionManage;
import com.czy.ai.langchain4j.ChatLanguageModelFactory;
import com.czy.ai.langchain4j.IAiChatProvider;
import com.czy.ai.langchain4j.chatrequest.ChatRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * longchain4j 抽象类
 *
 * @author chenzhenyu 2026年05月18日 下午22:04:20
 */
public abstract class AbstractAiChatProvider<T extends ChatRequest> implements IAiChatProvider<T> {

    @Autowired
    private ChatLanguageModelFactory aiChatProviderFactory;
    @Autowired
    protected RestTemplate restTemplate;
    @Autowired
    protected SessionManage sessionManage;

    @Override
    public Result<String> chat(String content) {
        String answer = aiChatProviderFactory.getAiChatProvider(supportAiModel()).generate(content);
        return Result.success(answer);
    }

    @Override
    public Result<String> customChat(T chatRequest) {
        return customChatNormal(chatRequest);
    }

    protected abstract String getApiUrl();

    protected abstract Map<String, Object> getBody(ChatRequest chatRequest);

    protected Result<String> customChatNormal(ChatRequest chatRequest) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", chatRequest.getUserId());

        SessionContext session = sessionManage.getOrCreate(chatRequest.getSessionId(), chatRequest.getUserId(), chatRequest.getSystemPrompt());
        sessionManage.updateLastActiveTime(chatRequest.getSessionId());

        Map<String, Object> body = getBody(chatRequest);
        body.put("messages", session.getMessages());

        return getResult(body, headers, session);
    }

    protected Result<String> getResult(Map<String, Object> body, HttpHeaders headers, SessionContext session) {
        // 3. 发送请求
        ResponseEntity<Map> resp = restTemplate.postForEntity(getApiUrl(),
                new HttpEntity<>(body, headers), Map.class);

        // 4. 解析返回
        var choices = (List<Map>) resp.getBody().get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.getFirst().get("message");
        String content = (String) message.get("content");
        session.addAssistantMessage(content);
        return Result.success(content);
    }
}
