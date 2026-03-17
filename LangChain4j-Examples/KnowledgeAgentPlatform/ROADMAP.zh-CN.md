# Knowledge Agent Platform 路线图（中文）

本路线图以当前代码、配置和验证结果为准，不以愿景性描述作为完成依据。

> 扫描日期：2026-03-17  
> 项目路径：`F:\JavaProjects\AiProjects\LangChain4j-Examples\KnowledgeAgentPlatform`  
> 构建结果：`mvn -DskipTests compile` 已通过  
> 验证说明：`mvn test` 已通过；当前仓库已补齐面向配置的轻量验证测试，避免基础设施缺失直接阻塞基础回归

## 状态说明

- `[x] 已完成`：代码、配置或脚本已存在，且可在仓库中定位到依据
- `[-] 部分完成`：已经有可用基础，但距离完整交付还差关键环节
- `[ ] 计划中`：尚未形成完整实现
- `[~] 延后处理`：明确不进入近期主线

## 总体目标

围绕 `知识辅导 -> 知识沉淀 -> 复习调度` 的主链路，构建学习型 Knowledge Agent 平台。当前优先落地服务化 MVP，然后再逐步增强编排、调度和自动化能力。

## 核心流程

### 1. 对话主流程

`输入` -> `身份与画像` -> `意图识别` -> `状态选择（IDLE/TEACHING/REVIEW/SUMMARY）` -> `知识检索` -> `Tool/LLM 执行` -> `响应输出` -> `可选归档`

### 2. 知识归档流程

`总结文本` -> `清洗` -> `切片` -> `Embedding` -> `Milvus 入库` -> `KnowledgeCard 写入/更新` -> `标签与元数据`

### 3. 复习流程

`登录或调度触发` -> `到期卡片查询` -> `复习列表生成` -> `Agent 发问` -> `质量评分（0..5）` -> `SM-2 更新` -> `去重`

### 4. 事件流策略

- MVP 阶段：优先使用同步 Dubbo 调用
- 稳定阶段：将归档、提醒和分析逐步迁移到 RocketMQ 异步事件

## Phase 0：基础设施与运行基线

- [x] Maven 多模块结构：`common/api/user/rag/core/gateway`
- [x] `knowledge-agent-user` Flyway 初始化
- [x] `knowledge-agent-rag` Flyway 初始化
- [x] `user/rag/gateway` 的基础 Spring/Dubbo/Nacos 配置
- [x] `knowledge-agent-core` 的基础运行配置
- [x] Milvus dense/sparse 集合初始化
- [x] `docker-compose.yml` 中的 Nacos/Redis/MySQL/RocketMQ 基线
- [ ] Redis 连接与缓存接入
- [ ] RocketMQ Topic、生产者、消费者接入

完成判定：

- 标为 `[x]` 的项必须有明确文件或配置支撑
- Phase 0 只有在 Redis 和 RocketMQ 也形成最小可用接入后才算真正完成

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
- [ ] 跨服务契约测试
- [ ] 统一错误码/状态码规范

完成判定：

- 共享类型和服务接口都已具备源码实现
- 在补齐契约级测试前，该阶段仍未完全硬化

## Phase 2：User Service

- [x] `sys_user` 表结构
- [x] `SysUser` 实体与 `SysUserMapper`
- [x] `login`
- [x] `getUserProfile`
- [ ] JWT 鉴权
- [ ] 登录态模型
- [ ] 真正可用的 `UserController` 接口
- [ ] 更完整的用户偏好模型

完成判定：

- Dubbo Provider 能力已存在
- 对外鉴权和稳定会话机制仍未完成

## Phase 3：RAG Service

- [x] `knowledge_card` 表结构
- [x] `KnowledgeCard` 实体与 Mapper
- [x] Milvus hybrid schema 初始化
- [x] Dense + sparse + RRF 检索基础能力
- [x] `saveKnowledge`
- [x] `updateReviewStatus`
- [x] 标准检索接口基础：`searchKnowledge`
- [x] 待复习查询基础：`listPendingReviews`
- [-] `userId/summary/tags` 元数据已补齐，但 citation/source 响应结构仍未落地
- [ ] 文档导入流水线
- [ ] 标签过滤策略
- [ ] citation/source 返回结构
- [ ] 直答策略
- [ ] 批量导入工具

完成判定：

- 当前已覆盖归档写入、复习状态更新、知识检索、待复习查询
- 只有补齐导入、引用信息和更完整的检索响应契约后，RAG 才算完整

## Phase 4：Agent Core MVP

- [x] `ConversationState`
- [x] `StateContext`
- [x] Prompt 组装服务
- [-] 基于 `AgentToolRouter` 的 Tool 路由与 Dubbo 编排基础已落地
- [x] `AgentService` Provider 实现
- [x] User/RAG Dubbo 联动
- [-] 当前会话记忆为内存实现，持久化仍未完成
- [-] 当前状态流转为规则驱动，真实 LangChain4j 模型执行尚未接入

完成判定：

- `chat` 已实现，并可通过 Dubbo 调用
- `IDLE/TEACHING/REVIEW/SUMMARY` 四个基础状态已支持
- User profile + RAG + 响应编排已形成 MVP 闭环
- 在接入真实模型执行和持久化记忆前，本阶段仍属于“部分完成”

## Phase 5：Gateway / BFF

- [x] 启动类和基础配置
- [x] HTTP Controller
- [x] 最小 SSE 端点
- [x] Agent/User/RAG 路由聚合
- [-] 当前仍只有透传式登录，没有统一请求鉴权
- [ ] 对外 API 文档

完成判定：

- Gateway 已不再只是壳模块
- 在统一鉴权和 API 文档补齐前，本阶段仍视为“部分完成”

## Phase 6：Review 调度闭环

- [x] 到期复习卡片查询
- [ ] `ReviewTask` 模型
- [ ] 登录触发复习提醒
- [ ] 定时调度生成复习列表
- [ ] Redis 队列或去重缓存
- [x] 对话内 Agent 主动发起复习提问
- [x] 复习结果回写

完成判定：

- 当前已经具备“手动触发 review -> Agent 提问 -> 评分 -> 回写”的对话内复习闭环
- 在登录触发、定时调度和去重机制完成之前，本阶段仍未完成

## Phase 7：增强能力与长期方向

- [~] 知识图谱
- [~] 多模态生成
- [~] 动态角色进化
- [~] 复杂工作流编排
- [~] RocketMQ 全事件驱动架构

完成判定：

- 这些项不应阻塞当前 MVP 主线
- 只有在 Agent Core、Gateway 和 Review 流程都稳定后才进入优先级提升区

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
- 内存态会话记忆
- 对话内 review 流程

### 仅规划中

- `ReviewTask`
- `ReminderEvent`
- citation/source/direct-answer 响应结构
- Gateway 统一鉴权契约

## 下一阶段实施顺序

1. 补齐 Gateway 鉴权与外部 API 文档
2. 完成 review 的触发、调度和去重
3. 将当前规则驱动的 Agent 编排替换为真实 LangChain4j 模型/Tool 执行
4. 继续保持 Phase 7 延后

排序原因：

- 当前 MVP 主链路已经打通，接下来最有价值的是补强和自动化
- Review 目前缺少调度触发与去重
- Agent Core 虽然已经可用，但仍缺少真实模型执行能力
- Phase 7 仍然不应提前进入主线

## 下一阶段开发目标

当前下一阶段的重点，是把 Gateway 从“可用入口”推进到“稳定入口层”，同时为 review 流程补上触发基础。

### 目标 A：将 Gateway 从 MVP 入口提升为稳定的对外接入层

- 为除登录外的所有 Gateway 接口补齐统一鉴权
- 明确登录、对话、检索、复习接口之间的 token/header 约定
- 为当前已经暴露的 Gateway 接口补齐外部 API 文档
- 统一 Gateway 边界上的请求、响应和错误处理方式

### 目标 B：让 review 从手动调用能力演进为“可触发”的能力

- 增加登录后立即查询待复习项的能力
- 定义初版 `ReviewTask` 模型或等价的响应契约
- 预留定时调度生成复习任务的入口
- 先预留基于 Redis 的去重挂点，但暂不实现完整队列体系

### 目标 C：保持 Agent Core 稳定，同时延后真实模型执行接入

- 保留当前基于 Dubbo 的规则编排链路作为兜底实现
- 不把 Phase 7 的图谱、多模态、复杂工作流提前拉入当前阶段
- 将 LangChain4j 真实运行时接入放在 Gateway/review 加固之后

### 下一阶段完成判定

- 除登录外的 Gateway 接口都受统一鉴权保护
- 项目对外提供清晰可用的 login/chat/search/review API 文档
- 用户登录后可以立即触发待复习项查询
- review 的触发契约和调度契约已经清晰到足以进入下一阶段实现
- `mvn -DskipTests compile` 持续通过，定向单测持续通过

## 当前完成依据

- User 侧：`SysUser`、`SysUserMapper`、`SysUserServiceImpl`、User Flyway 脚本
- RAG 侧：`KnowledgeCard`、`KnowledgeCardMapper`、`RagServiceImpl`、`MilvusConfig`、`V2__enhance_knowledge_card.sql`
- Agent Core 侧：`ConversationState`、`StateContext`、`AgentServiceImpl`、`AgentPromptService`、`DubboAgentToolRouter`
- Gateway 侧：`GatewayAuthController`、`GatewayAgentController`、`GatewayRagController`
- 验证依据：`AgentServiceImplTest`、`GatewayAgentControllerTest`、`knowledge-agent-user/ApplicationTest`、`knowledge-agent-rag/ApplicationTest`、`knowledge-agent-gateway/KnowledgeAgentGatewayApplicationTest`

## 维护规则

- 只有在代码、配置、脚本或验证结果支持时，才允许标记为 `[x]`
- 不允许把 roadmap 设计稿或 README 叙述直接当成完成依据
- 每次更新路线图时，都要同步检查 `已实现 / 部分完成 / 仅规划中` 三层边界是否仍然准确
