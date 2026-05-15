package com.czy.qwen.server;

import com.czy.qwen.facade.Message;
import com.czy.qwen.facade.QwenApiClient;
import com.czy.qwen.req.ChatRequest;
import com.czy.qwen.resp.ChatResponse;
import com.czy.qwen.resp.Result;
import com.czy.qwen.resp.SessionInfo;
import com.czy.qwen.util.AiParamUtils;
import com.czy.qwen.util.AiStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Qwen AI 服务
 *
 * @author chenzhenyu 2026年05月14日
 */
@Slf4j
@Service
public class QwenAiServiceImpl implements IQwenAiService {

    private static final ZoneId HK_TIME_ZONE = ZoneId.of("Asia/Hong_Kong");
    private static final DateTimeFormatter HK_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    @Resource
    private SessionManager sessionManager;

    @Resource
    private QwenApiClient qwenApiClient;

    @Override
    public String generateSessionId() {
        return AiParamUtils.generateSessionId();
    }

    @Override
    public Result<String> chat(String question, String model, Double temperature) {
        long startTime = System.currentTimeMillis();
        String effectiveModel = AiParamUtils.getEffectiveModel(model);
        Double effectiveTemperature = AiParamUtils.getEffectiveTemperature(temperature);

        log.info("单轮对话请求 - question: {}, model: {}, temperature: {}",
                AiStringUtils.truncate(question), effectiveModel, effectiveTemperature);

        try {
            List<Message> messages = Collections.singletonList(Message.user(AiStringUtils.escapeJson(question)));
            String response = qwenApiClient.sendRequest(messages, effectiveModel, effectiveTemperature);
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
        AiParamUtils.EffectiveParams params = AiParamUtils.extractParams(request);

        SessionContext sessionContext = sessionManager.getOrCreate(params.getSessionId(), params.getSystemPrompt());
        sessionManager.updateLastActiveTime(params.getSessionId());

        log.info("多轮对话请求 - sessionId: {}, question: {}, hasSystemPrompt: {}, model: {}, temperature: {}, 历史消息数: {}",
                params.getSessionId(), AiStringUtils.truncate(params.getQuestion()),
                AiStringUtils.isNotBlank(params.getSystemPrompt()),
                params.getModel(), params.getTemperature(), sessionContext.getMessageCount());

        try {
            sessionContext.addUserMessage(AiStringUtils.escapeJson(params.getQuestion()));
            String response = qwenApiClient.sendRequest(new ArrayList<>(sessionContext.getMessages()), params.getModel(), params.getTemperature());
            String answer = AiStringUtils.parseTextFromResponse(response);
            sessionContext.addAssistantMessage(answer);

            log.info("多轮对话响应 - sessionId: {}, answer: {}, 耗时: {}ms, 历史消息数: {}",
                    params.getSessionId(), AiStringUtils.truncate(answer), System.currentTimeMillis() - startTime, sessionContext.getMessageCount());

            return Result.success(ChatResponse.builder()
                    .sessionId(params.getSessionId())
                    .answer(answer)
                    .build());
        } catch (IOException e) {
            log.error("多轮对话异常 - sessionId: {}, error: {}", params.getSessionId(), e.getMessage());
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

    private String formatTimeToHongKong(long timestamp) {
        return Instant.ofEpochMilli(timestamp)
                .atZone(HK_TIME_ZONE)
                .format(HK_FORMATTER);
    }
}