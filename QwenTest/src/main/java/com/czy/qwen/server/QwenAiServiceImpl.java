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
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
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
        log.info("QwenAiServiceImpl 初始化完成，默认模型: {}, 默认温度: {}", DEFAULT_MODEL, DEFAULT_TEMPERATURE);
    }

    @Override
    public Result<String> chat(String question, String model, Double temperature) {
        long startTime = System.currentTimeMillis();
        String effectiveModel = getEffectiveModel(model);
        Double effectiveTemperature = getEffectiveTemperature(temperature);
        
        log.info("单轮对话请求 - question: {}, model: {}, temperature: {}", 
                truncate(question), effectiveModel, effectiveTemperature);
        
        try {
            String escapedQuestion = escapeJson(question);
            List<Message> messages = Collections.singletonList(Message.user(escapedQuestion));
            String response = sendRequest(messages, effectiveModel, effectiveTemperature);
            String answer = parseAnswer(response);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("单轮对话响应 - answer: {}, 耗时: {}ms", truncate(answer), duration);
            
            return Result.success(answer);
        } catch (java.net.SocketTimeoutException e) {
            log.error("单轮对话请求超时 - question: {}, model: {}, error: {}", 
                    truncate(question), effectiveModel, e.getMessage());
            return Result.fail("请求超时，请稍后重试");
        } catch (Exception e) {
            log.error("单轮对话异常 - question: {}, model: {}, error: {}", 
                    truncate(question), effectiveModel, e.getMessage(), e);
            return Result.fail("异常: " + e.getMessage());
        }
    }

    @Override
    public Result<String> chatWithContext(String question, String model, Double temperature) {
        long startTime = System.currentTimeMillis();
        String effectiveModel = getEffectiveModel(model);
        Double effectiveTemperature = getEffectiveTemperature(temperature);
        
        log.info("多轮对话请求 - question: {}, model: {}, temperature: {}, 历史消息数: {}",
                truncate(question), effectiveModel, effectiveTemperature, messageHistory.size());
        
        try {
            String escapedQuestion = escapeJson(question);
            messageHistory.add(Message.user(escapedQuestion));
            
            String response = sendRequest(new ArrayList<>(messageHistory), effectiveModel, effectiveTemperature);
            String answer = parseAnswer(response);
            messageHistory.add(Message.assistant(answer));
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("多轮对话响应 - answer: {}, 耗时: {}ms, 历史消息数: {}", 
                    truncate(answer), duration, messageHistory.size());
            
            return Result.success(answer);
        } catch (java.net.SocketTimeoutException e) {
            log.error("多轮对话请求超时 - question: {}, model: {}, 历史消息数: {}, error: {}", 
                    truncate(question), effectiveModel, messageHistory.size(), e.getMessage());
            return Result.fail("请求超时，请稍后重试");
        } catch (Exception e) {
            log.error("多轮对话异常 - question: {}, model: {}, 历史消息数: {}, error: {}", 
                    truncate(question), effectiveModel, messageHistory.size(), e.getMessage(), e);
            return Result.fail("异常: " + e.getMessage());
        }
    }

    @Override
    public Result<String> clearContext() {
        int historySize = messageHistory.size();
        messageHistory.clear();
        log.info("清空上下文 - 已清除 {} 条历史消息", historySize);
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
        
        log.debug("AI请求体 - {}", truncate(json, 1000));

        Request request = new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(json, JSON_MEDIA_TYPE))
                .addHeader("Authorization", "Bearer " + qwenConfig.getApiKey())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                log.error("API调用失败 - status: {}, errorBody: {}", response.code(), truncate(errorBody));
                throw new RuntimeException("API调用失败: " + response.code() + " -> " + truncate(errorBody));
            }
            String responseBody = response.body() != null ? response.body().string() : "";
            log.debug("AI响应体 - {}", truncate(responseBody, 2000));
            return responseBody;
        } catch (Exception e) {
            log.error("请求发送失败 - model: {}, error: {}", model, e.getMessage());
            throw new RuntimeException("请求发送失败: " + e.getMessage(), e);
        }
    }

    private String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\\", "\\\\");
    }

    private String parseAnswer(String response) throws IOException {
        JsonNode rootNode = objectMapper.readTree(response);
        JsonNode outputNode = rootNode.path("output");
        JsonNode textNode = outputNode.path("text");
        
        if (textNode.isTextual()) {
            return textNode.asText();
        }
        throw new RuntimeException("解析响应失败: " + truncate(response));
    }

    private String truncate(String str) {
        return truncate(str, 2000);
    }

    private String truncate(String str, int maxLength) {
        if (str == null) {
            return "null";
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...(truncated)";
    }
}