package com.czy.ai.langchain4j.chatrequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author chenzhenyu 2026年05月19日 上午10:38:12
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LangChainChatRequest implements WebullChatRequest{

    private String sessionId;
    private String userId;
    private String systemPrompt;
    private String question;
    private String aiType;
    private String model;
    private Double temperature;
    private Double topP;
    private Integer maxTokens;

    // ai 特性参数
    private Boolean stream;
}
