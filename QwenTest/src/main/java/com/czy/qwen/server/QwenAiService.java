package com.czy.qwen.server;

import com.czy.qwen.config.QwenConfig;
import com.czy.qwen.resp.Result;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author chenzhenyu 2026年05月14日 上午10:51:55
 */
@Service
public class QwenAiService {

    @Resource
    private QwenConfig qwenConfig;

    private final OkHttpClient client = new OkHttpClient();

    public Result<String> chat(String question) {
        try {
            // 通义千问官方正确请求格式
            String json = "{"
                    + "\"model\":\"qwen-turbo\","
                    + "\"input\":{"
                    + "\"messages\":[{\"role\":\"user\",\"content\":\"" + escape(question) + "\"}]"
                    + "},"
                    + "\"parameters\":{\"temperature\":0.7}"
                    + "}";

            Request request = new Request.Builder()
                    .url("https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation")
                    .post(RequestBody.create(json, MediaType.get("application/json; charset=utf-8")))
                    .addHeader("Authorization", "Bearer " + qwenConfig.getApiKey())
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String resp = response.body().string();
                System.out.println("通义千问原始返回：" + resp);

                if (!response.isSuccessful()) {
                    return Result.fail("调用失败：" + response.code());
                }

                // 正确解析 text 字段！！！
                String ans = parseText(resp);
                return Result.success(ans);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("异常：" + e.getMessage());
        }
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }

    // 正确解析：从 output -> text 读取回答
    private String parseText(String resp) {
        try {
            int textIndex = resp.indexOf("\"text\"");
            int start = resp.indexOf("\"", textIndex + 7) + 1;
            int end = resp.indexOf("\"", start);
            return resp.substring(start, end);
        } catch (Exception e) {
            return "解析错误";
        }
    }
}