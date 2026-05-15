package com.czy.ai.qwen.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamResponse {
    private String sessionId;
    private String content;
    private Boolean finished;
    private String error;
}