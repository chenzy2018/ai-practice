# 为什么选择LangChain4J？
* Java原生支持：强类型注解驱动开发，无缝整合Spring生态
* 企业级特性：生产验证的RAG流程、多数据库向量存储支持
* 极简代码：10行实现法律顾问、数学助手等专业AI应用
* 版本稳定：1.0+版本API冻结，适合长期项目

# LangChain4j 框架核心组件详解

## 消息相关组件
作用：统一封装对话角色与文本内容，规范交互格式
* ChatMessage：聊天消息顶层父类
* UserMessage：用户提问消息
* AiMessage：模型回复消息
* SystemMessage：系统人设指令消息

## 模型顶层接口

### ChatModel（1.0.1 新命名，原 ChatLanguageModel）
通用对话模型标准接口
* 所有大模型、自定义私有模型均实现该接口
* 统一 chat() 调用方法，实现底层模型无感切换

### StreamingChatModel（1.0.1 新命名，原 StreamingChatLanguageModel）
通用流式对话标准接口
* 所有大模型、自定义私有模型均实现该接口
* 统一 chat() 调用方法，实现底层模型无感切换

## 角色定义 @SystemMessage
控制AI的行为边界，实现领域专注
```java
@AiService  
public interface LegalAssistant {  
    @SystemMessage("你是一位中国法律顾问，仅回答法律相关问题。非法律问题回复：'抱歉，我只能回答法律问题。'")  
    @UserMessage("解答：{{question}}")  
    String answer(@V("question") String question);  
}
```

## 函数调用 @Tool
赋予大模型调用外部方法的能力
```java
@Component  
public class CalculatorTools {  
    @Tool("计算两个数的和")  
    double add(  
        @ToolMemoryId int userId,  // 会话隔离  
        @P("加数1") double a,  
        @P("加数2") double b  
    ) {  
        return a + b;  
    }  
}  

// 服务层集成工具  
@AiService(tools = "calculatorTools")  
public interface MathAssistant {  
    String chat(@MemoryId int userId, @UserMessage String msg);  
}
```

## 结构化输出 @Description
从文本中精准提取POJO对象字段
```java
public class Recipe {  
    @Description("简短标题，最多3个词")  
    private String title;  

    @Description("烹饪步骤列表，每步不超过10字")  
    private List<String> steps;  
}  

interface RecipeExtractor {  
    @UserMessage("从文本提取菜谱：{{it}}")  
    Recipe extract(String text);  
}
```

优势：解决大模型自由文本输出难以解析的问题

---

# LangChain4j 0.32.0 → 1.0.1 升级变更记录

## 一、版本与依赖

| 项目 | 0.32.0 | 1.0.1 |
|------|--------|-------|
| langchain4j.version | 0.32.0 | 1.0.1 |
| langchain4j-bom | 无 | 新增引入，统一版本管理 |

## 二、接口与类重命名

| 0.32.0 | 1.0.1 | 说明 |
|--------|-------|------|
| `ChatLanguageModel` | `ChatModel` | 同步对话模型接口重命名 |
| `StreamingChatLanguageModel` | `StreamingChatModel` | 流式对话模型接口重命名 |
| `ChatLanguageModelFactory` | `ChatModelFactory` | 本项目工厂类同步重命名 |
| `StreamingResponseHandler<AiMessage>` | `StreamingChatResponseHandler` | 流式回调处理器重命名，泛型参数移除 |
| `Response<AiMessage>` | `ChatResponse` | 响应类型替换，不再包装 AiMessage |

## 三、方法签名变更

| 0.32.0 | 1.0.1 | 说明 |
|--------|-------|------|
| `model.generate(String)` | `model.chat(String)` | 同步调用方法名变更 |
| `model.generate(List<ChatMessage>)` | `model.chat(List<ChatMessage>)` | 同步调用方法名变更 |
| `model.generate(messages, handler)` | `model.chat(messages, handler)` | 流式调用方法名变更 |
| `handler.onNext(String token)` | `handler.onPartialResponse(String partialResponse)` | 流式 token 回调方法重命名 |
| `handler.onComplete(Response<AiMessage>)` | `handler.onCompleteResponse(ChatResponse)` | 流式完成回调方法重命名 |
| `response.content().text()` | `response.aiMessage().text()` | 响应内容提取方式变更 |

## 四、包路径变更

| 类名 | 0.32.0 包路径 | 1.0.1 包路径 |
|------|-------------|-------------|
| `ChatResponse` | 不存在（原为 `Response<AiMessage>`） | `dev.langchain4j.model.chat.response.ChatResponse` |
| `StreamingChatResponseHandler` | 不存在（原为 `StreamingResponseHandler`） | `dev.langchain4j.model.chat.response.StreamingChatResponseHandler` |
| `ChatModel` | `dev.langchain4j.model.chat.ChatLanguageModel` | `dev.langchain4j.model.chat.ChatModel` |
| `StreamingChatModel` | `dev.langchain4j.model.chat.StreamingChatLanguageModel` | `dev.langchain4j.model.chat.StreamingChatModel` |
| `ChatMessage` | `dev.langchain4j.data.message.ChatMessage` | `dev.langchain4j.data.message.ChatMessage`（不变） |
| `UserMessage` | `dev.langchain4j.data.message.UserMessage` | `dev.langchain4j.data.message.UserMessage`（不变） |
| `AiMessage` | `dev.langchain4j.data.message.AiMessage` | `dev.langchain4j.data.message.AiMessage`（不变） |
| `SystemMessage` | `dev.langchain4j.data.message.SystemMessage` | `dev.langchain4j.data.message.SystemMessage`（不变） |
| `OpenAiChatModel` | `dev.langchain4j.model.openai.OpenAiChatModel` | `dev.langchain4j.model.openai.OpenAiChatModel`（不变） |
| `OpenAiStreamingChatModel` | `dev.langchain4j.model.openai.OpenAiStreamingChatModel` | `dev.langchain4j.model.openai.OpenAiStreamingChatModel`（不变） |

## 五、本项目受影响文件清单

| 文件 | 变更内容 |
|------|----------|
| `pom.xml`（父POM） | 版本 0.32.0→1.0.1，新增 langchain4j-bom 依赖管理 |
| `ChatModelFactory.java` | 新建（原 ChatLanguageModelFactory 删除），内部 ChatLanguageModel→ChatModel，StreamingChatLanguageModel→StreamingChatModel |
| `DynamicModelFactory.java` | 所有类型引用 ChatLanguageModel→ChatModel，StreamingChatLanguageModel→StreamingChatModel |
| `AbstractAiChatProvider.java` | `.generate()`→`.chat()`，`Response<AiMessage>`→`ChatResponse`，`.content().text()`→`.aiMessage().text()` |
| `QwenRegister.java` | 引用 ChatLanguageModelFactory→ChatModelFactory，注册方法名更新 |
| `WebullRegister.java` | 引用 ChatLanguageModelFactory→ChatModelFactory，注册方法名更新 |
| `ChatServiceImpl.java` | StreamingResponseHandler→StreamingChatResponseHandler，onNext→onPartialResponse，onComplete→onCompleteResponse，import 路径更新 |
| `AiChatProviderFactory.java` | Maps.newHashMap()→ConcurrentHashMap（线程安全优化，非 1.0.1 必须） |

## 六、1.0.1 新增 StreamingChatResponseHandler 完整接口

```java
public interface StreamingChatResponseHandler {
    void onPartialResponse(String partialResponse);
    default void onPartialThinking(PartialThinking partialThinking) {}
    default void onPartialToolCall(PartialToolCall partialToolCall) {}
    default void onCompleteToolCall(CompleteToolCall completeToolCall) {}
    void onCompleteResponse(ChatResponse completeResponse);
    void onError(Throwable error);
}
```

相比 0.32.0 的 `StreamingResponseHandler<AiMessage>`：
- 移除泛型参数 `<AiMessage>`
- `onNext` → `onPartialResponse`（语义更明确）
- `onComplete` → `onCompleteResponse`（与 onPartialResponse 对称）
- 新增 `onPartialThinking`（支持思维链推理模型，如 DeepSeek-R1）
- 新增 `onPartialToolCall` / `onCompleteToolCall`（支持流式工具调用）

## 七、ChatMemory 原生对话记忆集成

### 背景与动机

LangChain4j 1.0.1 提供原生 `ChatMemory` 能力，替代了本项目的自定义会话管理方案：

| 对比维度 | 自定义方案（已替换） | ChatMemory 原生方案 |
|----------|---------------------|-------------------|
| 消息存储 | `Message` DTO + `CopyOnWriteArrayList` | `ChatMemory` 直接存储 `ChatMessage` |
| 消息转换 | `MessageConverter` 手动转换 Message↔ChatMessage | 无需转换，直接使用 ChatMessage |
| SystemMessage | 手动初始化 + trimMessages 保护逻辑 | ChatMemory 自动处理：唯一、保留、去重 |
| 消息淘汰 | `SessionContext.trimMessages()` 手动裁剪 | `MessageWindowChatMemory` 滑动窗口自动淘汰 |
| 持久化 | 无 | `ChatMemoryStore` 接口可插拔（Redis/DB） |
| 工具消息 | 未处理 | 自动淘汰孤立 ToolExecutionResultMessage |

### 架构变更

```
重构前：
  ChatRequest → SessionManage → SessionContext(Message) → MessageConverter → ChatMessage → Model

重构后：
  ChatRequest → ChatMemoryManager → ChatMemory(ChatMessage) → Model
```

### 涉及文件

| 文件 | 操作 | 说明 |
|------|------|------|
| `ChatMemoryManager.java` | 新增 | 替代 SessionManage，管理 ChatMemory 实例 + 过期清理 |
| `AbstractAiChatProvider.java` | 修改 | SessionManage + MessageConverter → ChatMemoryManager + ChatMemory |
| `ChatServiceImpl.java` | 修改 | SessionManage + MessageConverter → ChatMemoryManager + ChatMemory |
| `MessageConverter.java` | 删除 | ChatMemory 直接存储 ChatMessage，无需转换 |
| `SessionManage.java` | 保留 | qwen 模块仍在使用 |
| `SessionContext.java` | 保留 | qwen 模块仍在使用 |
| `Message.java` | 保留 | qwen 模块仍在使用 |

### 核心代码示例

```java
// 创建 ChatMemory（自动处理 SystemMessage）
ChatMemory chatMemory = chatMemoryManager.getOrCreate(sessionId, userId, systemPrompt);

// 添加用户消息
chatMemory.add(UserMessage.from(question));

// 使用 ChatMemory 中的所有消息调用模型
ChatResponse response = model.chat(chatMemory.messages());

// 将 AI 响应加入 ChatMemory（自动处理淘汰）
chatMemory.add(response.aiMessage());
```

### ChatMemory 扩展能力

- **MessageWindowChatMemory**：按消息数量滑动窗口（当前使用，maxMessages 由 `ai.session.max-messages` 配置）
- **TokenWindowChatMemory**：按 Token 数量滑动窗口（更精确，需 Tokenizer）
- **ChatMemoryStore**：持久化接口，可接入 Redis/DB，实现会话跨重启恢复

```java
// 未来持久化扩展示例
ChatMemory chatMemory = MessageWindowChatMemory.builder()
    .id(sessionId)
    .maxMessages(sessionConfig.getMaxMessages())
    .chatMemoryStore(new RedisChatMemoryStore())  // 可插拔
    .build();
```

## 八、AiServices + ChatMemoryProvider + @MemoryId 高阶 API 集成

### 四大核心概念

| 概念 | 1.0.1 类/注解 | 作用 |
|------|--------------|------|
| ChatMemory | `dev.langchain4j.memory.ChatMemory` | 对话记忆容器，自动淘汰 + SystemMessage 保护 |
| @MemoryId | `dev.langchain4j.service.MemoryId` | 方法参数注解，标识会话 ID，AiServices 自动路由到对应 ChatMemory |
| ChatMemoryProvider | `dev.langchain4j.memory.chat.ChatMemoryProvider` | 多用户记忆工厂，按 memoryId 创建/获取 ChatMemory |
| AiServices | `dev.langchain4j.service.AiServices` | 声明式 AI 服务，自动编排记忆 + 模型调用 + 消息管理 |

### 高阶 vs 低阶 API 选择

| 场景 | API 选择 | 原因 |
|------|---------|------|
| 简单对话（默认模型+记忆） | **AiAssistant（高阶）** | AiServices 自动编排，一行代码搞定 |
| 自定义对话（动态模型参数） | **ChatMemory + ChatModel（低阶）** | 需动态创建模型，AiServices 固定绑定不支持 |
| 流式对话（SSE） | **ChatMemory + StreamingChatModel（低阶）** | SSE 需要 SseEmitter 精确控制，AiServices 不支持 |

### AiAssistant 接口定义

```java
public interface AiAssistant {
    /**
     * 带记忆的对话
     * @MemoryId → ChatMemoryProvider.get(memoryId) → 获取/创建 ChatMemory
     * @UserMessage → 自动添加 UserMessage 到 ChatMemory
     * AiServices 自动：调用 ChatModel → 添加 AiMessage 到 ChatMemory
     */
    String chat(@MemoryId String memoryId, @UserMessage String userMessage);
}
```

### AiServices 构建配置

```java
AiAssistant assistant = AiServices.builder(AiAssistant.class)
    .chatModel(chatModel)                              // 固定模型
    .chatMemoryProvider(chatMemoryManager)              // ChatMemoryProvider（即 ChatMemoryManager）
    .systemMessageProvider(memoryId ->                  // 动态系统提示
        chatMemoryManager.getSystemPrompt(memoryId))
    .build();
```

### 自动编排流程

```
assistant.chat("session-123", "你好")
    │
    ├─ 1. @MemoryId("session-123") → chatMemoryProvider.get("session-123")
    ├─ 2. @UserMessage("你好")     → chatMemory.add(UserMessage.from("你好"))
    ├─ 3. systemMessageProvider    → 自动注入 SystemMessage
    ├─ 4. chatModel.chat(chatMemory.messages())
    ├─ 5. chatMemory.add(aiMessage)
    └─ 6. return aiMessage.text()
```

### 涉及文件

| 文件 | 操作 | 说明 |
|------|------|------|
| `ChatMemoryManager.java` | 修改 | 实现 `ChatMemoryProvider` 接口，支持 `get(Object memoryId)` 和 `getSystemPrompt()` |
| `AiAssistant.java` | 新增 | 声明式 AI 对话接口，使用 @MemoryId + @UserMessage |
| `AiAssistantConfiguration.java` | 新增 | 为每个 AiType 构建 AiAssistant Bean |
| `AbstractAiChatProvider.java` | 修改 | `chat()` 使用 AiAssistant 高阶 API，`customChat()` 保留低阶 API |

## 复杂提示工程 @StructuredPrompt
组合多变量生成专业提示词
```java
@Data
@StructuredPrompt("根据中国{{area}}法律，分析案件：{{case1}}")
class LegalPrompt {
    private String area;
    private String case1;
}

interface Lawyer {
    String analyze(LegalPrompt prompt);
}
```