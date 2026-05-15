package com.czy.qwen.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 流式响应对象
 *
 * @author chenzhenyu 2026年05月15日
 */
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