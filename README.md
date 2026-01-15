# AI-Projects

这是一个用于探索和实践各种大语言模型（LLM）与 AI 工程化能力的 **Java/Spring Boot 多模块项目集合**。仓库聚焦于：

- 多模型接入：Ollama、本地/云端 OpenAI Compatible、Alibaba DashScope（通义千问）
- 多种交互形态：同步、SSE/HTTP 流式
- Agent 能力：Tool Calling、MCP（Model Context Protocol）、RAG（向量检索增强）、会话记忆
- 工程实践：多模块 Maven 组织、可复用的配置/封装方式

> 本仓库以“示例 + 实验”为主：各模块都尽量做到可运行、可复用，但部分能力需要依赖外部服务（Redis/MySQL/PostgreSQL/Ollama/DashScope）。

---

## 仓库结构（Maven Modules）

- 根聚合：`AiProjects`
  - `LangChain4j-Examples`（示例聚合，内容以实际模块为准）
  - `SpringAi`
    - `Ollama-Base`
    - `ChatClient`
    - `Model`
    - `Alibaba`
      - `Alibaba-Model`
      - `Alibaba-FunctionCalling`
      - `Alibaba-MCP-Server`
      - `Alibaba-MCP-Client`
      - `Alibaba-Graph`
      - `Alibaba-Examples`
        - `AgentCustomer`
        - `AgentCustomerServer`

---

## 快速开始（本地运行）

### 1) 环境要求

- JDK：21（根 `pom.xml` 指定）
- Maven

### 2) 通用外部依赖（按模块选择）

- **Ollama**：默认 `http://localhost:11434`（用于 `Ollama-Base`、以及部分 spring-ai-ollama 示例模块）
- **DashScope**：需要环境变量 `DASHSCOPE_API_KEY`（用于 Alibaba 系列模块以及 OpenAI compatible 示例）
- **Redis**：默认 `localhost:6379`（用于会话记忆/ChatMemory）
- **MySQL**：用于订单/工具类示例（FunctionCalling、MCP Server、AgentCustomerServer 等）
- **PostgreSQL + pgvector**：用于本地向量库（Alibaba-Model、AgentCustomer 等）

### 3) 构建（跳过测试）

```powershell
cd /d F:\JavaProjects\AiProjects
mvn -q -DskipTests package
```

### 4) 运行单个模块（示例）

```powershell
# 例如：启动 Ollama-Base
cd /d F:\JavaProjects\AiProjects\SpringAi\Ollama-Base
mvn -q -DskipTests spring-boot:run
```

> 说明：部分模块默认端口存在重叠（例如多个模块使用 8081/18081）。并行运行时建议修改各自 `application.yml` 的 `server.port`。

---

## 模块导航与功能简介

### SpringAi

- **Ollama-Base**：最小 Ollama HTTP 转发示例（RestTemplate 调用 Ollama API）
  - README：`SpringAi/Ollama-Base/README.md`

- **ChatClient**：统一 `ChatClient` 的应用层示例（支持 Ollama/OpenAI compatible 切换 + 多种流式输出 + Redis ChatMemory）
  - README：`SpringAi/ChatClient/README.md`

- **Model**：模型层示例（`AiModelFactory` 统一选择 Chat/Image 等底层 Model，并支持运行时切换）
  - README：`SpringAi/Model/README.md`

### SpringAi/Alibaba

- **Alibaba-Model**：DashScope 多能力示例（Chat/结构化输出/PromptTemplate、Image、Audio、RAG：PgVector 本地 + DashScope 远程向量库）
  - README：`SpringAi/Alibaba/Alibaba-Model/README.md`

- **Alibaba-FunctionCalling**：DashScope + Spring AI Tools 的订单查询 Tool Calling 示例（同时包含 MCP Server 配置）
  - README：`SpringAi/Alibaba/Alibaba-FunctionCalling/README.md`

- **Alibaba-MCP-Server**：MCP Server（WebMVC，`/mcp`）示例，注册 TimeTool/OrderTools 对外提供工具能力
  - README：`SpringAi/Alibaba/Alibaba-MCP-Server/README.md`

- **Alibaba-MCP-Client**：MCP Client（WebFlux）示例，连接 MCP Server 获取 Tools，并在对话中通过 toolCallbacks 调用远端工具
  - README：`SpringAi/Alibaba/Alibaba-MCP-Client/README.md`

- **Alibaba-Graph**：基于 spring-ai-alibaba-graph 的图编排/工作流示例（造句 ->（可选循环优化）-> 翻译 -> TTS）
  - README：`SpringAi/Alibaba/Alibaba-Graph/README.md`

- **Alibaba-Examples / AgentCustomer**：电商客服 Agent 示例（RAG + Redis 记忆 + Tools + 可选 MCP Client）
  - README：`SpringAi/Alibaba/Alibaba-Examples/AgentCustomer/README.md`

- **Alibaba-Examples / AgentCustomerServer**：为 Agent 场景配套的 MCP Server（WebFlux，`/mcp`），对外暴露订单工具
  - README：`SpringAi/Alibaba/Alibaba-Examples/AgentCustomerServer/README.md`

---

## 一些实现要点（按当前代码现状整理）

- **统一模型调用的两种路线**
  - 使用 `ChatClient`（更贴近应用层：支持 advisor、memory、tool calling、stream）
  - 直接使用底层 `Model<?, ?>`（更贴近能力层：ChatModel/ImageModel/AudioModel 等，通过工厂统一选择）

- **会话记忆（ChatMemory）**
  - `ChatClient` 模块提供了自定义 `RedisChatMemory`（Redis List + Lua 脚本原子追加/裁剪/续期 TTL）
  - `AgentCustomer` 使用 Redis repository 进行对话记忆持久化

- **RAG（向量检索增强）**
  - `Alibaba-Model`：支持 PgVector（本地）与 DashScope Cloud Store（远程）两种向量存储模式切换
  - `AgentCustomer`：提供 RAG 管理接口 + RAG Tools，便于在对话中由模型主动调用写入/检索/删除

- **MCP（Model Context Protocol）**
  - MCP Server：把 `@Tool` 方法以 MCP 形式暴露（默认 `/mcp`）
  - MCP Client：把远端 MCP Tools 拉取为 `ToolCallbackProvider`，供对话时调用

---

## Changelog（仓库级）

### v0.0.1-SNAPSHOT

- 初始化多模块 Maven 工程（JDK 21 / Spring Boot 3.5.x / Spring AI 1.1.x）
- 增加 Ollama、OpenAI compatible、DashScope 等多种模型接入示例
- 增加 Tool Calling、MCP、RAG、Graph 等典型 AI 应用形态的示例子项目
