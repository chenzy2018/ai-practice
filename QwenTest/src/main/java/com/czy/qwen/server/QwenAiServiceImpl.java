package com.czy.qwen.server;

import com.czy.qwen.config.QwenConfig;
import com.czy.qwen.facade.Message;
import com.czy.qwen.facade.QwenRequest;
import com.czy.qwen.req.ChatRequest;
import com.czy.qwen.resp.ChatResponse;
import com.czy.qwen.resp.Result;
import com.czy.qwen.resp.SessionInfo;
import com.czy.qwen.util.AiStringUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Qwen AI 服务
 *
 * @author chenzhenyu 2026年05月14日
 */
@Slf4j
@Service
public class QwenAiServiceImpl implements IQwenAiService {

    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final ZoneId HK_TIME_ZONE = ZoneId.of("Asia/Hong_Kong");
    private static final DateTimeFormatter HK_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    @Resource
    private QwenConfig qwenConfig;

    @Resource
    private SessionManager sessionManager;

    private final OkHttpClient client = createOkHttpClient();

    @Override
    public String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    @Override
    public Result<String> chat(String question, String model, Double temperature) {
        long startTime = System.currentTimeMillis();
        String effectiveModel = getEffectiveModel(model);
        Double effectiveTemperature = getEffectiveTemperature(temperature);

        log.info("单轮对话请求 - question: {}, model: {}, temperature: {}",
                AiStringUtils.truncate(question), effectiveModel, effectiveTemperature);

        try {
            List<Message> messages = Collections.singletonList(Message.user(AiStringUtils.escapeJson(question)));
            String response = sendRequest(messages, effectiveModel, effectiveTemperature);
            String answer = AiStringUtils.parseTextFromResponse(response);

            log.info("单轮对话响应 - answer: {}, 耗时: {}ms", AiStringUtils.truncate(answer), System.currentTimeMillis() - startTime);
            return Result.success(answer);
        } catch (IOException e) {
            log.error("单轮对话异常 - question: {}, error: {}", AiStringUtils.truncate(question), e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    @Override
    public Result<ChatResponse> chatWithContext(ChatRequest request) {
        long startTime = System.currentTimeMillis();
        String effectiveSessionId = getEffectiveSessionId(request.getSessionId());
        String effectiveModel = getEffectiveModel(request.getModel());
        Double effectiveTemperature = getEffectiveTemperature(request.getTemperature());

        SessionContext sessionContext = sessionManager.getOrCreate(effectiveSessionId, request.getSystemPrompt());
        sessionManager.updateLastActiveTime(effectiveSessionId);

        log.info("多轮对话请求 - sessionId: {}, question: {}, hasSystemPrompt: {}, model: {}, temperature: {}, 历史消息数: {}",
                effectiveSessionId, AiStringUtils.truncate(request.getQuestion()),
                AiStringUtils.isNotBlank(request.getSystemPrompt()),
                effectiveModel, effectiveTemperature, sessionContext.getMessageCount());

        try {
            sessionContext.addUserMessage(AiStringUtils.escapeJson(request.getQuestion()));
            String response = sendRequest(new ArrayList<>(sessionContext.getMessages()), effectiveModel, effectiveTemperature);
            String answer = AiStringUtils.parseTextFromResponse(response);
            sessionContext.addAssistantMessage(answer);

            log.info("多轮对话响应 - sessionId: {}, answer: {}, 耗时: {}ms, 历史消息数: {}",
                    effectiveSessionId, AiStringUtils.truncate(answer), System.currentTimeMillis() - startTime, sessionContext.getMessageCount());

            return Result.success(ChatResponse.builder()
                    .sessionId(effectiveSessionId)
                    .answer(answer)
                    .build());
        } catch (IOException e) {
            log.error("多轮对话异常 - sessionId: {}, error: {}", effectiveSessionId, e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    @Override
    public Result<String> clearContext(String sessionId) {
        if (AiStringUtils.isBlank(sessionId)) {
            return Result.fail("sessionId不能为空");
        }

        SessionContext removed = sessionManager.remove(sessionId);
        int messageCount = removed != null ? removed.getMessageCount() : 0;

        log.info("清空会话上下文 - sessionId: {}, 清除消息数: {}", sessionId, messageCount);
        return Result.success("会话 " + sessionId + " 已清空，共 " + messageCount + " 条消息");
    }

    @Override
    public Result<List<SessionInfo>> listSessions() {
        List<SessionInfo> sessionInfos = sessionManager.getAllSessions().entrySet().stream()
                .map(entry -> {
                    SessionContext context = entry.getValue();
                    return SessionInfo.builder()
                            .sessionId(context.getSessionId())
                            .messageCount(context.getMessageCount())
                            .lastActiveTime(formatTimeToHongKong(context.getLastActiveTime()))
                            .hasSystemPrompt(AiStringUtils.isNotBlank(context.getSystemPrompt()))
                            .build();
                })
                .sorted((a, b) -> b.getLastActiveTime().compareTo(a.getLastActiveTime()))
                .collect(Collectors.toList());

        log.info("查询会话列表 - 活跃会话数: {}", sessionInfos.size());
        return Result.success(sessionInfos);
    }

    @Override
    public Result<String> removeSession(String sessionId) {
        if (AiStringUtils.isBlank(sessionId)) {
            return Result.fail("sessionId不能为空");
        }

        SessionContext removed = sessionManager.remove(sessionId);
        if (removed != null) {
            log.info("删除会话 - sessionId: {}, 消息数: {}", sessionId, removed.getMessageCount());
            return Result.success("会话 " + sessionId + " 已删除");
        }
        return Result.fail("会话 " + sessionId + " 不存在");
    }

    @Override
    public Result<Map<String, Object>> getSessionInfo(String sessionId) {
        if (AiStringUtils.isBlank(sessionId)) {
            return Result.fail("sessionId不能为空");
        }

        SessionContext context = sessionManager.get(sessionId);
        if (context == null) {
            return Result.fail("会话 " + sessionId + " 不存在");
        }

        Map<String, Object> info = new HashMap<>();
        info.put("sessionId", context.getSessionId());
        info.put("messageCount", context.getMessageCount());
        info.put("lastActiveTime", formatTimeToHongKong(context.getLastActiveTime()));
        info.put("hasSystemPrompt", AiStringUtils.isNotBlank(context.getSystemPrompt()));

        return Result.success(info);
    }

    private OkHttpClient createOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    private String sendRequest(List<Message> messages, String model, Double temperature) throws IOException {
        QwenRequest requestBody = QwenRequest.builder()
                .model(model)
                .input(QwenRequest.Input.builder().messages(messages).build())
                .parameters(QwenRequest.Parameters.builder().temperature(temperature).build())
                .build();
        String json = AiStringUtils.toJson(requestBody);

        log.debug("AI请求体 - {}", AiStringUtils.truncate(json, 1000));

        Request request = new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(json, JSON_MEDIA_TYPE))
                .addHeader("Authorization", "Bearer " + qwenConfig.getApiKey())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                log.error("API调用失败 - status: {}, errorBody: {}", response.code(), AiStringUtils.truncate(errorBody));
                throw new IOException("API调用失败: " + response.code());
            }
            String responseBody = response.body() != null ? response.body().string() : "";
            log.debug("AI响应体 - {}", AiStringUtils.truncate(responseBody, 2000));
            return responseBody;
        }
    }

    private String formatTimeToHongKong(long timestamp) {
        return Instant.ofEpochMilli(timestamp)
                .atZone(HK_TIME_ZONE)
                .format(HK_FORMATTER);
    }

    private String getEffectiveSessionId(String sessionId) {
        return AiStringUtils.isNotBlank(sessionId) ? sessionId : generateSessionId();
    }

    private String getEffectiveModel(String model) {
        return AiStringUtils.isNotBlank(model) ? model.trim() : DEFAULT_MODEL;
    }

    private Double getEffectiveTemperature(Double temperature) {
        if (temperature == null) {
            return DEFAULT_TEMPERATURE;
        }
        return Math.max(0.0, Math.min(1.0, temperature));
    }
}