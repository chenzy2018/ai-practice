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

### ChatLanguageModel
通用对话模型标准接口
* 所有大模型、自定义私有模型均实现该接口
* 统一generate调用方法，实现底层模型无感切换

### StreamingChatLanguageModel
通用流式对话标准接口
* 所有大模型、自定义私有模型均实现该接口
* 统一generate调用方法，实现底层模型无感切换

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