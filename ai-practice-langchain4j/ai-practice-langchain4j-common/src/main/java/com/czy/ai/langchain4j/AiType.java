package com.czy.ai.langchain4j;

import com.czy.ai.common.exception.BusinessException;
import com.czy.ai.common.exception.CommonErrorCodeEnum;

/**
 * AI 模型类型枚举
 *
 * @author chenzhenyu 2026年05月18日 下午22:01:43
 */
public enum AiType {
    WEBULL,
    QWEN;

    /**
     * 安全转换字符串为 AiType，无效值抛出业务异常
     *
     * @param name 类型名称
     * @return AiType
     */
    public static AiType safeValueOf(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException(CommonErrorCodeEnum.PARAM_ERROR.getCode(), "aiType 不能为空");
        }
        try {
            return AiType.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(CommonErrorCodeEnum.PARAM_ERROR.getCode(),
                    String.format("不支持的 AI 类型: %s，当前支持: %s", name, java.util.Arrays.toString(values())));
        }
    }
}
