oshi# 开发路线图：知识 Agent 平台 (Knowledge Agent Platform)

本路线图指导企业级智能知识 Agent 的开发。根据架构设计，采用 **"MySQL (Core Truth) + Milvus (Semantic Index)"** 的双层记忆体系，并明确了各阶段的实施细节。

> **项目根目录**: `F:\JavaProjects\AiProjects\LangChain4j-Examples\KnowledgeAgentPlatform`
> **核心数据库**: `knowledge_agent` (MySQL)

## 🏁 第 0 阶段：环境准备与基础设施
**目标**: 搭建本地开发环境，确保微服务基础设施就绪。

- [x] **项目结构**: 基于 Maven 多模块构建 (`common`, `api`, `user`, `core`, `rag`, `gateway`)。
- [x] **服务注册与配置中心 (Nacos)**:
    - 部署 Nacos (Stand-alone/Cluster)。
    - namespace: `dev` / `prod`.
- [x] **数据存储设施**:
    - **MySQL 8.0+**: 创建数据库 `knowledge_agent`。
    - **Flyway**: 
      - `knowledge-agent-rag`: 管理核心知识表 (`V1__init_rag_schema.sql`).
      - `knowledge-agent-user`: 管理用户表 (`V1__init_user_schema.sql`).
    - **Redis 7.x**: 用于 Session 缓存。
    - **Milvus 2.4+**: 部署向量数据库 (Standalone)，端口 `19530`。
    - **RocketMQ 5.x**: Topic: `topic-knowledge-sync`, `topic-review-push`。

## 🏗 第 1 阶段：核心契约 (API & Common)
**目标**: 定义严格的接口契约，确保 Dubbo 服务间调用的类型安全。

- [x] **公共组件 (`knowledge-agent-common`)**:
    - **统一响应**: `Result<T>` (int code, String message, T data).
    - **异常体系**: `BussinessException` (extends RuntimeException).
- [x] **API 定义 (`knowledge-agent-api`)**:
    - **DTO 模型**:
        - `KnowledgeDto`:
            ```java
            Long id;              // 向量ID (关联 Milvus 中的向量记录，代表切片后的知识点)
            String summary;      // 对话知识总结
            Set<String> tags;        // 标签集合 (用于混合检索或过滤)
            Double easinessFactor;// EF值 (2.5)
            Integer intervalDays; // 间隔 (1)
            Integer repetition;   // 重复次数 (0)
            LocalDateTime nextReviewDate; // 下次复习时间
            ```
    - **Dubbo 接口**:
        - `AgentService` (Agent 核心):
            - `Result<String> chat(Long userId, String query);` // 对话入口
            - `Result<Void> switchState(Long userId, String targetState);` // 状态机流转
        - `RagService` (知识核心):
            - `Result<Boolean> saveKnowledge(KnowledgeDto dto);` // 存入(Mysql+Milvus)
            - `Result<List<KnowledgeDto>> search(String query, int topK, double minScore);` // 检索
            - `Result<Void> updateReviewStatus(Long id, Double newEf, Integer newInterval, LocalDateTime nextReview);` // 更新记忆参数
        - `UserService` (用户画像):
            - `Result<UserProfileDto> getProfile(Long userId);` // 获取偏好

## 👤 第 2 阶段：用户身份与画像 (User Service)
**目标**: 建立用户体系，逻辑位于 `knowledge-agent-user` 模块。

- [x] **Mysql 表设计 (`sys_user`)**:
    - **脚本位置**: `knowledge-agent-user/src/main/resources/db/migration/V1__init_user_schema.sql`
    - **字段**:
        - `id`: BIGINT (PK)
        - `username`: VARCHAR(50)
        - `password_hash`: VARCHAR(100)
        - `learning_style`: VARCHAR(20) (Enum: SOCRATIC, DIRECT, ELABORATE) -> 偏好风格
- [x] **Service 实现**:
    - `UserServiceImpl`: 实现 `getProfile`，返回用户 ID 及 `learning_style`。

## 🧠 第 3 阶段：知识存储与 RAG 核心 (RAG Service)
**目标**: 实现双层记忆更新策略。数据库连接至 `knowledge_agent`。

- [x] **MySQL 表结构 (`knowledge_card`)**:
    - **脚本位置**: `knowledge-agent-rag/src/main/resources/db/migration/V1__init_rag_schema.sql`
    - **必须字段**:
        - `id`: BIGINT (PK, Snowflake)
        - `question`: TEXT
        - `answer`: LONGTEXT
        - `easiness_factor`: DOUBLE (Default 2.5)
        - `interval_days`: INT (Default 1)
        - `repetition`: INT (Default 0)
        - `next_review_date`: DATETIME
        - `tags`: JSON
        - `deleted`: TINYINT
- [x] **Milvus Schema**:
    - Collection: `knowledge_index`
    - Fields:
        - `id`: Int64 (Primary, Non-Auto, == MySQL ID)
        - `vector`: FloatVector (Dim 768)
        - `metadata`: VarChar (JSON String)
    - Index: `HNSW` (M=16, efConstruction=256), Metric: `COSINE`.
- [x] **RagService 业务逻辑**:
    - **保存逻辑 (`saveKnowledge`)**:
        1. 检查相似度: `search(dto.question)` -> if score > 0.85 -> 视为更新。
        2. 若新增: MySQL Insert -> Milvus Insert.
        3. 若更新: MySQL Update (Content + EF) -> Milvus Delete (by ID) -> Milvus Insert (New Vector).

## 🤖 第 4 阶段：Agent 大脑与调度 (Agent Service)
**目标**: 实现 **"宏观状态机 (FSM) + 微观 ReAct"** 的双层混合架构，确保辅导流程既严谨又灵活。

- [ ] **LLM 集成与配置**:
    - 配置 `ChatLanguageModel` (Gemini Pro) 并开启 `FunctionCalling` 能力。
    - **ReAct 模板设计**: 设计通用的 "Thought-Action-Observation" 提示词结构，并支持动态插入状态指令。
- [ ] **有限状态机 (FSM) 体系**:
    - **状态定义**:
        - `IDLE` (待机/闲聊): 默认状态，探测用户意图。
        - `TEACHING_PHASE` (深度辅导): 强制苏格拉底式教学引导。
        - `SUMMARY` (知识结晶): 负责调用工具生成 KnowledgeDto。
        - `REVIEW` (复习模式): 负责艾宾浩斯抽认卡问答。
    - **状态流转机制**:
        - 实现 `StateMachineTools`: 赋予 LLM 调用 `switchState(TargetState)` 的能力，实现语义驱动的状态跳转。
        - 实现 `StateContext`: 在 Session 中维护用户当前状态及上下文变量。
- [ ] **双层控制逻辑实现**:
    - **Prompt 动态注入**: 根据当前 FSM 状态，实时组装 `SystemPrompt = BasePersona + CurrentStateInstruction`。
    - **ReAct 循环**: 确保 Agent 在每个状态内部都能自主调用 `RagService` 工具 (`searchKnowledge`) 解决具体问题。
- [ ] **核心业务流程**:
    - **深度辅导**: 识别用户困惑 -> 进入 TEACHING -> 多轮 ReAct 检索解释 -> 用户确认 -> 跳转 SUMMARY。
    - **知识结晶**: 调用 LLM 提炼对话精华 -> 生成 `KnowledgeDto`。
    - **相似度检测**: 在结晶前调用 `RagService` 的 `searchVector`，若存在高度相似 (Score > 0.85)，触发 **更新流程** 而非 **新增流程**。

## ⚡ 第 6 阶段：任务调度 (复习推送)
**目标**: 主动提醒复习。

- [ ] **调度器**:
    - 每天 08:00 执行。
    - SQL: `SELECT * FROM knowledge_card WHERE next_review_date <= NOW() AND deleted = 0`.
- [ ] **推送**:
    - 将待复习 ID 存入 Redis List `user:review:list:{date}`。
    - 用户登录时，`ChatController` 读取 List 并通过 SSE 发送："今日有 5 个知识点需要复习"。

## 🧭 第 7 阶段：未来演进 (Future Optimization)
*(此阶段为长期规划，不包含在 MVP 版本中)*

- [ ] **知识文档增量更新**: 目前采用只增不更新的策略，接下来需要实现RAG知识文档的增量更新。
- [ ] **混合检索 (Hybrid Search)**: 引入 Elasticsearch，实现 "Keyword + Vector" 并行召回与 RRF 融合。
- [ ] **知识图谱 (Knowledge Graph)**: 引入 Neo4j，构建知识点之间的显式关联，支持图谱多跳查询。
- [ ] **动态角色进化**: 实现 Agent 性格随用户交互历史动态演变的功能 (基于 `interaction_history`)。
- [ ] **多模态生成**: 基于知识结构生成辅助记忆的图片。

