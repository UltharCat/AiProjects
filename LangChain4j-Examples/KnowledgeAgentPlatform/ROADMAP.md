# 开发路线图：知识 Agent 平台 (Knowledge Agent Platform)

本路线图指导企业级智能知识 Agent 的开发。根据最新架构设计，采用了 **"MySQL (Core Truth) + Milvus (Semantic Index)"** 的双层记忆体系，并明确了各阶段的实施细节。

> **项目根目录**: `F:\JavaProjects\AiProjects\LangChain4j-Examples\KnowledgeAgentPlatform`

## 🏁 第 0 阶段：环境准备与基础设施
**目标**: 搭建本地开发环境，确保微服务基础设施就绪。

- [x] **项目结构**: 基于 Maven 多模块构建 (`common`, `api`, `user`, `core`, `rag`, `gateway`)。
- [ ] **服务注册与配置中心 (Nacos)**:
    - 部署 Nacos (Stand-alone/Cluster)。
    - 为各模块创建 `bootstrap.yml`，配置 `spring.cloud.nacos.config`。
    - 定义 `dev` 和 `prod` 命名空间。
- [ ] **数据存储设施**:
    - **MySQL 8.0+**: 初始化 `knowledge_platform` 库。
    - **Redis 7.x**: 用于 Session 缓存和短期记忆。
    - **Milvus 2.4+**: 部署向量数据库 (Standalone)。
    - **RocketMQ 5.x**: 部署 NameServer 和 Broker，创建业务 Topic (`topic-knowledge-sync`, `topic-review-push`)。

## 🏗 第 1 阶段：核心契约 (API & Common)
**目标**: 定义服务间通信标准、数据模型和工具类。

- [ ] **公共组件 (`knowledge-agent-common`)**:
    - **统一响应**: `Result<T>` (code, message, data).
    - **异常体系**: `BizException`, `GlobalExceptionHandler`.
    - **工具箱**: `JsonUtil`, `DateUtil`, `SecurityContext` (MyBatis-Plus 自动填充支持).
- [ ] **接口定义 (`knowledge-agent-api`)**:
    - **DTO 模型**:
        - `KnowledgeCardDTO`: 知识卡片传输对象 (id, question, answer, tags, ef, nextReviewTime).
        - `ChatCommand`: 包含 userId, query, intent (LEARN/REVIEW).
    - **Dubbo 接口**:
        - `KnowledgeManageService`: 知识点的增删改查 (CRUD)。
        - `AgentInteractionService`: 深度辅导与对话状态流转。
        - `UserProfileService`: 用户画像与偏好查询。

## 👤 第 2 阶段：用户身份与画像 (User Service)
**目标**: 建立用户体系，为后续的个性化辅导（虽静态角色，但有偏好）打基础。

- [ ] **数据库设计 (MySQL)**:
    - `sys_user`: 基础账号信息。
    - `user_learning_profile`: 学习偏好配置（如：每日复习上限、偏好回答风格）。
- [ ] **功能实现**:
    - 基于 JWT 的身份认证。
    - 实现 `UserProfileService`，供 Agent 读取用户偏好。

## 🧠 第 3 阶段：知识存储与 RAG 核心 (RAG Service)
**目标**: 实现 **MySQL 存本体、Milvus 存索引** 的双层存储机制，以及关键的 **关联更新策略**。

- [ ] **MySQL 核心记忆库 (`knowledge_card`)**:
    - **表结构设计**:
        - `content_core`: 存储 Question 和 Answer (Core Truth)。
        - `algo_params`: 存储艾宾浩斯参数 (`easiness_factor`, `interval`, `repetition`, `next_review_date`, `review_count`).
        - `meta_info`: 来源文档 ID、创建时间、更新时间。
    - **持久层**: 使用 MyBatis-Plus 实现对 Card 的 CRUD。
- [ ] **Milvus 向量索引**:
    - **Collection 管理**: 创建 `knowledge_index` 集合，定义 Schema (id, embedding)。
    - **Embedding**: 集成 `langchain4j-google-ai-gemini`。
    - **向量操作**:
        - `insertVector`: 新增知识点向量。
        - `deleteVector`: 根据 CardID 删除旧向量。
        - `searchVector`: 根据 Query 检索 TopK 相似的 CardID。
- [ ] **业务逻辑实现 (`KnowledgeService`)**:
    - **新增知识**: 事务内完成 [MySQL Insert -> Milvus Insert]。
    - **关联更新 (重点)**: 实现 **"Milvus 删插 + MySQL 覆盖"** 策略。
        - 1. 接收更新请求 (CardID, 新 Answer, 新算法参数)。
        - 2. MySQL: 执行 Update 操作，覆盖旧 Answer，并更新 EF/Interval 等参数。
        - 3. Milvus: 执行 `delete(ref_id=CardID)` 然后 `insert(new_vector)`，确保语义索引是最新的。

## 🤖 第 4 阶段：Agent 大脑与调度 (Agent Service)
**目标**: 实现 **"宏观状态机 (FSM) + 微观 ReAct"** 的双层混合架构，确保辅导流程既严谨又灵活。

- [ ] **LLM 集成与配置**:
    - 配置 `ChatLanguageModel` (Gemini Pro) 并开启 `FunctionCalling` 能力。
    - **ReAct 模板设计**: 设计通用的 "Thought-Action-Observation" 提示词结构，并支持动态插入状态指令。
- [ ] **有限状态机 (FSM) 体系**:
    - **状态定义**:
        - `IDLE` (待机/闲聊): 默认状态，探测用户意图。
        - `TEACHING_PHASE` (深度辅导): 强制苏格拉底式教学引导。
        - `SUMMARY` (知识结晶): 负责调用工具生成 KnowledgeCard。
        - `REVIEW` (复习模式): 负责艾宾浩斯抽认卡问答。
    - **状态流转机制**:
        - 实现 `StateMachineTools`: 赋予 LLM 调用 `switchState(TargetState)` 的能力，实现语义驱动的状态跳转。
        - 实现 `StateContext`: 在 Session 中维护用户当前状态及上下文变量。
- [ ] **双层控制逻辑实现**:
    - **Prompt 动态注入**: 在 `AgentService` 中，根据当前 FSM 状态，实时组装 `SystemPrompt = BasePersona + CurrentStateInstruction`。
    - **ReAct 循环**: 确保 Agent 在每个状态内部都能自主调用 RAG 工具 (`searchKnowledge`) 解决具体问题。
- [ ] **核心业务流程**:
    - **深度辅导**: 识别用户困惑 -> 进入 TEACHING -> 多轮 ReAct 检索解释 -> 用户确认 -> 跳转 SUMMARY。
    - **知识结晶**: 调用 LLM 提炼对话精华 -> 生成 `KnowledgeCardDTO`。
    - **相似度检测**: 在结晶前调用 RAG Service `searchVector`，若存在高度相似 (Score > 0.85)，触发 **更新流程** 而非 **新增流程**。
- [ ] **艾宾浩斯调度器**:
    - **定时任务**: Quartz/Spring Task 每日扫描 MySQL `knowledge_card` 表。
    - **筛选逻辑**: `next_review_date <= today` AND `status != ARCHIVED`。
    - **推送机制**: 将待复习卡片推入待办队列，供用户登录时消费。

## 🌐 第 5 阶段：网关与交互层 (Gateway Service)
**目标**: 统一流量入口，处理鉴权与流式响应。

- [ ] **API 网关**:
    - `AuthController`: 登录/注册。
    - `ChatController`: 提供 SSE (Server-Sent Events) 接口，支持打字机效果。
- [ ] **Dubbo 消费**: 配置 `@DubboReference` 调用后端 Agent/User 服务。
- [ ] **安全上下文**: 解析 Token，透传 UserID 至下游 RPC 服务。

## ⚡ 第 6 阶段：高可靠性与异步化架构
**目标**: 使用 RocketMQ 解耦耗时操作，保证最终一致性。

- [ ] **知识入库异步化**:
    - **场景**: 用户在前端确认总结后，Agent 不阻塞等待 RAG 落库。
    - **流程**: Agent 发送 MQ 消息 -> RAG Service 消费消息 (执行 MySQL+Milvus 事务)。
- [ ] **复习推送异步化**:
    - **场景**: 定时任务扫描出 1000 个待复习知识点。
    - **流程**: 批量发送 MQ 消息 -> 用户的 "今日待办" 列表异步更新。

## 🚢 第 7 阶段：容器化与 CI/CD
- [ ] **Dockerization**: 编写 Dockerfile (基于 OpenJDK 21)。
- [ ] **Compose 编排**: 编写 `docker-compose-prod.yml`，定义资源限制与网络拓扑。

## 🧭 第 8 阶段：未来演进 (Future Optimization)
*(此阶段为长期规划，不包含在 MVP 版本中)*

- [ ] **混合检索 (Hybrid Search)**: 引入 Elasticsearch，实现 "Keyword + Vector" 并行召回与 RRF 融合。
- [ ] **知识图谱 (Knowledge Graph)**: 引入 Neo4j，构建知识点之间的显式关联，支持图谱多跳查询。
- [ ] **动态角色进化**: 实现 Agent 性格随用户交互历史动态演变的功能。
- [ ] **多模态生成**: 基于知识结构生成辅助记忆的图片。

