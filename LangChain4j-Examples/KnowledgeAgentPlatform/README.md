# Enterprise Intelligent Knowledge Agent (企业级智能知识助手)

基于 LangChain4j 构建的企业级 AI 知识管理与学习助手应用。本项目旨在打造一个具备深度学习指导、动态角色进化、多模态知识图谱生成及符合艾宾浩斯遗忘曲线记忆功能的智能体系统。

## 🎯 项目愿景 (Vision)

打造一个不仅仅是简单的问答机器人，而是用户的**专属知识伴侣**。它能够：
1.  **深度辅导**：通过多轮对话引导用户理解复杂知识点，并协助总结结论。
2.  **动态成长**：Agent 角色随用户交互习惯动态进化，形成独特的性格与沟通方式。
3.  **科学记忆**：结合**知识图谱**与**艾宾浩斯遗忘曲线**，主动管理用户的知识留存。

## 🛠 技术栈 (Tech Stack)

*   **Core Framework**: Spring Boot 3.2+ (JDK 21 虚拟线程 - Virtual Threads)
*   **Microservices Framework**: **Apache Dubbo 3.x** (Triple Protocol) - 高性能 RPC 通信。
*   **Registry & Config**: **Alibaba Nacos 2.x** - 统一服务注册与发现、分布式动态配置中心 (通过 `bootstrap.yml` 引导)。
*   **AI Framework**: LangChain4j (ReAct Agent, RAG, Tooling)
*   **LLM Providers**:
    *   **国内**: 通义千问 (DashScope)
    *   **国外**: Google Gemini (通过 OpenAI 接口兼容协议接入)
*   **Storage**:
    *   **MySQL**: 这里的用户管理 (MyBatis-Plus) 与基础业务数据。
    *   **Redis**: 短期记忆缓存 (Sliding Window)、用户会话状态。
    *   **PostgreSQL (PGVector)**: 向量数据库 (混合检索)。
    *   **Neo4j**: 知识图谱 (存储知识点结构与关系)。
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
    *   功能：向量化 (Embedding)、混合检索 (Keyword + Vector)、图谱查询 (Cypher)。
    *   技术：PGVector + Neo4j。

### 2. 记忆与知识库设计 (Hybrid Memory & Knowledge Graph)
采用 **混合记忆体系**：
*   **短期记忆 (Short-term)**: 存储于 **Redis**。保存最近的对话上下文，确保多轮对话流畅。
*   **长期记忆 (Long-term)**:
    *   **非结构化**: 模型总结压缩后的对话精华，存入 **PGVector**。
    *   **结构化**: 关键知识点提取为实体与关系，存入 **Neo4j** (知识图谱)。
*   **RAG 策略**:
    *   结合 **关键字检索 (Keyword/BM25)** 与 **向量检索 (Vector/Embedding)**。
    *   Agent 回答时，不仅参考当前角色设定的知识，更优先检索**用户个人知识库**及**可复用的公共知识**。

### 3. Agent 核心机制
*   **ReAct 架构**: Agent 具备"思考-行动"循环能力，解析用户需求后调度不同工具或子 Agent��
*   **动态角色进化**:
    *   用户登录时绑定专属角色。
    *   根据历史对话的情感、语气偏好，动态调整 Agent 的 Prompt 配置，使其逐渐"适应"用户。
*   **状态机 (State Machine)**: 严格管理会话状态（闲聊 -> 学习 -> 总结 -> 确认 -> 归档）。

### 4. 高并发与一致性
*   **虚拟线程 (Virtual Threads)**: 全链路启用 JDK 21 虚拟线程，大幅提升 RPC 调用和 IO 密集型任务吞吐。
*   **RocketMQ 事务消息**:
    *   确保 知识点入库、图谱更新、统计分析 等操作的数据一致性。
    *   **流程**: Agent Service 确认知识点 -> 发送 Half Msg -> 扣减用户Token/记录Log (Local Tx) -> Commit Msg -> RAG Service 消费消息并写入 Neo4j/PGVector。

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
*   **States**: `IDLE`, `LEARNING`, `SUMMARIZING`, `CONFIRMING`, `RECORDING`.
*   **Transitions**: 基于用户意图 (Intent Classification) 触发状态流转。

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
├── knowledge-agent-rag/        # RAG服务 (Provider): Neo4j, PGVector -> Implements RagService
├── knowledge-agent-core/       # Agent核心服务 (Provider): LLM, FSM, RocketMQ Producer -> Implements AgentService
├── knowledge-agent-gateway/    # 网关/Web层 (Consumer): Spring Boot Web, Controller, SSE -> Consumes Dubbo Services
├── pom.xml
└── README.md
```
