# Gateway API 文档

本文描述 `knowledge-agent-gateway` 当前对外开放的登录、对话、检索与复习接口。

## 通用约定

- Base URL: `http://localhost:8080`
- 除登录接口外，其余接口都要求请求头 `Authorization: Bearer <accessToken>`
- 统一响应格式：

```json
{
  "code": 200,
  "message": "Success",
  "data": {}
}
```

- 常见鉴权失败格式：

```json
{
  "code": 401,
  "message": "Missing bearer token",
  "data": null
}
```

## 1. 登录

- Method: `POST`
- Path: `/api/auth/login`

请求体：

```json
{
  "username": "alice",
  "password": "password"
}
```

响应体：

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "userId": 1,
    "accessToken": "<jwt>",
    "tokenType": "Bearer",
    "expiresAt": "2026-03-18T12:00:00Z",
    "pendingReviewCount": 1,
    "pendingReviewTasks": [
      {
        "taskId": "task-1",
        "userId": 1,
        "knowledgeId": 99,
        "summary": "SM-2 overview",
        "dueAt": "2026-03-18T09:00:00",
        "triggerSource": "LOGIN",
        "status": "PENDING",
        "dedupKey": "LOGIN:1:99"
      }
    ]
  }
}
```

说明：

- 登录成功后立即返回 access token。
- Gateway 会在登录完成后同步派发当前用户的待复习任务。
- 登录触发的复习任务会经过去重窗口控制，避免短时间内重复提醒。

## 2. 对话

### 2.1 普通对话

- Method: `POST`
- Path: `/api/agent/chat`

请求体：

```json
{
  "prompt": "review",
  "sessionId": "session-1"
}
```

### 2.2 SSE 对话

- Method: `GET`
- Path: `/api/agent/chat/stream?prompt=review`

说明：

- 当前实现为最小 SSE 能力，返回单个 `message` 事件后完成连接。

### 2.3 强制切换状态

- Method: `POST`
- Path: `/api/agent/state?targetState=REVIEW`

可选状态：

- `IDLE`
- `TEACHING`
- `REVIEW`
- `SUMMARY`

## 3. 检索

- Method: `GET`
- Path: `/api/rag/search?query=sm2&limit=3&tags=memory&tags=review`

说明：

- `userId` 由 token 自动解析。
- `tags` 为可选参数，支持多值过滤。
- 单条知识结果会返回 `source`、`citation`、`directAnswer`、`matchedSegment` 等字段。

## 4. 复习任务

### 4.1 查询待复习任务

- Method: `GET`
- Path: `/api/reviews/pending?limit=5`

返回的批次结果包含：

- `batchId`：复习批次唯一标识
- `createdAt`：批次生成时间
- `tasks`：本次派发的任务列表

### 4.2 查询最近一次调度结果

- Method: `GET`
- Path: `/api/reviews/scheduled/latest`

说明：

- 返回当前用户最近一次 `SCHEDULED` 来源的 review 批次。
- 优先读取 RAG 持久化结果；若持久化结果不可用，则回退到 Redis 或内存中的最近批次缓存。

### 4.3 回写复习结果

- Method: `PATCH`
- Path: `/api/reviews/status`

请求体：

```json
{
  "knowledgeId": 99,
  "quality": 4
}
```

说明：

- `quality` 取值范围遵循当前 SM-2 评分输入 `0..5`。
- 回写成功后会同步把对应持久化 review 任务标记为 `COMPLETED`。

## 5. 鉴权约定

- Header 名称：`Authorization`
- Header 格式：`Bearer <accessToken>`
- Token 内容：HS256 签名 JWT，包含 `userId`、`username`、`learningStyle`、`iss`、`iat`、`exp`

## 6. Review 调度约定

当前 review 派发相关契约：

- `ReviewTaskDTO`
- `ReviewTaskBatchResponse`
- `ReviewTriggerSource`：`LOGIN`、`MANUAL`、`SCHEDULED`

当前已补齐的能力：

- Review 批次生成后会持久化到 `review_task_record`
- 最近调度批次支持从持久化层回查
- 复习结果回写后会同步更新任务状态

当前仍待继续完善的部分：

- 多实例场景下更完整的投递确认与消费语义
- 基于事件的异步审计和重试策略
