package com.czy.qwen.server;

import com.czy.qwen.resp.Result;

/**
 * Qwen AI 服务接口
 *
 * @author chenzhenyu 2026年05月14日
 */
public interface IQwenAiService {

    String DEFAULT_MODEL = "qwen-turbo";
    Double DEFAULT_TEMPERATURE = 0.7;

    /**
     * 单轮对话（自定义参数）
     *
     * @param question    用户问题
     * @param model       模型名称，默认 "qwen-turbo"
     * @param temperature 温度，控制随机性/创意，范围 0~1，默认 0.7
     * @return 回答结果
     */
    Result<String> chat(String question, String model, Double temperature);

    /**
     * 多轮对话（带上下文，自定义参数）
     *
     * @param question    用户问题
     * @param model       模型名称，默认 "qwen-turbo"
     * @param temperature 温度，控制随机性/创意，范围 0~1，默认 0.7
     * @return 回答结果
     */
    Result<String> chatWithContext(String question, String model, Double temperature);

    /**
     * 清空对话上下文
     *
     * @return 操作结果
     */
    Result<String> clearContext();
}