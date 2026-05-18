package com.czy.ai.qwen.common.enums;

/**
 * Qwen AI 实现提供者枚举
 * 
 * @author nober
 */
public enum QwenAiProvider {

    /**
     * 原生API实现
     */
    QWEN_NATIVE("qwen-native"),

    /**
     * LangChain4j实现
     */
    QWEN_LANGCHAIN4J("qwen-langchain4j");

    private final String code;

    QwenAiProvider(String code) {
        this.code = code;
    }

    /**
     * 获取提供者编码
     *
     * @return 提供者编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 根据编码获取提供者枚举
     *
     * @param code 提供者编码
     * @return 提供者枚举，默认返回 QWEN_NATIVE
     */
    public static QwenAiProvider fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return QWEN_NATIVE;
        }
        for (QwenAiProvider provider : values()) {
            if (provider.code.equalsIgnoreCase(code)) {
                return provider;
            }
        }
        return QWEN_NATIVE;
    }

    /**
     * 判断是否为原生实现
     *
     * @return true 表示原生实现
     */
    public boolean isNative() {
        return this == QWEN_NATIVE;
    }

    /**
     * 判断是否为 LangChain4j 实现
     *
     * @return true 表示 LangChain4j 实现
     */
    public boolean isLangChain4j() {
        return this == QWEN_LANGCHAIN4J;
    }
}
