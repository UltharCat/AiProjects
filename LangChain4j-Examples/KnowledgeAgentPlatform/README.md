# Enterprise Intelligent Knowledge Agent (企业级智能知识助手)

基于 LangChain4j 构建的企业级 AI 知识管理与学习助手应用。本项目旨在打造一个具备深度学习指导、动态角色进化、多模态知识图谱生成及符合艾宾浩斯遗忘曲线记忆功能的智能体系统。

## 🎯 项目愿景 (Vision)

打造一个不仅仅是简单的问答机器人，而是用户的**专属知识伴侣**。它能够：
1.  **深度辅导**：通过多轮对话引导用户理解复杂知识点，并协助总结结论。
2.  **动态成长**：Agent 角色随用户交互习惯动态进化，形成独特的性格与沟通方式。
3.  **科学记忆**：结合**知识图谱**与**艾宾浩斯遗忘曲线**，主动管理用户的知识留存。

## 🚧 当前进度 (Current Progress)

截至 2026/02/15，项目开发进展如下：
- [x] **基础设施**: Maven 多模块架构、MySQL 数据库及 Flyway 脚本。
- [x] **API 定义**: 完成 `KnowledgeDTO`, `AgentService`, `RagService`, `UserService` 接口契约。
- [x] **User Service**: 完成用户表设计、`SysUser` 实体及画像获取逻辑。
- [x] **RAG Service**: 完成知识切片存储、Milvus 向量检索逻辑及 MySQL 知识卡片管理。
- [ ] **Agent Service**: 正在开发中（核心状态机与 LLM 集成待实现）。
- [ ] **Gateway**: 待完善。

## 🛠 技术栈 (Tech Stack)

*   **Core Framework**: Spring Boot 3.2+ (JDK 21 虚拟线程 - Virtual Threads)
*   **Microservices Framework**: **Apache Dubbo 3.x** (Triple Protocol) - 高性能 RPC 通信。
*   **Registry & Config**: **Alibaba Nacos 3.x** - 统一服务注册与发现、分布式动态配置中心。
*   **AI Framework**: LangChain4j (ReAct Agent, RAG, Tooling)
*   **LLM Providers**:
    *   **Chat Model**: Google Gemini (via `langchain4j-google-ai-gemini`)
    *   **Embedding Model**: Google Gemini (via `langchain4j-google-ai-gemini`)
    *   *(Future/Opt)* **OpenAI**: 计划在优化阶段引入，提供更强的 Embedding 或备用对话模型。
*   **Storage**:
    *   **MySQL**: 这里的用户管理 (MyBatis-Plus) 与基础业务数据。
    *   **Redis**: 短期记忆缓存 (Sliding Window)、用户会话状态。
    *   **Milvus**: 向量数据库，存储 Embedding，提供语义检索。
    *   *(Future/Opt)* **Elasticsearch**: 计划在优化阶段引入，提供 BM25 全文检索。
    *   *(Future/Opt)* **Neo4j**: 计划在优化阶段引入，构建知识图谱。
*   **Messaging**: RocketMQ 5.x (系统解耦、事务消息保障数据一致性)。
*   **Architecture**: Dubbo Microservices (Restless/Headless Back-end).

## 🏗 系统架构设计 (System Architecture)

系统采用基于 **Dubbo** 的微服务架构，各服务间通过 RPC (Triple协议) 进行高性能通信。对外通过 **Gateway/BFF** 层暴露标准的 HTTP/Restful 接口。

### 1. 核心服务划分 (Service Modules)
*   **Gateway Service (Web/BFF)**: 
    *   作为系统的统一入口 (Dubbo Consumer)。
    *   处理 HTTP 请求，进行鉴权，并调用后端 RPC 服务。
    *   支持 SSE (Server-Sent Events) 流式响应，适配 LLM 打字机效果。
*   **User Service (Provider)**:
    *   负责用户领域模型。
    *   功能：账号管理、角色绑定、偏好设置。
    *   技术：MySQL + MyBatis-Plus。
*   **Agent Service (Provider)**: 
    *   系统的核心大脑。
    *   功能：维护状态机 (FSM)、Prompt工程、ReAct 任务调度、文生图调用。
    *   通信：通过 Dubbo 调用 RAG Service 获取知识，调用 User Service 获取画像。
*   **RAG Service (Provider)**:
    *   知识检索与存储中心。
    *   功能：向量化 (Embedding)、语义检索。
    *   技术：Milvus。

### 2. 记忆与知识库设计 (Storage Strategy)
采用 **分层记忆体系**，明确区分"客观文档"与"主观认知"：

*   **MySQL (Core Truth)**: 
    *   存储所有 **Knowledge Card** 的完整内容、元数据及艾宾浩斯算法参数 (`review_count`, `ef`, `interval` 等)。
    *   **关联更新策略**: 当发生深度辅导导致知识点进化时，直接 **覆盖 (Overwrite)** MySQL 中的 `answer` 字段，代表当前最新的认知状态。
*   **Milvus (Semantic Index)**: 
    *   存储文本的 Embedding 向量。
    *   **静态文档库**: 存储参考书籍/文档切片，策略为 **只增不改 (Append-only)**。
    *   **内化知识库**: 存储 Knowledge Card 向量，策略为 **删旧插新 (Delete Old & Insert New)**，确保向量检索总是命中最新的知识形态。
*   **Redis (Short-term)**: 
    *   保存最近的对话上下文 (Sliding Window)，确保多轮对话流畅。

### 3. Agent 核心机制
*   **ReAct 架构**: Agent 具备"思考-行动"循环能力，解析用户需求后调度不同工具或子 Agent。
*   **状态机 (State Machine)**: 严格管理会话状态（闲聊 -> 学习 -> 总结 -> 确认 -> 归档），防止 Prompt 漂移。
*   *(Future/Opt)* **动态角色进化**:
    *   计划在优化阶段实现。根据历史对话的情感、语气偏好，动态调整 Agent 的 Prompt 配置，使其逐渐"适应"用户。

### 4. 高并发与一致性
*   **虚拟线程 (Virtual Threads)**: 全链路启用 JDK 21 虚拟线程，大幅提升 RPC 调用和 IO 密集型任务吞吐。
*   **RocketMQ 事务消息**:
    *   确保 知识点入库、图谱更新、统计分析 等操作的数据一致性。
    *   **流程**: Agent Service 确认知识点 -> 发送 Half Msg -> 扣减用户Token/记录Log (Local Tx) -> Commit Msg -> RAG Service 消费消息并写入 Milvus。

## 💡 核心业务流程 (Core Features)

### 1. 深度学习模式 (Deep Learning Mode)
*   **交互**: 用户询问不懂的知识点 -> Agent 多轮解释/举例 -> 用户/Agent 总结结论。
*   **归档**: 结论被确认为"知识点"，经压缩后存入向量库与图谱。

### 2. 艾宾浩斯记忆提醒 (Ebbinghaus Reminders)
*   **触发机制**: 用户登录/应用启动时。
*   **逻辑**: User Service 登录成功后，异步触发 Agent Service 计算遗忘曲线，查询 RAG Service 获取相关知识点。
*   **频率控制**: 低频提醒 (同一知识点单日不重复)。
*   **主动交互**: Agent 主动发起对话："你还记得关于 [XXX] 的这个结论吗？"

### 3. 多模态图谱生成 (Multimodal Graph Generaton)
*   **场景**: 在进行艾宾浩斯提醒时。
*   **功能**: 调用文生图模型 (Text-to-Image)。
*   **内容**: 基于 Neo4j 中的知识点结构，生成一张表示知识点关系的逻辑图谱图片 (辅助记忆)。

## 🧩 关键概念详解

### 1. 状态机 (State Machine)
引入 FSM 管理复杂的"教学-总结-确认"流程，避免 Prompt 漂移。
*   **States**: 
    *   `IDLE`: 空闲状态。
    *   `TEACHING_EXPLAIN`: 概念讲解状态。
    *   `TEACHING_QUIZ`: 互动提问状态。
    *   `SUMMARIZING`: 知识总结状态。
    *   `CONFIRMING`: 用户确认状态。
    *   `RECORDING`: 知识入库状态。
*   **Transitions**: 基于用户意图 (Intent Classification) 和对话轮次触发状态流转。

### 2. 事务消息流程 (RocketMQ)
利用 RocketMQ 的 Transactional Message 特性：
1.  **Sender (Agent Service)**: 发送 `Half Message` (Topic: `KNOWLEDGE_Confirm`).
2.  **Local Transaction**: 记录用户学习日志到 MySQL。
3.  **Confirm**: 提交消息。
4.  **Receiver (RAG Service)**: 监听 Topic，收到消息后，幂等地将知识写入 Neo4j 和 PGVector。

## 📂 项目结构规划 (Dubbo Structure)

标准的 Dubbo 分层架构：

```
KnowledgeAgentPlatform/
├── knowledge-agent-common/     # 公共模块：Utils, Constants, Base Classes
├── knowledge-agent-api/        # 接口模块：存放 Dubbo Interface (Service), DTOs, Enums
├── knowledge-agent-user/       # 用户服务 (Provider): MySQL, User Logic -> Implements UserService
├── knowledge-agent-rag/        # RAG服务 (Provider): Milvus -> Implements RagService
├── knowledge-agent-core/       # Agent核心服务 (Provider): LLM, FSM, RocketMQ Producer -> Implements AgentService
├── knowledge-agent-gateway/    # 网关/Web层 (Consumer): Spring Boot Web, Controller, SSE -> Consumes Dubbo Services
├── pom.xml
└── README.md
```
