# Alibaba-MCP-Client

一个基于 **Spring AI MCP Client（WebFlux, Streamable HTTP）** 的示例：连接远端 MCP Server（默认 `http://localhost:18081/mcp`），将 MCP Server 暴露的 Tools 拉取到本地，作为 `ToolCallbackProvider` 注入到 `ChatClient`，实现“模型对话过程中调用远端工具”。

## 依赖关系

- 需要先启动 MCP Server：`Alibaba-MCP-Server`（或任何兼容 MCP 的服务）
- 本模块自身使用 DashScope ChatModel 作为对话模型（需要 `DASHSCOPE_API_KEY`）

## 功能概览

- `GET /mcpChat/callOrderTools?question=...`
  - 使用 `ChatClient` + `.toolCallbacks(toolCallbackProvider)`
  - 由模型决定是否调用来自 MCP Server 的 Tools（例如订单查询、时间工具等）
  - 返回：`text/event-stream`（Flux 流式输出）

## 技术栈

- Spring Boot Web
- Spring AI Alibaba DashScope（Chat）
- Spring AI MCP Client（WebFlux / Streamable HTTP）

## 配置说明（`application.yml`）

- 端口：`server.port: 18082`
- DashScope
  - `spring.ai.dashscope.api-key: ${DASHSCOPE_API_KEY}`
  - `spring.ai.dashscope.chat.options.model: qwen-max`
- MCP Client
  - `spring.ai.mcp.client.enabled: true`
  - `spring.ai.mcp.client.streamable-http.connections.server1.url: http://localhost:18081`
  - `spring.ai.mcp.client.streamable-http.connections.server1.endpoint: /mcp`

## 启动顺序

1. 启动 MCP Server（例如 `Alibaba-MCP-Server`，默认端口 18081）
2. 启动本模块（默认端口 18082）

启动命令（在 `SpringAi/Alibaba/Alibaba-MCP-Client` 目录下）：

```powershell
mvn -q -DskipTests spring-boot:run
```

## 调用示例

```http
GET http://localhost:18082/mcpChat/callOrderTools?question=帮我查询订单ID为1的订单信息
```

## 代码结构（核心类）

- 启动入口：`com.ai.alibaba.AlibabaMcpClientApplication`
- Controller：`com.ai.alibaba.controller.McpChatController`

## 更新日志（Changelog）

### v0.0.1-SNAPSHOT

- 初始化 MCP Client（WebFlux / Streamable HTTP）
- 增加对 DashScope ChatModel 的封装调用，并支持通过 MCP 回调调用远端 Tools
- 增加示例接口 `/mcpChat/callOrderTools`（SSE/Flux 流式返回）

