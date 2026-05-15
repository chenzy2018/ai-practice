package com.czy.qwen.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 会话信息
 *
 * @author chenzhenyu 2026年05月14日
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionInfo {
    private String sessionId;
    private String userId;
    private Integer messageCount;
    private String lastActiveTime;
    private Boolean hasSystemPrompt;
}