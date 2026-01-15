# Ollama-Base

一个最小可运行的 **Spring Boot + Ollama** 示例应用：通过一个简单的 HTTP 接口，把用户的 `prompt` 转发给本地 Ollama（默认 `http://localhost:11434`），并返回大模型的原始响应。

> 该子项目目前走的是“自己用 `RestTemplate` 调 Ollama HTTP API”的方式（见 `com.base.service.impl.ollamaBase.restTemplate.OllamaRestTemplate`），并通过配置 `spring.ai.ollama.type=restTemplate` 来启用。

## 功能概览

- 提供一个 GET 接口：`/ollamaBase/chat`
  - 入参：`prompt`
  - 出参：Ollama 返回的原始 JSON 字符串
- 通过配置项切换/指定：
  - Ollama 服务地址（`spring.ai.ollama.base-url`）
  - 使用的模型（`spring.ai.ollama.chat.options.model`）
- 使用 `@ConditionalOnProperty` 按配置启用不同实现（当前仅实现了 `restTemplate` 版本）。

## 技术栈

- Java + Spring Boot Web
- Lombok（日志注解）
- `RestTemplate` 调用 Ollama HTTP API

## 接口说明

### 1) 对话接口

- URL：`GET /ollamaBase/chat`
- Query 参数：
  - `prompt`：用户输入（必填）
- 返回：`String`（Ollama 响应体原文）

示例请求：

```http
GET http://localhost:8081/ollamaBase/chat?prompt=你好，介绍一下你自己
```

> 说明：当前实现直接把 Ollama 的响应体当字符串返回，未做结构化解析、未做流式输出，也未做错误码/超时等治理。

## 配置说明（`application.yml`）

该子项目的默认配置如下（路径：`src/main/resources/application.yml`）：

- `server.port`: `8081`
- `spring.application.name`: `AI-Ollama-Base`
- `spring.ai.ollama.type`: `restTemplate`
  - 用于启用 `OllamaRestTemplate`（`@ConditionalOnProperty`）
- `spring.ai.ollama.base-url`: `http://localhost:11434`
  - Ollama 本地服务地址
- `spring.ai.ollama.chat.options.model`: `gpt-oss:20b`
  - 调用的模型名称（需确保已在本地拉取/可用）

## 运行前置条件

1. 本机已安装并启动 Ollama
2. Ollama 已经有可用模型（与 `spring.ai.ollama.chat.options.model` 一致）
3. 默认假设 Ollama API 可通过 `http://localhost:11434` 访问

## 启动与验证

在该子项目目录 `SpringAi/Ollama-Base` 下启动：

```powershell
mvn -q -DskipTests spring-boot:run
```

启动后访问：

```text
http://localhost:8081/ollamaBase/chat?prompt=Hello
```

## 代码结构（核心类）

- 入口：`com.base.OllamaBaseApplication`
- Controller：`com.base.controller.OllamaBaseController`
  - 暴露 `/ollamaBase/chat`
- Service 接口：`com.base.service.OllamaBaseService`
- Client 抽象类：`com.base.service.impl.ollamaBase.OllamaClient`
  - 统一读取配置：`OLLAMA_URL`、`OLLAMA_MODEL`
- RestTemplate 实现：`com.base.service.impl.ollamaBase.restTemplate.OllamaRestTemplate`
  - 通过 `POST {baseUrl}/api/chat` 调用 Ollama

## 更新日志（Changelog）

### v0.0.1-SNAPSHOT

- 初始化 `Ollama-Base` 子项目（Spring Boot Web）
- 增加 `/ollamaBase/chat` 接口，支持通过 query 参数 `prompt` 发起请求
- 增加基于 `RestTemplate` 的 Ollama 调用实现（`spring.ai.ollama.type=restTemplate`）
- 增加基础配置：端口、Ollama base URL、模型名称

