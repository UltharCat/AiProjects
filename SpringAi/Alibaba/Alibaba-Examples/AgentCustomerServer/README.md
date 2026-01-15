# AgentCustomerServer

一个为 `AgentCustomer` 场景配套的 **MCP Server（WebFlux）** 示例服务：通过 MCP 协议对外暴露“订单查询”等工具能力，供 MCP Client / Agent 调用。

默认使用 **STREAMABLE** 协议，mcp endpoint：`/mcp`。

## 功能概览

- MCP Server 端点：`POST /mcp`（由 `spring-ai-starter-mcp-server-webflux` 提供）
- Tools：
  - `OrderTools#getOrderById(orderId)`：返回订单详情（依赖 MySQL/JPA）

工具注册：`com.ai.config.McpConfig` 使用 `MethodToolCallbackProvider` 注册 `OrderTools`。

## 技术栈

- Spring Boot Web
- Spring Data JPA + MySQL
- Spring AI Alibaba DashScope（示例依赖存在，便于组合“模型+工具”）
- Spring AI MCP Server（WebFlux / Streamable HTTP）

## 配置说明（`application.yml`）

- 端口：`server.port: 18082`
- MySQL：
  - `spring.datasource.*`（示例库 agent）
  - `spring.datasource.druid.mysql.*`（另一个 MySQL 连接配置）
  - `spring.jpa.hibernate.ddl-auto: create`（注意：会建表/重建表）
- DashScope：`spring.ai.dashscope.api-key: ${DASHSCOPE_API_KEY}`
- MCP Server：
  - `spring.ai.mcp.server.protocol: STREAMABLE`
  - `spring.ai.mcp.server.streamable-http.mcp-endpoint: /mcp`

## 运行前置条件

- MySQL 可用
- 如需模型调用：设置 `DASHSCOPE_API_KEY`

## 启动

在 `SpringAi/Alibaba/Alibaba-Examples/AgentCustomerServer` 目录下：

```powershell
mvn -q -DskipTests spring-boot:run
```

## 联动方式（典型）

- 作为 MCP Server：
  - `AgentCustomer`（或 `Alibaba-MCP-Client`）把 `spring.ai.mcp.client.*` 指向本服务的 `http://localhost:18082/mcp`

## 代码结构（核心类）

- 启动入口：`com.ai.AgentCustomerServerApplication`
- MCP 工具注册：`com.ai.config.McpConfig`
- Tools：`com.ai.tools.OrderTools`

## 更新日志（Changelog）

### v0.0.1-SNAPSHOT

- 初始化 AgentCustomerServer（WebFlux MCP Server）
- 增加 OrderTools 并注册为 MCP Tools
- 增加 MySQL/JPA 示例配置与订单查询能力

