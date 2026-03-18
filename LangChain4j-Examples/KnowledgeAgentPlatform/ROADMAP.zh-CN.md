# Knowledge Agent Platform 路线图（中文）

本路线图以当前代码、配置和验证结果为准，不以愿景性描述作为完成依据。

> 扫描日期：2026-03-18  
> 项目路径：`F:\JavaProjects\AiProjects\LangChain4j-Examples\KnowledgeAgentPlatform`  
> 构建结果：`mvn -DskipTests compile` 已通过  
> 验证说明：关键模块定向测试已通过，包括 Gateway review 链路、RAG review 持久化、公共契约与 User/Agent 相关测试

## 当前结论

- `Phase 0-5`：已完成
- `Phase 6`：功能闭环已具备，但仍属于部分完成
- `Phase 7`：明确延后

原因说明：

- review 调度、去重、持久化、最近批次回查、状态回写都已经落地。
- 当前剩余缺口不是“有没有 review 闭环”，而是 Redis 去重与批次缓存本身仍然处于部分完成状态，缓存/持久化边界和异常恢复能力还需要继续收口。

## 状态说明

- `[x] 已完成`：代码、配置、脚本或验证结果可支撑该项已落地
- `[-] 部分完成`：已有可运行基础，但离完整交付仍有关键差距
- `[ ] 计划中`：尚未形成完整实现
- `[~] 延后处理`：明确不进入当前主线

## 总体目标

围绕 `知识辅导 -> 知识沉淀 -> 复习调度` 主链路，构建学习型 Knowledge Agent 平台。当前优先把服务化 MVP 主链路做稳，再逐步增强编排、调度和自动化能力。

## 核心流程

### 对话流程

`输入` -> `身份与画像` -> `意图识别` -> `状态选择(IDLE/TEACHING/REVIEW/SUMMARY)` -> `知识检索` -> `Tool/LLM 执行` -> `响应输出` -> `可选归档`

### 知识归档流程

`总结文本` -> `清洗` -> `切片` -> `Embedding` -> `Milvus 入库` -> `KnowledgeCard 写入/更新` -> `标签与元数据`

### 复习流程

`登录或调度触发` -> `到期卡片查询` -> `复习列表生成` -> `Agent 发问` -> `质量评分(0..5)` -> `SM-2 更新` -> `去重`

### 事件流策略

- MVP 阶段：优先采用同步 Dubbo 调用
- 稳定化阶段：逐步把归档、提醒、审计迁移到 RocketMQ 异步事件

## Phase 0：基础设施与运行基线

- [x] Maven 多模块结构：`common/api/user/rag/core/gateway`
- [x] `knowledge-agent-user` Flyway 初始化
- [x] `knowledge-agent-rag` Flyway 初始化
- [x] `user/rag/gateway` 的基础 Spring/Dubbo/Nacos 配置
- [x] `knowledge-agent-core` 的基础运行配置
- [x] Milvus dense/sparse 集合初始化
- [x] `docker-compose.yml` 中的 Nacos/Redis/MySQL/RocketMQ 基线
- [-] Redis 驱动的缓存、去重和降级链路
- [x] RocketMQ Topic、生产者、消费者最小接入骨架

完成判定：

- 标记为 `[x]` 的项必须有仓库中的代码或配置支撑
- RocketMQ 最小事件通道已接入知识归档和 review 批次事件
- Redis 已接入 review 去重、批次缓存和会话存储降级，但仍需更多运行时验证，因此本项继续保留为部分完成

## Phase 1：公共契约与通用能力

- [x] `Result`
- [x] `BizException`
- [x] `GlobalExceptionHandler`
- [x] `EbbinghausUtils`
- [x] `KnowledgeDTO`
- [x] `ChatRequest`
- [x] `UserLoginRequest`
- [x] `AgentService`
- [x] `RagService`
- [x] `UserService`
- [x] 跨服务契约测试
- [x] 统一错误码/状态码规范

完成判定：

- 共享类型和服务接口都已经存在
- JWT 工具、错误码、契约测试与 review 契约 DTO 已落地

## Phase 2：User Service

- [x] `sys_user` 表结构
- [x] `SysUser` 实体与 `SysUserMapper`
- [x] `login`
- [x] `getUserProfile`
- [x] JWT 鉴权
- [x] 登录态模型
- [x] 真实可用的 `UserController` HTTP 接口
- [x] 结构化用户偏好模型

完成判定：

- Dubbo Provider 能力已存在
- 用户登录、画像读取和结构化偏好能力已可运行
- Token 撤销和长会话治理仍属于后续增强，而不是本阶段阻塞项

## Phase 3：RAG Service

- [x] `knowledge_card` 表结构
- [x] `KnowledgeCard` 实体与 Mapper
- [x] Milvus hybrid schema 初始化
- [x] Dense + sparse + RRF 检索基础
- [x] `saveKnowledge`
- [x] `updateReviewStatus`
- [x] `searchKnowledge`
- [x] `listPendingReviews`
- [x] `userId/summary/tags/source` 元数据持久化
- [x] 文档导入流水线
- [x] 标签过滤能力
- [x] citation/source 返回结构
- [x] 轻量 direct-answer 能力
- [x] 小批量同步导入能力

完成判定：

- 已覆盖知识归档、复习更新、检索和待复习查询
- 导入、标签过滤和 richer retrieval response 已补齐

## Phase 4：Agent Core MVP

- [x] `ConversationState`
- [x] `StateContext`
- [x] Prompt 组装服务
- [-] `AgentToolRouter` 与 Dubbo 编排基础
- [x] `AgentService` Provider 实现
- [x] User/RAG Dubbo 联动
- [-] Redis 优先、内存降级的会话存储
- [-] 教学/总结场景的 LangChain4j 运行时集成与规则回退

完成判定：

- `chat` 已实现并可通过 Dubbo 调用
- `IDLE/TEACHING/REVIEW/SUMMARY` 四个基础状态已支持
- 当前仍属于部分完成，因为完整 tool-driven 编排和更广泛的 Redis 运行时验证尚未完成

## Phase 5：Gateway / BFF

- [x] 启动类和基础配置
- [x] HTTP Controller
- [x] 最小 SSE 端点
- [x] Agent/User/RAG 路由聚合
- [x] 统一 Bearer Token 鉴权
- [x] 对外 API 文档

完成判定：

- Gateway 已不是空壳模块
- 当前阶段的入口层加固目标已完成

## Phase 6：Review 调度闭环

- [x] 到期复习卡片查询
- [x] `ReviewTask` 模型
- [x] 登录触发复习提醒
- [x] 定时调度生成复习列表
- [-] Redis 去重、持久化批次、调度循环和事件发布
- [x] 对话内 Agent 主动发起复习提问
- [x] 复习结果回写

完成判定：

- 手动触发 review -> Agent 提问 -> 评分 -> 回写 的闭环已经存在
- 登录触发、定时调度、批次持久化与最近批次回查都已经存在
- 当前仍为部分完成，因为路线图中 Redis 相关能力本身仍标记为部分完成，review 运行时仍未收敛成稳定的 Redis 驱动层

## Phase 7：增强能力与长期方向

- [~] 知识图谱
- [~] 多模态生成
- [~] 动态角色进化
- [~] 复杂工作流编排
- [~] RocketMQ 全事件驱动架构

完成判定：

- 这些项不应阻塞当前 MVP 主线
- 只有在 Agent Core、Gateway 和 Review 流程稳定后才进入优先级提升区

## 接口分层规则

### 代码已实现

- `UserService.login`
- `UserService.getUserProfile`
- `RagService.saveKnowledge`
- `RagService.updateReviewStatus`
- `RagService.searchKnowledge`
- `RagService.listPendingReviews`
- `AgentService.chat`
- `AgentService.switchState`
- `KnowledgeDTO`
- `ChatRequest`
- `UserLoginRequest`
- `ConversationState`
- `StateContext`
- Gateway HTTP 接口
- Gateway SSE 接口

### 已定义但仍不完整

- `AgentToolRouter`
- Redis 优先的会话记忆体系
- 对话内 review 编排

### 仅规划中

- 更强的多实例 review 投递语义
- 异步审计与重试工作流
- Phase 7 增强能力

## 下一阶段实施顺序

1. 收口 Redis 去重、批次缓存与持久化回查之间的运行边界
2. 增强 review 批次生成、缓存失效、状态回写失败等异常场景下的恢复与观测能力
3. 将 LangChain4j 从教学/总结入口扩展到更完整的 Tool 驱动编排
4. 继续保持 Phase 7 延后

## API 测试案例

### 登录

1. 调用 `POST /api/auth/login`
预期结果：返回 `accessToken`、`pendingReviewCount` 和 `pendingReviewTasks`。

### 查询待复习批次

1. 携带 `Authorization: Bearer <accessToken>` 调用 `GET /api/reviews/pending?limit=5`
预期结果：返回 `batchId`、`triggerSource=MANUAL` 和任务列表。

### 查询最近一次调度批次

1. 携带 `Authorization: Bearer <accessToken>` 调用 `GET /api/reviews/scheduled/latest`
预期结果：存在调度批次时返回最近一次 `SCHEDULED` 批次；不存在时返回空批次结构而不是报错。

2. 在 Redis 不可用或主动绕过 Redis 的情况下重复验证该接口
预期结果：接口仍能通过持久化结果或当前运行时降级路径返回结果，且不会因为缓存不可用直接报错。

### 回写复习结果

1. 携带 `Authorization: Bearer <accessToken>` 调用 `PATCH /api/reviews/status`
2. 传入有效的 `knowledgeId` 和 `quality`
预期结果：知识卡复习参数被更新，对应 review 任务状态被标记为已完成。

### 检索知识

1. 携带 `Authorization: Bearer <accessToken>` 调用 `GET /api/rag/search?query=sm2&limit=3&tags=memory`
预期结果：返回结果中可包含 `source`、`citation`、`directAnswer`、`matchedSegment`，且 `tags` 过滤生效。

### 对话接口

1. 调用 `POST /api/agent/chat`
2. 调用 `GET /api/agent/chat/stream?prompt=review`
预期结果：普通对话返回统一响应结构，SSE 至少返回一个 `message` 事件。

## 当前证据指针

- User 侧：`SysUser`、`SysUserMapper`、`SysUserServiceImpl`、用户 Flyway 脚本
- RAG 侧：`KnowledgeCard`、`KnowledgeCardMapper`、`ReviewTaskRecord`、`ReviewTaskRecordMapper`、`RagServiceImpl`、`V3__add_knowledge_source.sql`、`V4__add_review_task_record.sql`
- Agent Core 侧：`ConversationState`、`StateContext`、`AgentServiceImpl`、`AgentPromptService`、`DubboAgentToolRouter`、`LangChain4jAgentRuntime`、`RedisConversationStore`
- Gateway 侧：`GatewayAuthController`、`GatewayAgentController`、`GatewayRagController`、`GatewayReviewController`、`ReviewTaskDispatcher`、`ReviewTaskScheduler`
- 文档：`docs/gateway-api.zh-CN.md`

## 维护规则

- 只有在代码、配置、脚本或验证结果支持时，才允许标记为 `[x]`
- 不允许把路线图设想或 README 叙述直接当作完成依据
- 每次更新路线图时，都要同步检查 `已实现 / 部分完成 / 仅规划中` 三层边界是否仍然准确
