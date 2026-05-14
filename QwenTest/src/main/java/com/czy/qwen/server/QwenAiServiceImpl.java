package com.czy.qwen.server;

import com.czy.qwen.config.QwenConfig;
import com.czy.qwen.dto.Message;
import com.czy.qwen.dto.QwenRequest;
import com.czy.qwen.resp.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Qwen AI 服务实现类
 *
 * @author chenzhenyu 2026年05月14日
 */
@Service
public class QwenAiServiceImpl implements IQwenAiService {

    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    @Resource
    private QwenConfig qwenConfig;

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final List<Message> messageHistory;

    public QwenAiServiceImpl() {
        this.client = createOkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.messageHistory = Collections.synchronizedList(new ArrayList<>());
    }

    @Override
    public Result<String> chat(String question) {
        return chat(question, DEFAULT_MODEL, DEFAULT_TEMPERATURE);
    }

    @Override
    public Result<String> chat(String question, String model, Double temperature) {
        try {
            String effectiveModel = getEffectiveModel(model);
            Double effectiveTemperature = getEffectiveTemperature(temperature);
            
            List<Message> messages = Collections.singletonList(Message.user(question));
            String response = sendRequest(messages, effectiveModel, effectiveTemperature);
            String answer = parseAnswer(response);
            return Result.success(answer);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @Override
    public Result<String> chatWithContext(String question) {
        return chatWithContext(question, DEFAULT_MODEL, DEFAULT_TEMPERATURE);
    }

    @Override
    public Result<String> chatWithContext(String question, String model, Double temperature) {
        try {
            String effectiveModel = getEffectiveModel(model);
            Double effectiveTemperature = getEffectiveTemperature(temperature);
            
            messageHistory.add(Message.user(question));
            String response = sendRequest(new ArrayList<>(messageHistory), effectiveModel, effectiveTemperature);
            String answer = parseAnswer(response);
            messageHistory.add(Message.assistant(answer));
            return Result.success(answer);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @Override
    public Result<String> clearContext() {
        messageHistory.clear();
        return Result.success("上下文已清空");
    }

    private String getEffectiveModel(String model) {
        return model != null && !model.trim().isEmpty() ? model.trim() : DEFAULT_MODEL;
    }

    private Double getEffectiveTemperature(Double temperature) {
        if (temperature == null) {
            return DEFAULT_TEMPERATURE;
        }
        return Math.max(0.0, Math.min(1.0, temperature));
    }

    private OkHttpClient createOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    private String sendRequest(List<Message> messages, String model, Double temperature) throws JsonProcessingException {
        QwenRequest requestBody = new QwenRequest(model, messages, temperature);
        String json = objectMapper.writeValueAsString(requestBody);

        Request request = new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(json, JSON_MEDIA_TYPE))
                .addHeader("Authorization", "Bearer " + qwenConfig.getApiKey())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                throw new RuntimeException("API调用失败: " + response.code() + " -> " + errorBody);
            }
            return response.body() != null ? response.body().string() : "";
        } catch (Exception e) {
            throw new RuntimeException("请求发送失败: " + e.getMessage(), e);
        }
    }

    private String parseAnswer(String response) throws IOException {
        JsonNode rootNode = objectMapper.readTree(response);
        JsonNode outputNode = rootNode.path("output");
        JsonNode textNode = outputNode.path("text");
        
        if (textNode.isTextual()) {
            return textNode.asText();
        }
        throw new RuntimeException("解析响应失败: " + response);
    }

    private Result<String> handleException(Exception e) {
        return Result.fail("异常: " + e.getMessage());
    }
}