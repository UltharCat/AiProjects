# Alibaba-Examples

本目录是 `SpringAi/Alibaba/Alibaba-Examples` 的示例聚合模块（packaging=pom），用于放置“更贴近真实业务形态”的综合示例。

当前包含两个子项目：

- `AgentCustomer`：电商智能客服 Agent（RAG + Redis Memory + Tools，可选 MCP Client）
- `AgentCustomerServer`：为 Agent 场景配套的 MCP Server（订单工具）

> 两个模块默认端口分别为 `18081`（AgentCustomer）与 `18082`（AgentCustomerServer）。

---

## 子模块导航

- `AgentCustomer`
  - README：`AgentCustomer/README.md`

- `AgentCustomerServer`
  - README：`AgentCustomerServer/README.md`

---

## 典型运行方式

### 1) 只运行 AgentCustomer（本地 RAG + Tools）

适合只验证：
- `/agent/chat` 对话
- `/rag/*` 知识库写入/检索/删除
- Redis 对话记忆

前置依赖（按 `AgentCustomer` 的配置）：
- DashScope：`DASHSCOPE_API_KEY`
- Redis
- PostgreSQL + pgvector
- MySQL（示例业务库）

### 2) AgentCustomer + MCP（连接 AgentCustomerServer）

适合验证：
- Agent 在对话过程中调用“远端 MCP 工具”（例如订单查询）

启动顺序：
1. 启动 `AgentCustomerServer`（MCP Server，默认 `18082`，端点 `/mcp`）
2. 在 `AgentCustomer` 的 `application.yml` 中把 `spring.ai.mcp.client.enabled` 改为 `true`，并将连接指向 `http://localhost:18082/mcp`
3. 启动 `AgentCustomer`

> 说明：当前 `AgentCustomer` 配置里默认 `spring.ai.mcp.client.enabled: false`，因此不会主动连接 MCP。

---

## Changelog（聚合级）

### v0.0.1-SNAPSHOT

- 初始化示例聚合 `Alibaba-Examples`
- 增加电商客服 Agent 示例（AgentCustomer）
- 增加配套 MCP Server（AgentCustomerServer）

