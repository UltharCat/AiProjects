# Gateway API 文档

本文描述 `knowledge-agent-gateway` 当前对外暴露的登录、对话、检索、复习接口约定。

## 通用约定

- Base URL: `http://localhost:8080`
- 登录接口之外的所有接口都需要请求头 `Authorization: Bearer <accessToken>`
- 统一响应结构：

```json
{
  "code": 200,
  "message": "Success",
  "data": {}
}
```

- 统一错误结构：

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
        "taskId": "3cd3e0a8-25ef-4f48-9eb0-608c58bb9021",
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

- 登录成功后立即返回 access token
- Gateway 会在登录后自动查询当前用户待复习任务
- 登录触发的复习任务会经过去重窗口控制，避免短时间重复提醒

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

响应体：

```json
{
  "code": 200,
  "message": "Success",
  "data": "Let's start reviewing: explain SM-2 in your own words."
}
```

### 2.2 SSE 对话

- Method: `GET`
- Path: `/api/agent/chat/stream?prompt=review`

说明：

- 当前实现为最小 SSE 能力，返回单条 `message` 事件后完成连接

### 2.3 强制切换状态

- Method: `POST`
- Path: `/api/agent/state?targetState=REVIEW`

可选状态：

- `IDLE`
- `TEACHING`
- `REVIEW`
- `SUMMARY`

## 3. 知识检索

- Method: `GET`
- Path: `/api/rag/search?query=sm2&limit=3`

说明：

- `userId` 由 token 自动解析，不再允许客户端直接传入

## 4. 复习任务

### 4.1 查询待复习任务

- Method: `GET`
- Path: `/api/reviews/pending?limit=5`

响应体：

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "userId": 1,
    "triggerSource": "MANUAL",
    "requestedLimit": 5,
    "dispatchedCount": 1,
    "tasks": [
      {
        "taskId": "task-1",
        "userId": 1,
        "knowledgeId": 99,
        "summary": "SM-2 overview",
        "dueAt": "2026-03-18T09:00:00",
        "triggerSource": "MANUAL",
        "status": "PENDING",
        "dedupKey": "MANUAL:1:99"
      }
    ]
  }
}
```

说明：

- 手动查询不做去重，便于客户端主动拉取
- 调度触发和登录触发复用同一批次契约 `ReviewTaskBatchResponse`

### 4.2 回写复习结果

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

- `quality` 取值范围遵循当前 SM-2 评分输入 `0..5`

## 5. 鉴权约定

- Header 名称：`Authorization`
- Header 格式：`Bearer <accessToken>`
- Token 内容：HS256 签名 JWT，包含 `userId`、`username`、`learningStyle`、`iss`、`iat`、`exp`

## 6. Review 调度约定

当前阶段已提供调度所需的任务派发契约：

- `ReviewTaskDTO`
- `ReviewTaskBatchResponse`
- `ReviewTriggerSource`：`LOGIN`、`MANUAL`、`SCHEDULED`

当前阶段尚未实现：

- 定时任务实际注册
- Redis 队列/去重缓存落地
- Review 任务持久化表
