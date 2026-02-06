# 开发路线图：知识 Agent 平台 (Knowledge Agent Platform)

本路线图指导企业级智能知识 Agent 的开发，按逻辑阶段划分。

> **项目根目录**: `F:\JavaProjects\AiProjects\LangChain4j-Examples\KnowledgeAgentPlatform`

## 🏁 第 0 阶段：前提条件与环境搭建
**目标**: 准备本地开发环境并确保所有依赖项已管理。

- [x] **项目结构**: 创建 Maven 模块 (`common`, `api`, `user`, `core`, `rag`, `gateway`)。
- [x] **依赖管理**: 修复根目录 `AiProjects/pom.xml` 中的 `flyway.version` 和 `dubbo.version`。
- [ ] **基础设施 (Docker)**: 使用 Docker Compose 设置以下服务：
    - **Nacos** (注册与配置中心) - 端口 `8848`
    - **MySQL 8.0+** (用户/核心数据库) - 端口 `3306`
    - **Redis** (短期记忆) - 端口 `6379`
    - **PostgreSQL 15+** (带 `pgvector` 扩展) - 端口 `5432`
    - **Neo4j 5.x** (知识图谱) - 端口 `7687` (Bolt), `7474` (HTTP)
    - **RocketMQ 5.x** (NameServer & Broker) - 端口 `9876`, `10911`

## 🏗 第 1 阶段：核心契约与公共模块
**目标**: 定义通信协议和共享工具。

- [ ] **`knowledge-agent-common`**:
    - 实现 `Result<T>` 包装器以标准化 API 响应。
    - 添加通用异常 (`BusinessException`, `SystemException`) 和全局异常处理器。
    - 添加工具类 (JSON, Date, Security)。
- [ ] **`knowledge-agent-api`**:
    - 定义 Dubbo 接口 (`UserService`, `AgentService`, `RagService`)。
    - 创建 DTO (数据传输对象)，如 `UserLoginRequest`, `ChatRequest`, `KnowledgeDTO` 等。

## 👤 第 2 阶段：用户服务 (基础)
**目标**: 管理用户身份和偏好。

- [ ] **数据库设置**: 应用 Flyway 脚本 `V1.0.0__init_user_schema.sql` (MySQL)。
- [ ] **实现**:
    - 使用 MyBatis-Plus 实现 `UserServiceImpl`。
    - 创建 `User` 实体和 Mapper。
    - 实现登录/注册逻辑 (使用 BCrypt 进行密码哈希)。
    - 实现 `getUserProfile(userId)` 以检索偏好设置。
- [ ] **Dubbo 暴露**: 通过 Dubbo 协议暴露 `UserService`。

## 🧠 第 3 阶段：RAG 服务 (知识库)
**目标**: 启用向量存储和图谱操作。

- [ ] **数据库设置**:
    - 应用 Flyway 脚本 `V1.0.0__init_pgvector.sql` (PostgreSQL)。
    - 配置 Neo4j 连接。
- [ ] **LangChain4j 集成**:
    - 配置 `PgVectorEmbeddingStore` Bean。
    - 配置 `Neo4jGraph` Bean。
- [ ] **实现**:
    - 实现 `RagServiceImpl`。
    - `searchKnowledge(query)`: 混合搜索 (关键字 + 向量)。
    - `storeKnowledge(knowledgeDTO)`: 将摘要保存到向量数据库，将实体保存到 Neo4j。
- [ ] **Dubbo 暴露**: 暴露 `RagService`。

## 🤖 第 4 阶段：Agent 核心服务 (大脑)
**目标**: 集成 LLM 并编排逻辑。

- [ ] **LLM 集成**:
    - 配置 LangChain4j 对接 `DashScope` (通义千问) 和 `OpenAI` (Gemini)。
    - 创建 `ChatLanguageModel` Bean。
- [ ] **Agent 逻辑**:
    - 实现 `AgentServiceImpl`。
    - **状态机**: 实现简单的 FSM (IDLE 空闲 -> LEARNING 学习中 -> SUMMARIZING 总结中)。
    - **记忆**: 使用 Redis 存储 `ChatMemory` (消息列表)。
    - **编排**:
        1. 接收用户输入。
        2. 从 Redis + RAG 服务检索上下文。
        3. 调用 LLM 生成响应。
        4. 检测是否达成 "知识结论"。
- [ ] **艾宾浩斯定时任务**: 实现一个基本的定时任务 (Quartz/Spring Scheduler) 来验证提醒功能。

## 🌐 第 5 阶段：网关与 API
**目标**: 允许外部通过 HTTP 访问。

- [ ] **Web 层**:
    - 创建 `AuthController` (登录/注册)。
    - 创建 `ChatController` (用于流式聊天的 SSE 端点)。
- [ ] **Dubbo 消费**:
    - 注入 `@DubboReference` 以使用 User, Agent, 和 RAG 服务。
- [ ] **安全**: 实现 JWT 验证过滤器。

## ⚡ 第 6 阶段：高级一致性与特性
**目标**: 生产级可靠性。

- [ ] **RocketMQ 事务**:
    - 在 `AgentService` 中，将直接的 RAG 调用替换为 RocketMQ 事务消息，用于 "知识存储"。
    - 实现 `RocketMQLocalTransactionListener` 以记录本地事务。
    - 在 `RagService` 中，实现 `@RocketMQMessageListener` 以消费消息并写入数据库。
- [ ] **文生图**:
    - 在 Agent 服务中集成 `ImageModel` (Wanx/DALL-E)。
    - 当触发艾宾浩斯提醒时生成图谱可视化。

## 🚢 第 7 阶段：CI/CD 与部署
**目标**: 实现自动化构建与容器化部署。

- [ ] **Docker 化**:
    - 为每个模块 (`user`, `core`, `rag`, `gateway`) 编写 `Dockerfile`。
    - 优化镜像体积 (使用 OpenJDK 21 Slim)。
- [ ] **CI 流水线**:
    - 配置自动化构建脚本 (Maven Build -> Unit Test -> Docker Build)。
- [ ] **部署**:
    - 编写生产环境的 `docker-compose-prod.yml`。
    - 配置容器健康检查 (Health Checks) 与资源限制。

## 🚀 现在如何开始？

1.  **环境**: 运行 `docker-compose up -d`。
2.  **构建**: 在 `KnowledgeAgentPlatform` 中运行 `mvn clean install`。
3.  **代码**: 从 **第 1 阶段** 开始 (在 `api` 模块中创建包和类)。
