# Knowledge Agent Platform

面向知识辅导、知识沉淀与复习调度的学习型 Knowledge Agent 平台。

项目基于 `Spring Boot + Dubbo + LangChain4j + Milvus + MySQL + Redis + RocketMQ` 构建，当前已经完成从基础设施到 Gateway/BFF 的 MVP 主链路，并补齐了 review 批次持久化与回查能力。

## 当前结论

- `Phase 0-5`：已完成
- `Phase 6`：部分完成
- `Phase 7`：明确延后，不进入当前交付范围

当前不能将项目整体标记为“已全部完善”。原因不是主链路缺失，而是 `Phase 6` 依赖的 Redis 去重/批次缓存链路仍是“部分完成”，当前 review 闭环仍停留在“单实例可运行、具备持久化回查能力”的阶段。

## 已落地能力

### 基础设施与模块结构

- Maven 多模块结构已经完整拆分为 `common / api / user / rag / core / gateway`
- `docker-compose.yml` 提供了 Nacos、Redis、MySQL、RocketMQ 的本地依赖基线
- User、RAG 模块已具备 Flyway 迁移脚本
- Redis 已接入 review 去重、批次缓存与会话存储降级链路
- RocketMQ 已接入最小事件契约、生产者和消费者骨架

### 公共契约与通用能力

- 统一 `Result`、`BizException`、`GlobalExceptionHandler`
- 统一错误码/状态码规范
- 补齐 JWT 工具、认证 claims 与契约级测试
- 补齐 `ReviewTaskDTO`、`ReviewTaskBatchDTO`、`ReviewTaskStatus`、`ReviewTriggerSource`

### User Service

- 已实现 Dubbo 登录与画像读取能力
- 已提供真实 HTTP 接口：
  - `POST /user/login`
  - `GET /user/profile`
- 已切换到签名 JWT 登录态
- 已扩展结构化用户偏好模型与 `preferences_json`

### RAG Service

- 已支持知识归档写入 MySQL + Milvus
- 已支持 SM-2 复习参数更新
- 已支持检索、标签过滤、citation/source/direct-answer/matchedSegment 返回
- 已支持小批量同步导入
- 已支持 review 批次持久化、最近批次回查和任务状态更新

### Agent Core

- 已支持 `IDLE / TEACHING / REVIEW / SUMMARY` 状态
- 已实现 Prompt 装配、Dubbo Tool Router、用户画像与 RAG 联动
- 已接入 LangChain4j 教学/总结运行时，失败时回退规则链路
- 会话存储已升级为 Redis 优先、内存降级

### Gateway / BFF

- 已提供登录、聊天、SSE、检索、review 相关 HTTP API
- 已统一 Bearer Token 鉴权
- 已提供 review 手动触发、登录触发、定时调度入口
- 已提供最近调度批次查询和状态回写接口
- 已有外部 API 文档 `docs/gateway-api.zh-CN.md`

## 尚未完全完成的部分

### Phase 6 仍为部分完成

以下能力已经完成：

- review 任务模型
- 登录触发 review
- 定时调度生成批次
- Redis 去重与批次缓存
- review 批次持久化到 `review_task_record`
- 最近调度批次从持久化层回查
- 复习结果回写后同步更新任务状态

以下能力仍待继续增强：

- Redis 去重、批次缓存和降级逻辑在更多运行场景下的验证
- review 批次缓存、去重窗口与持久化回查之间更清晰的边界定义
- 更完整的异常恢复与事件审计能力

## 建议阅读顺序

1. [ROADMAP.md](F:/JavaProjects/AiProjects/LangChain4j-Examples/KnowledgeAgentPlatform/ROADMAP.md)
2. [ROADMAP.zh-CN.md](F:/JavaProjects/AiProjects/LangChain4j-Examples/KnowledgeAgentPlatform/ROADMAP.zh-CN.md)
3. [docs/gateway-api.zh-CN.md](F:/JavaProjects/AiProjects/LangChain4j-Examples/KnowledgeAgentPlatform/docs/gateway-api.zh-CN.md)

## API 测试案例

### 1. 登录并获取待复习任务

请求：

- `POST /api/auth/login`

示例请求体：

```json
{
  "username": "alice",
  "password": "password"
}
```

预期结果：

- 返回 `accessToken`
- 返回 `pendingReviewCount`
- 返回 `pendingReviewTasks`

### 2. 查询待复习批次

请求：

- `GET /api/reviews/pending?limit=5`
- Header: `Authorization: Bearer <accessToken>`

预期结果：

- 返回 `batchId`
- 返回 `triggerSource=MANUAL`
- 返回 `tasks` 列表

### 3. 查询最近一次调度批次

请求：

- `GET /api/reviews/scheduled/latest`
- Header: `Authorization: Bearer <accessToken>`

预期结果：

- 当存在调度批次时，返回最近一次 `SCHEDULED` 批次
- 返回中包含 `batchId`、`createdAt`、`tasks`
- 当不存在调度批次时，返回空批次结构而不是报错

### 4. 回写复习结果

请求：

- `PATCH /api/reviews/status`
- Header: `Authorization: Bearer <accessToken>`

示例请求体：

```json
{
  "knowledgeId": 99,
  "quality": 4
}
```

预期结果：

- 返回成功状态
- 对应知识卡复习参数被更新
- 对应 review 任务状态被标记为 `COMPLETED`

### 5. 检索知识并验证扩展字段

请求：

- `GET /api/rag/search?query=sm2&limit=3&tags=memory`
- Header: `Authorization: Bearer <accessToken>`

预期结果：

- 返回知识列表
- 单条结果包含 `source`、`citation`、`directAnswer`、`matchedSegment`
- `tags` 过滤生效

### 6. 对话接口与 SSE 接口

请求：

- `POST /api/agent/chat`
- `GET /api/agent/chat/stream?prompt=review`

预期结果：

- 普通对话接口返回统一响应结构
- SSE 接口返回至少一个 `message` 事件

## 下一阶段重点

1. 收口 Phase 6 中 Redis 去重、批次缓存与持久化回查之间的运行边界，明确哪些能力依赖缓存、哪些能力依赖持久化。
2. 增加 review 链路在异常场景下的恢复与观测能力，例如批次缺失、缓存失效、状态回写失败后的处理策略。
3. 继续把 LangChain4j 从教学/总结入口扩展到更完整的 Tool 驱动编排，同时保持 Phase 7 延后，不提前引入知识图谱、多模态和复杂工作流。
