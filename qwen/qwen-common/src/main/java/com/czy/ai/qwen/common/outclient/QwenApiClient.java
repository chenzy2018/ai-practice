package com.czy.ai.qwen.common.outclient;

import com.czy.ai.common.Message;
import com.czy.ai.qwen.common.config.QwenConfig;
import com.czy.ai.qwen.common.util.AiStringUtils;
import com.czy.ai.web.client.OkHttpService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import jakarta.annotation.Resource;

/**
 * Qwen API 客户端
 * 封装 Qwen AI 服务的调用逻辑
 *
 * @author nober 2026年05月15日
 */
@Slf4j
@Service
public class QwenApiClient {

    @Resource
    private QwenConfig qwenConfig;

    @Resource
    private OkHttpService okHttpService;

    public String sendRequest(List<Message> messages, String model, Double temperature) throws IOException {
        String jsonBody = buildRequestJson(messages, model, temperature);
        log.debug("AI请求体 - {}", AiStringUtils.truncate(jsonBody, 1000));

        String response = okHttpService.post(qwenConfig.getApiUrl(), jsonBody, qwenConfig.getApiKey());
        log.debug("AI响应体 - {}", AiStringUtils.truncate(response, 2000));

        return response;
    }

    public Response sendStreamRequest(List<Message> messages, String model, Double temperature) throws IOException {
        String jsonBody = buildStreamRequestJson(messages, model, temperature);
        log.debug("流式请求体 - {}", AiStringUtils.truncate(jsonBody, 1000));

        return okHttpService.postStream(qwenConfig.getApiUrl(), jsonBody, qwenConfig.getApiKey());
    }

    private String buildRequestJson(List<Message> messages, String model, Double temperature) throws JsonProcessingException {
        QwenRequest requestBody = QwenRequest.builder()
                .model(model)
                .input(QwenRequest.Input.builder().messages(messages).build())
                .parameters(QwenRequest.Parameters.builder().temperature(temperature).build())
                .build();
        return AiStringUtils.toJson(requestBody);
    }

    private String buildStreamRequestJson(List<Message> messages, String model, Double temperature) throws JsonProcessingException {
        QwenRequest requestBody = QwenRequest.builder()
                .model(model)
                .input(QwenRequest.Input.builder().messages(messages).build())
                .parameters(QwenRequest.Parameters.builder()
                        .temperature(temperature)
                        .stream(true)
                        .build())
                .build();
        return AiStringUtils.toJson(requestBody);
    }
}
