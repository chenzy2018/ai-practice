# Qwen接口说明
## 请求体
- model：模型名称，用哪个版本的 AI（qwen-turbo 免费最快）
- input：输入内容，你给 AI 的内容
    - messages：对话历史（上下文）
- parameters：AI 的 “性格设置”，用于控制控制AI行为
    - temperature：温度，控制随机性/创意
    - top_p：控制多样性，控制 AI 词汇选择范围，默认 0.8，一般不懂
    - max_tokens：控制回答长度
```json
{
  "model": "qwen-turbo",   
  "input": {                  
    "messages": [
      { "role": "user", "content": "你的问题" }
    ]
  },
  "parameters": {                
    "temperature": 0.7,
    "top_p": 0.8,
    "max_tokens": 1024
  }
}
```

### module
1. qwen3-max / qwen-max（最强旗舰）
2. qwen3-plus / qwen-plus（均衡旗舰）
3. qwen-turbo（高速通用，你现在用的）
4. qwen-flash（轻量极速）
5. qwen3-coder-plus（代码专用）
6. qwen3-vl-plus（图像理解）

通用文本模型

| 模型名称 | 综合能力 | 响应速度 | 计费成本 | 上下文窗口 | 适用场景 |
| ---- | ---- | ---- | ---- | ---- | ---- |
| qwen3-max / qwen-max | 旗舰最强 | 较慢 | 最高 | 128k | 复杂逻辑推理、专业报告、深度分析、公文创作 |
| qwen3-plus / qwen-plus | 全能均衡 | 中等 | 中高 | 128k | 长文本创作、办公文案、多轮深度对话、专业咨询 |
| qwen-turbo | 日常通用 | 较快 | 低价 | 32k | 普通聊天、业务客服、简单问答、日常接口调用 |
| qwen-flash | 基础够用 | 极快 | 极低 | 32k | 高并发高频闲聊、固定简单问答、节约Token成本 |

专项领域模型

| 模型名称 | 核心特长 | 响应速度 | 适用场景 |
| ---- | ---- | ---- | ---- |
| qwen3-coder-plus | 代码生成、Bug调试、架构设计 | 中等 | Java/Python/Go编码、代码优化、技术排错答疑 |
| qwen3-vl-plus | 图片识别、截图解析、图文理解 | 中等 | OCR文字提取、截图分析、PDF图文解读 |


### input
#### message
messages 支持三种角色：
- system：全局人设、规则、约束（永久生效）
- user：用户提问
- assistant：AI 回复

**system**

给AI一个全局人设、规则、约束（永久生效），可以随意设置，也可以不设置，即通用 AI 助手
- 约束回答风格：简洁、专业、口语、公文
- 学会强制固定格式输出：适合前后端对接，比如要求按照一定的json格式，封装返回结果

eg：
```java
private static final String SYS_ROLE_JAVA_EXPERT =
          "你是资深Java后端架构师，回答简洁专业，只讲重点，代码规范易懂，不啰嗦，不用多余客套话。";

private static final String SYS_ROLE_CHAT =
        "你是亲切友好的聊天助手，语气轻松自然，口语化回答。";

private static final String SYS_ROLE_COPYWRITER =
        "你是专业文案创作助手，文笔优美，条理清晰，适合写朋友圈、文案、短句。";

// 约定返回格式
private static final String SYS_JSON_MODE =
        "返回结果严格遵守规则：\n" +
                "1.只返回标准JSON，不要任何多余解释、前言、后语\n" +
                "2.不要用```代码块包裹，不换行，只输出单行JSON\n" +
                "3.固定返回结构：{\"code\":200,\"msg\":\"success\",\"answer\":\"回答内容\"}\n" +
                "4.把问题答案填入answer字段即可";
```

只要会话第一条固定放 system，全程人设不变。

### temperature
范围：0 ~ 1
- 0.1：非常严谨、死板、固定答案
- 0.7：平衡（推荐）
- 1.0：放飞自我、创意强、可能胡说

业务场景
- 写代码 / 查知识 → 用 0.1 ~ 0.3
- 聊天 / 创意 / 文案 → 用 0.7 ~ 0.9

### max_tokens
* 数字越大，回答越长
* 数字越小，回答越短
* 免费额度要节省，就设小一点

示例
* 50 → 很短
* 512 → 正常
* 1024 → 很长


## 返回体
* output：返回内容
    * text：真正的回答
    * finish_reason：stop = 正常结束
* usage：计费 / 额度消耗
    * input_tokens：你用了多少token
    * output_tokens：AI回答用了多少
```json
{
  "output": {
    "finish_reason": "stop",
    "text": "AI回答内容"
  },
  "usage": {
    "input_tokens": 5, 
    "output_tokens": 20   
  }
}
```