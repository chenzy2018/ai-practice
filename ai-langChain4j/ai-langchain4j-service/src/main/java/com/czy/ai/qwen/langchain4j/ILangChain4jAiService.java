package com.czy.ai.qwen.langchain4j;

import com.czy.ai.common.dto.Result;

/**
 * @author chenzhenyu 2026年05月18日 下午21:22:50
 */
public interface ILangChain4jAiService {

    Result<String> chat(String content);
}
