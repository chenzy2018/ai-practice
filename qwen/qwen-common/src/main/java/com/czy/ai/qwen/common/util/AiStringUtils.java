package com.czy.ai.qwen.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;


import java.io.IOException;

/**
 * 字符串工具类
 *
 * @author nober 2026年05月14日
 */
public final class AiStringUtils extends StringUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int DEFAULT_TRUNCATE_LENGTH = 200;

    private AiStringUtils() {
    }

    public static String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ");
    }

    public static String truncate(String str) {
        return truncate(str, DEFAULT_TRUNCATE_LENGTH);
    }

    public static String truncate(String str, int maxLength) {
        if (str == null) {
            return "null";
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...(truncated)";
    }

    public static String parseTextFromResponse(String response) throws IOException {
        JsonNode rootNode = OBJECT_MAPPER.readTree(response);
        JsonNode outputNode = rootNode.path("output");
        JsonNode textNode = outputNode.path("text");

        if (textNode.isTextual()) {
            return textNode.asText();
        }
        throw new IOException("解析响应失败: " + truncate(response));
    }

    public static String toJson(Object obj) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(obj);
    }
}