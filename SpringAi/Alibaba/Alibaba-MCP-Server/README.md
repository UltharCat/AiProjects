# Alibaba-MCP-Server

一个基于 **Spring AI MCP Server（WebMVC, Streamable HTTP）** 的工具服务示例：把本地的 `@Tool` 方法以 MCP 协议对外暴露，供 MCP Client 或支持工具调用的 Agent 使用。

默认配置为 **STREAMABLE 协议**，mcp endpoint：`/mcp`。

## 功能概览

- MCP Server 端点：`POST /mcp`（由 `spring-ai-starter-mcp-server-webmvc` 自动提供）
- 工具集（注册到 MCP）：
  - `TimeTool#getCityTimeMethod(timeZoneId)`：按时区返回当前时间
  - `OrderTools#getOrderById(orderId)`：查询订单信息（依赖 MySQL/JPA）

工具注册方式：`com.ai.alibaba.config.McpConfig` 通过 `MethodToolCallbackProvider` 把 `TimeTool`、`OrderTools` 注入为 Tool。

## 技术栈

- Spring Boot Web
- Spring Data JPA + MySQL
- Spring AI Alibaba DashScope（示例依赖存在，用于扩展“模型端 + 工具端”组合）
- Spring AI MCP Server（WebMVC / Streamable HTTP）

## 配置说明（`application.yml`）

- 端口：`server.port: 18081`
- MySQL + JPA
  - `spring.datasource.*`
  - `spring.jpa.hibernate.ddl-auto: validate`（需要提前建表）
- DashScope
  - `spring.ai.dashscope.api-key: ${DASHSCOPE_API_KEY}`
- MCP Server
  - `spring.ai.mcp.server.protocol: STREAMABLE`
  - `spring.ai.mcp.server.streamable-http.mcp-endpoint: /mcp`

## 运行前置条件

- MySQL 可用，并存在相应表结构（因为默认 `ddl-auto=validate`）
- 若使用到 DashScope 相关能力：设置 `DASHSCOPE_API_KEY`

## 启动

在 `SpringAi/Alibaba/Alibaba-MCP-Server` 目录下：

```powershell
mvn -q -DskipTests spring-boot:run
```

## 代码结构（核心类）

- 启动入口：`com.ai.alibaba.AlibabaMcpServerApplication`
- MCP 工具注册：`com.ai.alibaba.config.McpConfig`
- Tools：
  - `com.ai.alibaba.tools.TimeTool`
  - `com.ai.alibaba.tools.OrderTools`

## 更新日志（Changelog）

### v0.0.1-SNAPSHOT

- 初始化 MCP Server（WebMVC + Streamable HTTP）
- 增加 TimeTool 与 OrderTools，并通过 ToolCallbackProvider 注册为 MCP Tools
- 增加 MySQL/JPA 基础配置与示例订单查询能力

