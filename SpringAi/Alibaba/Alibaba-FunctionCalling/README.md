# Alibaba-FunctionCalling

一个基于 **Spring AI Alibaba（DashScope）** 的 Function Calling/Tool Calling 示例服务：通过 `ChatClient + @Tool` 暴露“订单查询”能力，让大模型在对话过程中按需调用工具完成查询。

该模块也包含一份 MCP Server 的配置（`spring.ai.mcp.server.*`），用于把工具能力通过 MCP 协议对外暴露（当前模块代码侧主要演示的是 `tools(...)` 调用方式）。

## 功能概览

- 订单查询 REST 接口（`com.ai.alibaba.controller.FunctionCallingController`）
  - `GET /functionCalling/findAllOrder`：直接查询数据库订单列表
  - `GET /functionCalling/callGetOrderById?question=...&orderId=...`：流式对话 + 工具调用（按订单 ID 查询）
  - `GET /functionCalling/callFindAllOrder?question=...`：流式对话 + 工具调用（查询全部订单）
  - `GET /functionCalling/callOrderTools?question=...`：流式对话 + 工具调用（并在 system prompt 中要求返回本次使用的 Tool 名称 JSON）
- 工具定义（`com.ai.alibaba.tools.OrderTools`）
  - `@Tool getOrderById(Long orderId)`：从本地订单列表筛选
  - `@Tool findAllOrder()`：通过 `AiWebClient` 远程调用获取订单列表

## 技术栈

- Spring Boot Web
- Spring Data JPA + MySQL
- Spring AI Alibaba DashScope（`DashScopeChatModel`）
- Spring AI Tools（`@Tool` / `tools(orderTools)`）
- （可选）Spring AI MCP Server 配置（streamable endpoint）

## 配置说明（`application.yml`）

- 端口
  - `server.port`: `18081`
- DashScope
  - `spring.ai.dashscope.api-key`: `${DASHSCOPE_API_KEY}`
  - `spring.ai.dashscope.chat.options.model`: `qwen-max`
- 数据库（MySQL）
  - `spring.datasource.url`: `jdbc:mysql://localhost:3306/ai...`
  - `spring.datasource.username/password`
  - `spring.jpa.hibernate.ddl-auto`: `validate`（需要数据库提前建表）
- MCP Server（配置已就绪，端点 `/mcp`）
  - `spring.ai.mcp.server.protocol`: `streamable`
  - `spring.ai.mcp.server.streamable-http.mcp-endpoint`: `/mcp`

## 运行前置条件

1. 已设置环境变量 `DASHSCOPE_API_KEY`
2. MySQL 可用，并存在 `ai` 库及对应表结构
   - 因为默认 `ddl-auto=validate`，若未建表启动会失败

## 启动

在 `SpringAi/Alibaba/Alibaba-FunctionCalling` 目录下：

```powershell
mvn -q -DskipTests spring-boot:run
```

## 快速调用示例

- 直接查询全部订单：

```http
GET http://localhost:18081/functionCalling/findAllOrder
```

- 让模型“带工具”查询订单：

```http
GET http://localhost:18081/functionCalling/callGetOrderById?question=帮我查一下这个订单&orderId=1
```

## 重要说明

- 代码里保留了一个被注释掉的 `.functions("orderFunction")` 示例，并注明：
  - **Spring AI 1.1.0 版本移除了 `functions(...)` API**
  - 当前推荐使用 `tools(...)`（对应 `OrderTools` 的 `@Tool` 方法）

## 代码结构（核心类）

- 启动入口：`com.ai.alibaba.AlibabaFunctionCallingApplication`
- Controller：`com.ai.alibaba.controller.FunctionCallingController`
- 工具：`com.ai.alibaba.tools.OrderTools`
- 订单领域：`com.ai.alibaba.entity.*`、`com.ai.alibaba.repository.*`、`com.ai.alibaba.service.*`

## 更新日志（Changelog）

### v0.0.1-SNAPSHOT

- 初始化 Function Calling 示例服务（DashScope + Web + JPA/MySQL）
- 增加 `OrderTools` 并通过 `tools(...)` 触发 Tool Calling
- 增加订单查询接口（直接查库 + 模型对话触发工具）
- 增加 MCP Server 相关配置（streamable `/mcp` 端点）

