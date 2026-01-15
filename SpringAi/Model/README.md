# Model

一个偏“模型层/适配层”的 Spring AI 示例：演示如何把 Spring AI 自动装配出来的 **各种 Model（Chat/Image/Audio/Moderation 等）** 收敛为统一的 `AiModelFactory`，并通过接口在运行时切换不同模型。

> 与 `ChatClient` 模块相比：
> - `ChatClient` 更偏“应用层调用 ChatClient + 流式输出 + 记忆”。
> - `Model` 更偏“直接使用底层 Model API（ChatModel/ImageModel 等）并做工厂选择”。

## 功能概览

- `AiModelFactory`：从 `Map<String, Model<?, ?>>` 中按 key 选择模型实例
  - `openai-chat` -> `openAiChatModel`
  - `openai-image` -> `openAiImageModel`
  - `openai-audio-transcription` / `openai-audio-speech` / `openai-moderation`
  - 默认 -> `ollamaChatModel`
- Chat 接口（`com.ai.model.controller.ChatController`）
  - `GET /chat/changeModel?modelType=...`：切换当前使用的模型
  - `GET /chat/singleChat?input=...`：同步对话（Ollama / OpenAI）
- Image 接口（`com.ai.model.controller.ImageController`）
  - `GET /image/singleImage?input=...`：生成图片（OpenAI Compatible），返回 URL

## 技术栈

- Spring Boot Web
- Spring AI（Ollama / OpenAI starters）
- Redis（配置存在，但当前 controller 演示未直接依赖；可用于扩展会话/缓存等）

## 配置说明（`application.yml`）

- 服务端口
  - `server.port`: `8081`
- Redis
  - `spring.data.redis.host` / `port` / `database`
- Ollama
  - `spring.ai.ollama.base-url`: `http://localhost:11434`
  - `spring.ai.ollama.chat.options.model`: `gpt-oss:20b`
- OpenAI Compatible（示例：DashScope）
  - `spring.ai.openai.api-key`: `${DASHSCOPE_API_KEY}`
  - `spring.ai.openai.base-url`: `https://dashscope.aliyuncs.com/compatible-mode`
  - `spring.ai.openai.chat.options.model`: `qwen-omni-turbo`

## 运行前置条件

- 使用 Ollama：Ollama 服务已启动且模型可用
- 使用 OpenAI Compatible：已设置 `DASHSCOPE_API_KEY`

## 启动

在 `SpringAi/Model` 目录下：

```powershell
mvn -q -DskipTests spring-boot:run
```

## 使用示例

- 默认（Ollama）聊天：

```http
GET http://localhost:8081/chat/singleChat?input=你好
```

- 切换到 OpenAI Chat：

```http
GET http://localhost:8081/chat/changeModel?modelType=openai-chat
```

- OpenAI 图片生成：

```http
GET http://localhost:8081/image/singleImage?input=一只在雨中散步的猫
```

## 代码结构（核心类）

- 启动入口：`com.ai.model.ModelApplication`
- 模型工厂：`com.ai.model.config.model.AiModelFactory`
- 常量：`com.ai.model.config.constant.AiConstant`
- Controller：
  - `com.ai.model.controller.ChatController`
  - `com.ai.model.controller.ImageController`

## 更新日志（Changelog）

### v0.0.1-SNAPSHOT

- 初始化 Model 示例服务（Spring Boot Web + Spring AI）
- 增加 `AiModelFactory`：统一从 IOC 中选择各类 Model 实例
- 增加 Chat 接口：支持运行时在 Ollama / OpenAI Chat 之间切换
- 增加 Image 示例接口：演示 OpenAI Compatible 图片生成调用

