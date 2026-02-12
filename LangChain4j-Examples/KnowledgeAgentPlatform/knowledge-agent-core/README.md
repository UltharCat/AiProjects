# Knowledge Agent Core 核心架构

`knowledge-agent-core` 模块作为系统的"大脑"，采用 **"宏观状态机 (FSM) + 微观 ReAct (Reasoning & Acting)"** 的双层混合架构。

这一设计旨在解决纯 ReAct 容易"跑题"、纯状态机过于呆板的问题，实现既有明确辅导流程，又能灵活处理复杂问题的智能体。

## 🧠 核心架构理念：双层控制 (The Dual-Layer Control)

1.  **宏观层 (Macro Layer) - 有限状态机 (FSM)**
    *   **职责**: 控制对话的整体节奏和当前任务目标。
    *   **实现**: Java 侧维护 `ConversationState` 枚举与流转逻辑。
    *   **作用**: 防止 LLM 在深度教学时突然闲聊，或者在复习时忘记纠错。
2.  **微观层 (Micro Layer) - ReAct Agent**
    *   **职责**: 在特定状态内，通过"思考-行动-观察"循环解决具体问题。
    *   **实现**: Google Gemini + LangChain4j Tools。
    *   **作用**: 灵活调用搜索工具、数据库工具，根据上下文生成具体回复。

## 🔄 状态机设计 (FSM Design)

状态机定义了 Agent 在不同阶段的**行为准则 (Behavior Instruction)**。

| 状态 (State) | 描述 (Description) | 核心指令 (Instruction) | 流转条件 (Transition) |
| :--- | :--- | :--- | :--- |
| **IDLE** | 待机/闲聊 | 友好回复，探测用户意图。 | 用户提问知识点 -> `TEACHING` <br> 用户请求复习 -> `REVIEW` |
| **TEACHING** | 深度辅导 | **苏格拉底教学法**。禁止直接给答案，通过反问引导用户思考。 | 用户确认理解/说"懂了" -> `SUMMARY` |
| **SUMMARY** | 知识结晶 | 总结对话核心点，生成结构化笔记。 | 总结完成 -> `IDLE` |
| **REVIEW** | 复习/抽认卡 | 根据艾宾浩斯曲线提问。若用户答错，进行纠正。 | 复习结束 -> `IDLE` |

## 🔗 ReAct 与 Prompt 的动态结合

为了让 LLM "感知" 到状态机，我们采用 **State-Driven Prompting (状态驱动提示词)** 策略。

### 1. 动态 System Prompt
我们不写死一个巨大的 Prompt，而是根据 Java 中的 `CurrentState` 动态注入指令。

```java
// 伪代码示例
String systemPrompt = """
    你是一个专业的知识辅导助手。
    
    ### 核心规则 (Core Rules)
    1. 你必须严格遵守下方的 [当前状态指令] 进行行动。
    2. 遇到不确认的知识，必须使用工具 `searchVector` 检索。
    
    ### 当前状态指令 (Current State Instruction)
    {{state_instruction}}
    """;
```

*   当处于 **TEACHING** 时，`{{state_instruction}}` 会被替换为：
    > "当前处于深度辅导模式。请一步步引导用户，每次只讲一个概念。如果用户偏离话题，请礼貌地拉回。"
*   当处于 **REVIEW** 时，`{{state_instruction}}` 会被替换为：
    > "当前处于复习模式。请根据用户的回答判断掌握程度。如果回答错误，通过 searchVector 查找正确答案并解释。"

### 2. 工具驱动流转 (Tool-Driven Transition)
LLM 拥有改变状态的"权利"。我们提供一个特殊的 Tool 给 LLM。

```java
@Tool("切换当前对话的状态。当你觉得当前阶段的任务已经完成，或者用户意图改变时，必须调用此工具。")
public void switchState(ConversationState targetState) {
    this.currentState = targetState;
    // 触发状态变更后的逻辑 (如推送前端 SSE 事件)
}
```

**执行流程**:
1.  用户说："我明白了，这个概念挺简单的。"
2.  LLM **思考 (Thought)**: "用户表示理解了，根据 TEACHING 状态的规则，我应该结束教学并进行总结。"
3.  LLM **行动 (Action)**: 调用 `switchState(SUMMARY)`。
4.  Java **更新 (Update)**: 后台状态变更为 SUMMARY。
5.  下一轮次: System Prompt 更新为 SUMMARY 的指令，Agent 开始输出总结内容。

