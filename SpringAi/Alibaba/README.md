# SpringAi / Alibaba

本目录是 `SpringAi/Alibaba` 聚合模块（packaging=pom），汇总了基于 **Spring AI Alibaba（DashScope）** 的一组示例项目，覆盖：

- Chat / 多模态能力（image、audio）
- RAG（PgVector 本地向量库、DashScope 远程向量库）
- Tool Calling（`@Tool`）
- MCP（Model Context Protocol）Server/Client
- Graph（工作流/图编排）
- 综合示例：电商客服 Agent（RAG + Memory + Tools）

> 运行这些模块通常需要 DashScope API Key，以及可选的 Redis/MySQL/PostgreSQL(pgvector)/Ollama 等本地依赖。

---

## 子模块导航

- `Alibaba-Model`
  - DashScope 多能力示例（Chat/结构化输出/PromptTemplate、Image、Audio、RAG）
  - README：`Alibaba-Model/README.md`

- `Alibaba-FunctionCalling`
  - DashScope + Spring AI Tools 的 Tool Calling 示例（订单查询），并包含 MCP Server 配置
  - README：`Alibaba-FunctionCalling/README.md`

- `Alibaba-MCP-Server`
  - MCP Server（WebMVC）示例：将 `@Tool` 方法以 MCP 协议对外暴露（默认 `/mcp`）
  - README：`Alibaba-MCP-Server/README.md`

- `Alibaba-MCP-Client`
  - MCP Client（WebFlux）示例：连接 MCP Server 拉取 Tools，并在对话中通过 toolCallbacks 调用远端工具
  - README：`Alibaba-MCP-Client/README.md`

- `Alibaba-Graph`
  - 基于 spring-ai-alibaba-graph 的图编排/工作流示例
  - README：`Alibaba-Graph/README.md`

- `Alibaba-Examples`（聚合）
  - 综合示例集合（当前包含 AgentCustomer 与 AgentCustomerServer）
  - README：`Alibaba-Examples/README.md`

---

## 统一前置条件（按需）

- DashScope：设置环境变量 `DASHSCOPE_API_KEY`
- Redis：用于聊天记忆/缓存（部分模块可选）
- MySQL：用于订单数据/工具示例（FunctionCalling、MCP Server、AgentCustomerServer 等）
- PostgreSQL + pgvector：用于本地 VectorStore（Alibaba-Model、AgentCustomer 等）

> 各模块在自己 `application.yml` 中都有端口配置；多个服务同时运行时建议避免端口冲突（常见默认：`18081/18082`）。

---

## 推荐启动顺序（典型场景）

### 1) MCP 联动（Client -> Server）

1. 启动 `Alibaba-MCP-Server`（默认 `18081`，`/mcp`）
2. 启动 `Alibaba-MCP-Client`（默认 `18082`）
3. 调用 `Alibaba-MCP-Client` 的接口触发对话 + 工具调用

### 2) AgentCustomer 联动（可选接 MCP）

- 若只跑本地 RAG + Tools：
  1. 在 `AgentCustomer` 中开启 `spring.ai.mcp.client.enabled=false`，启动 `AgentCustomer`
- 若同时需要 MCP 工具：
  1. 启动 `AgentCustomerServer`（MCP Server，默认 `18082`）
  2. 在 `AgentCustomer` 中开启 `spring.ai.mcp.client.enabled=true` 并指向 `AgentCustomerServer`
  3. 启动 `AgentCustomer`

---

## Changelog（聚合级）

### v0.0.1-SNAPSHOT

- 初始化 `SpringAi/Alibaba` 聚合模块
- 增加 Model/FunctionCalling/MCP Client/MCP Server/Graph/Examples 等子项目

