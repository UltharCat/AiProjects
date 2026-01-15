# AgentCustomer

一个“电商智能客服 Agent”示例：基于 **DashScope + RAG（pgvector）+ Redis ChatMemory + Tools**，实现一个具备知识库检索与工具调用能力的客服对话服务。

该模块的定位更偏“真实 Agent 应用样例”，包含：

- **对话 Agent**（流式输出）
- **知识库管理（RAG）**：文本/文件入库、相似度检索、按文档编号删除
- **会话记忆**：Redis 持久化聊天记录
- **工具能力**：RAG 相关工具（写入/检索/删除）作为 `@Tool` 暴露给模型
- （可选）MCP Client 能力：配置项已预留，可连接外部 MCP Server

## 功能概览

### 1) Agent 对话

- `POST /agent/chat`
  - 请求体：`AgentChatRequest`
  - 返回：`Flux<String>`（流式输出）

Agent 内部集成：
- `MessageChatMemoryAdvisor`：基于 Redis 的聊天记忆（最多保留 100 条）
- `RetrievalAugmentationAdvisor`：向量检索增强（similarityThreshold=0.4, topK=10）
- `defaultTools(ragTools)`：让模型可调用 RAG 工具方法

### 2) RAG 管理接口

- `POST /rag/insert-content`：写入一段文本到向量库
- `POST /rag/upload-file`：上传文件并入库（支持 PDF，非 PDF 走 Tika）
- `GET /rag/search-documents?content=...&topK=...`：相似度检索
- `DELETE /rag/delete-documents?documentNumber=...`：按业务文档编号删除

### 3) 知识库/Prompt 资源

- `src/main/resources/docs/customer-talking.md`
  - “电商智能客服话术准则”示例文档，可作为入库素材
- `src/main/resources/prompts/agent-system-qs.st`
  - agent system prompt 模板（当前代码里也内置了一份等价 system prompt）

## 技术栈

- Spring Boot Web
- Spring AI Alibaba DashScope
- Spring AI VectorStore + PgVector
- PostgreSQL（pgvector）
- MySQL（业务库/示例数据）
- Redis（对话记忆）
- Tika + PDF Reader（文档解析）
- （可选）Spring AI MCP Client（WebFlux）

## 配置说明（`application.yml`）

- 端口：`server.port: 18081`
- MySQL（业务相关）：`spring.datasource.*`
- PostgreSQL（pgvector，通过 druid 的另一个 datasource 配置）：`spring.datasource.druid.pgvector.*`
- DashScope：`spring.ai.dashscope.api-key: ${DASHSCOPE_API_KEY}`
- Embedding：`spring.ai.dashscope.embedding.options.*`（dimensions=1024）
- PgVector：`spring.ai.vectorstore.pgvector.*`（dimensions=1024）
- Redis Memory：`spring.ai.memory.redis.*`
- MCP Client（默认关闭）：`spring.ai.mcp.client.enabled: false`

## 运行前置条件

1. 设置环境变量 `DASHSCOPE_API_KEY`
2. Redis 可用（聊天记忆）
3. PostgreSQL 可用并启用 pgvector（向量库）
4. MySQL 可用（示例业务库）

## 启动

在 `SpringAi/Alibaba/Alibaba-Examples/AgentCustomer` 目录下：

```powershell
mvn -q -DskipTests spring-boot:run
```

## 使用示例

- 发起一次对话（示例，仅展示结构；字段以实际 `AgentChatRequest` 为准）：

```http
POST http://localhost:18081/agent/chat
Content-Type: application/json

{
  "conversationId": "demo",
  "content": "我的订单一直没发货，怎么办？"
}
```

- 把 `customer-talking.md` 内容入库：
  - 可通过 `/rag/upload-file` 上传文件
  - 或通过 `/rag/insert-content` 分段写入文本

## 代码结构（核心类）

- 启动入口：`com.ai.AgentCustomerClientApplication`
- Agent：
  - `com.ai.agent.controller.AgentController`
  - `com.ai.agent.service.impl.AgentServiceImpl`
- RAG：
  - `com.ai.rag.controller.RagController`
  - `com.ai.rag.service.impl.RagServiceImpl`
- Tools：`com.ai.tools.RagTools`

## 更新日志（Changelog）

### v0.0.1-SNAPSHOT

- 初始化电商客服 Agent 示例（DashScope + RAG + Redis Memory）
- 增加 RAG 管理接口：文本/文件入库、检索、删除
- 增加基于 Redis 的 ChatMemory（MessageWindowChatMemory + repository）
- 增加 RAG 工具（`RagTools`）以供模型在对话中调用
- 预留 MCP Client 配置，支持接入外部 MCP Server（默认关闭）

