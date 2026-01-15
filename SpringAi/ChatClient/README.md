# ChatClient

一个偏“应用层”的 Spring AI 示例：通过统一的 `ChatClient` 对外提供 **同步 + 流式**聊天接口，并支持通过配置在不同模型后端之间切换（当前包含 **Ollama** 与 **OpenAI Compatible** 两类 starter）。

本模块还演示了一个轻量的 **会话记忆（ChatMemory）** 方案：
- 默认使用 `MessageWindowChatMemory`（内存）
- 配置为 `x-project.app.chat.memory.type=redis` 时启用自定义 `RedisChatMemory`（Redis List + Lua 原子追加/裁剪/续期 TTL）

## 功能概览

- 模型后端切换：`x-project.client.type`
  - `ollama`：使用 `spring-ai-starter-model-ollama`
  - `openAi`：使用 `spring-ai-starter-model-openai`（本项目示例里配置成 DashScope OpenAI Compatible Endpoint）
- 对话接口（`com.ai.controller.ChatController`）：
  - 同步：`GET /chat/singleChat?prompt=...`
  - 同步结构化：`GET /chat/singleChat/entity?prompt=...`（返回 `Country`）
  - 流式 SSE：`GET /chat/stream/sse?prompt=...`
  - 流式 SSE（Flux）：`GET /chat/stream/sse/flux?prompt=...`
  - 流式 HTTP：`GET /chat/stream/httpV1?prompt=...`、`GET /chat/stream/httpV2?prompt=...`
  - 带会话记忆的流式 HTTP：`GET /chat/memory/stream/http?prompt=...`（按 `HttpSession` 维度记忆）

## 技术栈

- Spring Boot Web
- Spring AI（ChatClient / ChatModel）
- Redis（可选，用于 ChatMemory）

## 配置说明（`application.yml`）

关键配置项：

- 服务端口
  - `server.port`: `8081`
- 选择模型后端
  - `x-project.client.type`: `openAi | ollama`
- Ollama（当 `x-project.client.type=ollama` 时生效）
  - `spring.ai.ollama.base-url`: `http://localhost:11434`
  - `spring.ai.ollama.chat.options.model`: `gpt-oss:20b`
- OpenAI Compatible（当 `x-project.client.type=openAi` 时生效）
  - `spring.ai.openai.api-key`: `${DASHSCOPE_API_KEY}`
  - `spring.ai.openai.base-url`: `https://dashscope.aliyuncs.com/compatible-mode`
  - `spring.ai.openai.chat.options.model`: `qwen-omni-turbo`
- ChatMemory
  - `x-project.app.chat.memory.type`: `redis | in-memory | sqlite`（当前实现：redis / in-memory）
  - `x-project.app.chat.memory.namespace`: `chat:memory:`
  - `x-project.app.chat.memory.ttl`: `24h`

## 运行前置条件

- 选择 `ollama` 时：本机已启动 Ollama，且模型已拉取
- 选择 `openAi` 时：已设置环境变量 `DASHSCOPE_API_KEY`
- 选择 Redis 记忆时：本机 Redis 可用（默认 `localhost:6379`）

## 启动

在 `SpringAi/ChatClient` 目录下：

```powershell
mvn -q -DskipTests spring-boot:run
```

## 快速调用示例

- 同步对话：

```http
GET http://localhost:8081/chat/singleChat?prompt=你好
```

- SSE 流式：

```http
GET http://localhost:8081/chat/stream/sse/flux?prompt=请用三句话解释RAG
```

- 带记忆的流式（按 Session 维度）：

```http
GET http://localhost:8081/chat/memory/stream/http?prompt=我们刚刚聊了什么？
```

## 代码结构（核心类）

- 启动入口：`com.ai.ChatClientApplication`
- 对外接口：`com.ai.controller.ChatController`
- 模型选择与 ChatClient 装配：`com.ai.config.AiClientConfig`
  - 按命名约定从 `Map<String, ChatModel>` 里选择 `{type}ChatModel`
- ChatMemory 装配：`com.ai.config.ChatMemoryAutoConfiguration`
- Redis ChatMemory 实现：`com.ai.config.memory.RedisChatMemory`

## 更新日志（Changelog）

### v0.0.1-SNAPSHOT

- 初始化 ChatClient 示例服务（Spring Boot Web + Spring AI）
- 支持切换模型后端：Ollama / OpenAI Compatible（示例配置对接 DashScope）
- 增加同步、SSE 流式、HTTP 流式等多种对话接口
- 增加 ChatMemory 自动装配：默认内存，实现 Redis 版本（TTL + 可选裁剪）

