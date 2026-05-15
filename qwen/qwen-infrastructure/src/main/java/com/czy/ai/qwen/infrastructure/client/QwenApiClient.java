package com.czy.ai.qwen.infrastructure.client;

import com.czy.ai.qwen.common.util.AiStringUtils;
import com.czy.ai.qwen.domain.Message;
import com.czy.ai.qwen.infrastructure.config.QwenConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import javax.annotation.Resource;

/**
 * Qwen API 客户端
 * 封装 Qwen AI 服务的调用逻辑
 *
 * @author chenzhenyu 2026年05月15日
 */
@Slf4j
@Service
public class QwenApiClient {

    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

    @Resource
    private QwenConfig qwenConfig;

    @Resource
    private OkHttpService okHttpService;

    public String sendRequest(List<Message> messages, String model, Double temperature) throws IOException {
        String jsonBody = buildRequestJson(messages, model, temperature);
        log.debug("AI请求体 - {}", AiStringUtils.truncate(jsonBody, 1000));

        String response = okHttpService.post(API_URL, jsonBody, qwenConfig.getApiKey());
        log.debug("AI响应体 - {}", AiStringUtils.truncate(response, 2000));

        return response;
    }

    public Response sendStreamRequest(List<Message> messages, String model, Double temperature) throws IOException {
        String jsonBody = buildStreamRequestJson(messages, model, temperature);
        log.debug("流式请求体 - {}", AiStringUtils.truncate(jsonBody, 1000));

        return okHttpService.postStream(API_URL, jsonBody, qwenConfig.getApiKey());
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