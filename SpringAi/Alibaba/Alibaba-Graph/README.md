# Alibaba-Graph

一个基于 **spring-ai-alibaba-graph** 的“图编排/工作流”示例项目：用 `StateGraph` 把多个 AI 处理步骤串起来，并提供 HTTP 接口触发执行。

目前包含两条主要示例链路：

1. `quickStartGraph`：最小图示例（node1 -> node2）
2. `sentenceCreateGraph`：句子生产流水线（生成 ->（可选循环优化）-> 翻译 -> 语音合成）

## 功能概览

### 1) quickStartGraph

- `GET /graph/quickStartGraph`
- 执行：`node1`、`node2` 两个异步节点，演示 State 的追加/合并策略

### 2) sentenceCreateGraph（造句 + 优化 + 翻译 + TTS）

- `GET /graph/sentenceCreateGraph?handle=...`
- 输入：`handle`（关键字）
- 执行节点：
  - `GenerateSentenceNode`：让模型基于关键字生成句子，并输出 `sentence` + `positiveScore(0~10)`（JSON）
  - `OptimizeSentenceNode`：按 `positiveScore` 条件循环优化（示例条件：>5 则继续优化）
  - `TranslateSentenceNode`：翻译为英文（输出 `translate`）
  - `SentenceAudioSynthesisNode`：TTS 合成，写入 `src/main/resources/gen/tts/output.mp3`，返回音频文件路径
- 线程维度：通过 `RunnableConfig.threadId(httpServletRequest.getRequestedSessionId())` 绑定会话线程

## 技术栈

- Spring Boot Web
- Spring AI Alibaba DashScope（对话 + TTS）
- spring-ai-alibaba-graph-core（StateGraph/CompiledGraph）
- fastjson（节点产出/解析 JSON）

## 配置说明（`application.yml`）

- 端口：`server.port: 18081`
- DashScope
  - `spring.ai.dashscope.api-key: ${DASHSCOPE_API_KEY}`
  - `spring.ai.dashscope.chat.options.model: qwen-max`

## 运行前置条件

- 设置环境变量 `DASHSCOPE_API_KEY`
- 具备可用的 DashScope 模型权限（chat + audio speech）

## 启动

在 `SpringAi/Alibaba/Alibaba-Graph` 目录下：

```powershell
mvn -q -DskipTests spring-boot:run
```

## 调用示例

- quickStartGraph：

```http
GET http://localhost:18081/graph/quickStartGraph
```

- sentenceCreateGraph：

```http
GET http://localhost:18081/graph/sentenceCreateGraph?handle=孤独
```

## 代码结构（核心类）

- 启动入口：`com.ai.alibaba.AlibabaGraphApplication`
- Graph 装配：`com.ai.alibaba.config.GraphConfig`
- Controller：`com.ai.alibaba.controller.GraphController`
- Nodes：
  - `GenerateSentenceNode`
  - `OptimizeSentenceNode`
  - `TranslateSentenceNode`
  - `SentenceAudioSynthesisNode`

## 更新日志（Changelog）

### v0.0.1-SNAPSHOT

- 初始化 Graph 示例模块（spring-ai-alibaba-graph-core）
- 增加 quickStartGraph：演示基础节点/边/状态策略
- 增加 sentenceCreateGraph：造句 ->（可选循环优化）-> 翻译 -> 语音合成（落盘 mp3）

