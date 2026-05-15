package com.czy.qwen.util;

import com.czy.qwen.req.ChatRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * AI 参数工具类
 * 处理参数默认值、参数校验等
 *
 * @author chenzhenyu 2026年05月15日
 */
public final class AiParamUtils {

    public static final String DEFAULT_MODEL = "qwen-turbo";
    public static final Double DEFAULT_TEMPERATURE = 0.7;

    private AiParamUtils() {
    }

    public static String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    public static String getEffectiveSessionId(String sessionId) {
        return AiStringUtils.isNotBlank(sessionId) ? sessionId : generateSessionId();
    }

    public static String getEffectiveModel(String model) {
        return AiStringUtils.isNotBlank(model) ? model.trim() : DEFAULT_MODEL;
    }

    public static Double getEffectiveTemperature(Double temperature) {
        if (temperature == null) {
            return DEFAULT_TEMPERATURE;
        }
        return Math.max(0.0, Math.min(1.0, temperature));
    }

    public static EffectiveParams extractParams(ChatRequest request) {
        return EffectiveParams.builder()
                .sessionId(getEffectiveSessionId(request.getSessionId()))
                .model(getEffectiveModel(request.getModel()))
                .temperature(getEffectiveTemperature(request.getTemperature()))
                .systemPrompt(request.getSystemPrompt())
                .question(request.getQuestion())
                .build();
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EffectiveParams {
        String sessionId;
        String model;
        Double temperature;
        String systemPrompt;
        String question;
    }
}