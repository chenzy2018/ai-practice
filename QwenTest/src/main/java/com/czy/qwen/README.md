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
