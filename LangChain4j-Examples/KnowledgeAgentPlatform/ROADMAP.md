# 开发路线图：知识 Agent 平台 (Knowledge Agent Platform)

本路线图基于代码现状重新整理，并同步更新各阶段进度。

> 扫描日期: 2026-03-17
> 项目根目录: F:\JavaProjects\AiProjects\LangChain4j-Examples\KnowledgeAgentPlatform
> 核心数据库: knowledge_agent (MySQL)

## 🧭 总体目标
以 Dubbo 微服务为基础，落地“用户画像 + RAG + FSM/ReAct Agent + 复习调度 + Gateway/BFF”的完整链路。

## 🏁 第 0 阶段：基础设施与工程骨架
- [x] Maven 多模块结构 (common, api, user, rag, core, gateway)
- [x] MySQL + Flyway 脚本 (user/rag)
- [x] Nacos 配置导入 (user/rag/gateway)
- [ ] Nacos 配置导入 (core)
- [ ] Redis 连接与缓存配置
- [x] Milvus 连接与集合初始化 (dense + sparse + BM25)
- [ ] RocketMQ 基础配置与 Topic 定义

## 🧩 第 1 阶段：公共能力与接口契约
- [x] Result / BizException / GlobalExceptionHandler
- [x] EbbinghausUtils (SM-2)
- [x] API DTO: KnowledgeDTO
- [x] API Request: ChatRequest / UserLoginRequest
- [x] Dubbo 接口: AgentService / RagService / UserService
- [ ] 接口契约对齐文档与基础契约测试

## 👤 第 2 阶段：User Service (Dubbo Provider)
- [x] sys_user 表结构与实体/Mapper
- [x] login (BCrypt 校验, mock token)
- [x] getUserProfile (学习风格返回)
- [ ] JWT 鉴权与登录态模型
- [ ] REST Controller (UserController 目前为空)

## 🧠 第 3 阶段：RAG Service (Milvus + MySQL)
- [x] knowledge_card 记忆表 (doc_id + 复习参数)
- [x] Milvus Schema 初始化与 Hybrid 检索器 (dense + sparse + RRF)
- [x] saveKnowledge: 文本切分 -> 向量化 -> Milvus 入库
- [x] saveKnowledge: MySQL 复习记录初始化
- [x] updateReviewStatus: SM-2 计算与更新
- [ ] 对外检索 API (search/recall)
- [ ] 文档导入与批处理管道
- [ ] 复习事件联动 (RocketMQ/事件驱动)

## 🤖 第 4 阶段：Agent Core (FSM + ReAct)
- [ ] 会话状态模型与 StateContext
- [ ] LLM 接入与 Function Calling
- [ ] 动态 Prompt 组装 + Tool 调用
- [ ] AgentService 实现 (chat / switchState)
- [ ] 与 RagService/UserService 联动

## 🌐 第 5 阶段：Gateway/BFF
- [ ] 引入 Web 依赖与 Controller/SSE
- [ ] 统一鉴权与请求路由
- [ ] 对接 AgentService/UserService/RagService

## ⏰ 第 6 阶段：复习调度与推送
- [ ] 定时任务生成复习列表
- [ ] Redis 复习队列
- [ ] 登录后 SSE 提示

## 🧭 第 7 阶段：优化与扩展 (长期)
- [ ] 混合检索增强 (如 ES + RRF) 说明: 已在 Milvus 中实现基础版 Hybrid
- [ ] 知识图谱 (Neo4j)
- [ ] 动态角色进化
- [ ] 多模态生成
