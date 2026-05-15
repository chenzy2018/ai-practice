package com.czy.qwen.facade;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 通义千问请求 DTO
 *
 * @author chenzhenyu 2026年05月14日
 */
@Getter
@Setter
@Builder
public class QwenRequest {

    private String model;
    private Input input;
    private Parameters parameters;

    @Getter
    @Setter
    @Builder
    public static class Input {
        private List<Message> messages;
    }

    @Getter
    @Setter
    @Builder
    public static class Parameters {
        private Double temperature;
        @Builder.Default
        private Double top_p = 0.8;
        @Builder.Default
        private Integer max_tokens = 1024;
        private Boolean stream;
    }
}