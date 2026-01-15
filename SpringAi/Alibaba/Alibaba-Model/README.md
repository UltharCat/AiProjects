# Alibaba-Model

一个基于 **Spring AI Alibaba（DashScope）** 的“多模型能力 + RAG”示例服务：

- **Chat（文本对话）**：同步 + SSE/HTTP 流式
- **结构化输出**：把模型输出转换为实体（示例：`Country`）
- **Prompt Template**：从 `resources/prompts/*.st` 加载 system/user 模板
- **多模态能力**：
  - 图像生成（DashScope Image Model，返回图片 URL）
  - 语音转文字（Audio Transcription，示例音频 URL）
  - 文本转语音（Audio Synthesis，返回音频二进制下载）
- **RAG / 向量检索**：
  - 本地向量库：PgVector（PostgreSQL）
  - 远程向量库：DashScope Cloud Store（按 `indexName` 写入/检索）
  - 文档解析：Apache Tika

## 功能概览

### 1) Chat

- `GET /chat/singleChat?input=...`
- `GET /chat/singleChat/entity?input=...`：结构化返回 `Country`
- `GET /chat/stream/sse?input=...`：SseEmitter
- `GET /chat/stream/sse/flux?input=...`：Flux SSE
- `GET /chat/stream/http?input=...`：streamable-http（ResponseEntity + StreamingResponseBody）
- `GET /chat/promptTemplateChat?input=...`：使用 `prompts/user.st`
- `GET /chat/sysPromptTemplateChat?input=...`：system + user 模板
- （代码中还包含静态 RAG 示例等更多接口/示例片段）

### 2) Image

- `GET /image/singleImage?input=...`：生成图片 URL

### 3) Audio

- `GET /audio/transcription/singleTranscription`：音频转写（示例音频 URL）
- `GET /audio/synthesis/singleSynthesis?input=...`：TTS（返回音频 bytes 下载）

### 4) VectorStore（RAG）

该模块通过 `VectorStoreService` 统一封装：

- `saveFileToVectorStore(MultipartFile file, String indexName)`
  - `x-project.app.chat.vector-store.type=remote` 且 `indexName` 不为空：写入 DashScope Cloud Store
  - 否则：写入本地 PgVector
- `createDocumentRetriever(String indexName)`
  - remote：`DashScopeDocumentRetriever`
  - local：`VectorStoreDocumentRetriever`（threshold=0.4, topK=10）

## 技术栈

- Spring Boot Web
- Spring AI Alibaba DashScope（Chat/Image/Audio/Embedding 等）
- Spring AI VectorStore + PgVector
- PostgreSQL（pgvector extension）
- Redis（配置存在，用于扩展会话/缓存等）
- Apache Tika（文档读取/解析）

## 配置说明（`application.yml`）

- 端口
  - `server.port`: `18081`
- DashScope
  - `spring.ai.dashscope.api-key`: `${DASHSCOPE_API_KEY}`
  - `spring.ai.dashscope.chat.options.model`: `qwen-max`
  - `spring.ai.dashscope.embedding.options.model`: `text-embedding-v4`
  - `spring.ai.dashscope.embedding.options.dimensions`: `1024`
- Postgres（本地 PgVector 场景）
  - `spring.datasource.url`: `jdbc:postgresql://localhost:5432/springai`
  - `spring.datasource.username/password`
  - `spring.ai.vectorstore.pgvector.dimensions`: `1024`
  - `spring.ai.vectorstore.pgvector.initialize-schema`: `true`
- Redis
  - `spring.data.redis.host/port/database`
- 向量存储模式
  - `x-project.app.chat.vector-store.type`: `local | remote`

## 运行前置条件

- 必需：设置环境变量 `DASHSCOPE_API_KEY`
- 本地向量库（local）时：PostgreSQL 可用且启用 pgvector
- Redis：非强制，但配置默认存在（可按需启用）

## 启动

在 `SpringAi/Alibaba/Alibaba-Model` 目录下：

```powershell
mvn -q -DskipTests spring-boot:run
```

## 代码结构（核心类）

- 启动入口：`com.ai.alibaba.AlibabaModelApplication`
- Chat：`com.ai.alibaba.controller.ChatController`
- Image：`com.ai.alibaba.controller.ImageController`
- Audio：
  - `com.ai.alibaba.controller.AudioTranscriptionController`
  - `com.ai.alibaba.controller.AudioSynthesisController`
- RAG/VectorStore：
  - `com.ai.alibaba.service.VectorStoreService`
  - `com.ai.alibaba.service.impl.VectorStoreServiceImpl`

## 更新日志（Changelog）

### v0.0.1-SNAPSHOT

- 初始化 Alibaba-Model（DashScope 多模型能力示例）
- 增加 Chat：同步 + SSE/HTTP 流式、PromptTemplate、结构化输出示例
- 增加 Image（文生图）、Audio（转写/TTS）示例接口
- 增加 RAG：Embedding + PgVector（本地）与 DashScope Cloud Store（远程）两种向量存储模式
- 增加 Tika 文档解析与基础向量检索封装（`VectorStoreService`）

