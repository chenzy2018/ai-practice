package com.czy.ai.qwen.service.impl;

import com.czy.ai.common.Message;
import com.czy.ai.common.dto.Result;
import com.czy.ai.qwen.common.dto.ChatRequest;
import com.czy.ai.qwen.common.dto.ChatResponse;
import com.czy.ai.qwen.common.dto.SessionInfo;
import com.czy.ai.qwen.common.util.AiParamUtils;
import com.czy.ai.qwen.common.util.AiStringUtils;
import com.czy.ai.common.session.SessionContext;
import com.czy.ai.qwen.common.outclient.QwenApiClient;
import com.czy.ai.qwen.service.IAiService;
import com.czy.ai.common.session.SessionManage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiServiceImpl implements IAiService {

    private static final ZoneId HK_TIME_ZONE = ZoneId.of("Asia/Hong_Kong");
    private static final DateTimeFormatter HK_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    @Resource
    private SessionManage sessionManage;

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

        SessionContext session = sessionManage.getOrCreate(params.getSessionId(), params.getUserId(), params.getSystemPrompt());
        sessionManage.updateLastActiveTime(params.getSessionId());

        log.info("多轮对话请求 - sessionId: {}, userId: {}, question: {}, model: {}, temperature: {}",
                params.getSessionId(), params.getUserId(), AiStringUtils.truncate(params.getQuestion()),
                params.getModel(), params.getTemperature());

        try {
            session.addUserMessage(AiStringUtils.escapeJson(params.getQuestion()));
            String response = qwenApiClient.sendRequest(new ArrayList<>(session.getMessages()), params.getModel(), params.getTemperature());
            String answer = AiStringUtils.parseTextFromResponse(response);
            session.addAssistantMessage(answer);

            log.info("多轮对话响应 - sessionId: {}, answer: {}, 耗时: {}ms",
                    params.getSessionId(), AiStringUtils.truncate(answer), System.currentTimeMillis() - startTime);

            return Result.success(ChatResponse.builder()
                    .sessionId(params.getSessionId())
                    .userId(params.getUserId())
                    .answer(answer)
                    .build());
        } catch (IOException e) {
            log.error("多轮对话异常 - sessionId: {}, error: {}", params.getSessionId(), e, e);
            return Result.fail(e.getMessage());
        }
    }

    @Override
    public Result<String> clearContext(String sessionId) {
        if (AiStringUtils.isBlank(sessionId)) {
            return Result.fail("sessionId不能为空");
        }

        SessionContext removed = sessionManage.remove(sessionId);
        int messageCount = removed != null ? removed.getMessageCount() : 0;

        log.info("清空会话上下文 - sessionId: {}, 清除消息数: {}", sessionId, messageCount);
        return Result.success("会话 " + sessionId + " 已清空，共 " + messageCount + " 条消息");
    }

    @Override
    public Result<Map<String, List<SessionInfo>>> listSessions() {
        Map<String, List<SessionInfo>> groupedSessions = sessionManage.getAllSessions().entrySet().stream()
                .map(entry -> {
                    SessionContext session = entry.getValue();
                    return SessionInfo.builder()
                            .sessionId(session.getSessionId())
                            .userId(session.getUserId())
                            .messageCount(session.getMessageCount())
                            .lastActiveTime(formatTimeToHongKong(session.getLastActiveTime()))
                            .hasSystemPrompt(AiStringUtils.isNotBlank(session.getSystemPrompt()))
                            .build();
                })
                .collect(Collectors.groupingBy(SessionInfo::getUserId));

        int totalSessions = groupedSessions.values().stream().mapToInt(List::size).sum();
        log.info("查询会话列表 - 活跃会话数: {}, 用户数: {}", totalSessions, groupedSessions.size());
        return Result.success(groupedSessions);
    }

    @Override
    public Result<String> removeSession(String sessionId) {
        if (AiStringUtils.isBlank(sessionId)) {
            return Result.fail("sessionId不能为空");
        }

        SessionContext removed = sessionManage.remove(sessionId);
        if (removed != null) {
            log.info("删除会话 - sessionId: {}, userId: {}, 消息数: {}", sessionId, removed.getUserId(), removed.getMessageCount());
            return Result.success("会话 " + sessionId + " 已删除");
        }
        return Result.fail("会话 " + sessionId + " 不存在");
    }

    @Override
    public Result<Map<String, Object>> getSessionInfo(String sessionId) {
        if (AiStringUtils.isBlank(sessionId)) {
            return Result.fail("sessionId不能为空");
        }

        SessionContext session = sessionManage.get(sessionId);
        if (session == null) {
            return Result.fail("会话 " + sessionId + " 不存在");
        }

        Map<String, Object> info = new HashMap<>();
        info.put("sessionId", session.getSessionId());
        info.put("userId", session.getUserId());
        info.put("messageCount", session.getMessageCount());
        info.put("lastActiveTime", formatTimeToHongKong(session.getLastActiveTime()));
        info.put("hasSystemPrompt", AiStringUtils.isNotBlank(session.getSystemPrompt()));

        return Result.success(info);
    }

    private String formatTimeToHongKong(long timestamp) {
        return Instant.ofEpochMilli(timestamp)
                .atZone(HK_TIME_ZONE)
                .format(HK_FORMATTER);
    }
}
