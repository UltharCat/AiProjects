# Knowledge Agent Platform Roadmap

本路线图基于当前代码、配置与可编译结果校准，不以 README 中的愿景描述作为完成依据。

> 扫描日期：2026-03-17  
> 项目目录：`F:\JavaProjects\AiProjects\LangChain4j-Examples\KnowledgeAgentPlatform`  
> 构建结论：`mvn -DskipTests compile` 通过  
> 测试结论：`mvn test` 受本地 `Nacos`/外部依赖未启动影响，不能作为功能完成判定依据

## 状态说明

- `[x] 已完成`：代码、配置或脚本已存在，且可在仓库中定位到实现依据
- `[-] 部分完成`：已有局部实现或底座，但尚未形成完整对外能力
- `[ ] 待实现`：已有明确方向，但当前仓库中没有完成实现
- `[~] 延期观察`：保留为中长期能力，不作为近期主线

## 总体目标

围绕“知识辅导 -> 知识沉淀 -> 复习调度”构建学习型 Knowledge Agent 平台，先打通服务化 MVP，再逐步演进到完整 Agent Core 与 Review 闭环。

## 主流程与节点

### 对话主链路

`输入接入` -> `身份与用户画像` -> `意图判断` -> `状态选择(IDLE/TEACHING/REVIEW/SUMMARY)` -> `知识检索` -> `LLM/Tool 执行` -> `结果返回` -> `可选知识归档`

### 知识归档链路

`总结文本` -> `文本清洗` -> `切片` -> `Embedding` -> `Milvus 入库` -> `KnowledgeCard 初始化` -> `标签/来源元数据`

### 复习链路

`登录或定时触发` -> `筛选 next_review_date 到期卡片` -> `生成复习列表` -> `Agent 提问` -> `质量评分(0..5)` -> `SM-2 更新` -> `提醒去重`

### 事件流策略

- MVP 阶段：优先同步 Dubbo 调用，先让主链路跑通
- 稳定阶段：将知识归档、复习提醒、统计事件迁移到 RocketMQ 异步化

## Phase 0：基础工程与依赖基线

- [x] Maven 多模块结构：`common/api/user/rag/core/gateway`
- [x] `knowledge-agent-user` Flyway 初始化脚本
- [x] `knowledge-agent-rag` Flyway 初始化脚本
- [x] `user/rag/gateway` 基础 Spring/Dubbo/Nacos 配置
- [x] Milvus Collection 初始化与 dense/sparse 字段定义
- [x] `docker-compose.yml` 依赖编排骨架：Nacos/Redis/MySQL/RocketMQ
- [ ] `knowledge-agent-core` 配置资源文件
- [ ] Redis 连接配置与缓存接入
- [ ] RocketMQ Topic 定义、生产者与消费者落地

完成判定：

- `[x]` 项必须能在仓库文件中直接定位
- `Phase 0` 完成需要 `core` 具备最小可启动配置，且 Redis/RocketMQ 至少完成基础接入

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
- [ ] 契约测试
- [ ] 统一错误码/状态码规范

完成判定：

- 公共类型和接口均有源码定义
- 后续要把“契约已定义但无实现”和“代码已实现”继续在文档中分开维护

## Phase 2：User Service

- [x] `sys_user` 表结构
- [x] `SysUser` 实体与 `SysUserMapper`
- [x] `login`
- [x] `getUserProfile`
- [ ] JWT 鉴权
- [ ] 登录态模型
- [ ] `UserController` 实际接口
- [ ] 用户偏好扩展模型

完成判定：

- 当前仅能说明 Dubbo Provider 级别能力已存在
- 要判定本阶段真正完成，还需要补齐对外入口、鉴权和稳定登录态

## Phase 3：RAG Service

- [x] `knowledge_card` 表结构
- [x] `KnowledgeCard` 实体与 Mapper
- [x] Milvus hybrid schema 初始化
- [x] dense + sparse + RRF 检索底座
- [x] `saveKnowledge`
- [x] `updateReviewStatus`
- [-] 内部已有混合检索能力，但未暴露标准 `search/recall` API
- [ ] 文档导入管道
- [ ] 标签过滤
- [ ] 引用返回
- [ ] 直答策略
- [ ] 批处理导入

完成判定：

- 当前已完成“归档写入 + 复习参数更新 + 检索底座”
- 只有补齐标准检索接口和导入流水线后，才能认为 RAG 服务完成对话型 Agent 的知识供给职责

## Phase 4：Agent Core MVP

- [ ] `ConversationState` 状态模型
- [ ] `StateContext` 会话上下文
- [ ] Prompt 装配
- [ ] LangChain4j Tool 路由
- [ ] `AgentService` Provider 实现
- [ ] User/RAG Dubbo 联动
- [ ] 会话记忆持久化

完成判定：

- 至少实现 `chat` 主链路
- 至少支持 `IDLE/TEACHING/REVIEW/SUMMARY` 基本状态切换
- 至少完成一次 `UserProfile + RAG + LLM/Tool` 的闭环调用

## Phase 5：Gateway / BFF

- [-] 启动类与基础配置已存在
- [ ] HTTP Controller
- [ ] SSE 流式响应
- [ ] 统一鉴权
- [ ] Agent/User/RAG 路由聚合
- [ ] 对外 API 文档

完成判定：

- 当前只能视为壳模块
- 本阶段完成的标准是存在真实对外入口，并能稳定承接登录、对话、复习三个场景

## Phase 6：Review 调度闭环

- [ ] 到期复习卡片查询
- [ ] `ReviewTask` 模型
- [ ] 登录触发复习提醒
- [ ] 定时调度生成复习列表
- [ ] Redis 队列或去重缓存
- [ ] Agent 主动提问
- [ ] 复习结果回写

完成判定：

- 用户登录或定时任务能够稳定拉起复习流程
- 同一知识点具备去重控制
- 评分结果能够回写 `knowledge_card` 并更新下次复习时间

## Phase 7：增强能力与长期方向

- [~] 知识图谱
- [~] 多模态生成
- [~] 动态角色进化
- [~] 复杂工作流编排
- [~] RocketMQ 全异步事件化

完成判定：

- 这些能力不应阻塞主线 MVP
- 只有在 Agent Core、Gateway、Review 闭环稳定后才进入优先级上升区

## 文档中的接口分层规则

### 代码已存在

- `UserService.login`
- `UserService.getUserProfile`
- `RagService.saveKnowledge`
- `RagService.updateReviewStatus`
- `KnowledgeDTO`
- `ChatRequest`
- `UserLoginRequest`

### 契约已定义但无实现

- `AgentService.chat`
- `AgentService.switchState`

### 仅规划中

- Gateway HTTP/SSE 对外接口
- `ConversationState`
- `StateContext`
- `ReviewTask`
- `ReminderEvent`
- citation/source/direct-answer 检索响应结构

## 下一阶段实施顺序

1. Agent Core MVP
2. Gateway / BFF
3. Review 调度闭环
4. 增强能力

排序原因：

- 当前最缺的是对话主链路闭环，而不是更多基础设施
- Gateway 的价值依赖 Agent Core 可用
- Review 闭环依赖 Agent 与 RAG 先形成统一调用路径
- 图谱、多模态、复杂工作流不应提前消耗主线资源

## 当前完成依据速查

- User 侧依据：`SysUser`、`SysUserMapper`、`SysUserServiceImpl`、用户 Flyway 脚本
- RAG 侧依据：`KnowledgeCard`、`KnowledgeCardMapper`、`RagServiceImpl`、`MilvusConfig`、RAG Flyway 脚本
- 公共契约依据：`knowledge-agent-api` 与 `knowledge-agent-common`
- 壳模块依据：`KnowledgeAgentCoreApplication`、`KnowledgeAgentGatewayApplication`

## 维护规则

- 以后只有在代码、配置、脚本或验证结果能支撑时，才允许把步骤改成 `[x]`
- 愿景、方案设计、README 描述不能直接作为 roadmap 的完成依据
- 每次更新 roadmap 时，都要同步检查“代码已存在 / 契约已定义但无实现 / 仅规划中”三层边界是否仍然准确
