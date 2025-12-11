# AI-Projects

这是一个用于探索和实践各种大语言模型（LLM）和AI技术的Java项目集合。项目基于Spring Boot和Maven，采用多模块架构，整合了包括Ollama、OpenAI以及阿里巴巴通义千问在内的多种模型，旨在探索聊天、函数调用、多模态交互等不同AI应用场景的实现。

## 项目概述

本项目是一个多模块的Spring Boot工程，用于试验各种大语言模型（LLMs）和AI技术。项目探索了与不同AI服务（如Ollama、OpenAI、Alibaba）的集成，并记录了在开发过程中关于技术选型、架构设计和问题解决的思考。

## 模块说明

- **AI-Alibaba-FunctionCalling**: 探索和实现基于阿里巴巴通义千问模型的函数调用功能，同时兼有MCP Server的功能。
- **AI-Alibaba-MCP-Client**: 尝试构建多模态对话客户端，但因依赖库版本问题暂未完全实现。
- **AI-Alibaba-Model**: 用于阿里巴巴多模态模型学习的模块。
- **AI-ChatClient**: 一个通用的、与具体模型无关的聊天客户端模块。
- **AI-Model**: 试图兼容openai接口、ollma接口和dashscope接口，定义了项目中通用的模型实体和接口的模块。
- **AI-Ollama-Base**: 提供了与本地模型服务Ollama集成的基础功能，使用restTemplate通过访问本地ollama接口进行间接模型访问。

## 开发思路与技术探索

1. **Spring AOT (Ahead-Of-Time Compilation)**
    -   通过在编译期处理自动配置和生成代理类，AOT可以显著减少应用的启动时间和运行时开销。
    -   适用于无复杂静态初始化（如I/O操作）的POJO类，能有效提升性能。
    -   自SpringBoot 3.2起支持，并在3.3版本中默认启用。

2. **统一AI模型调用**
    -   尽管`spring-ai`项目致力于提供统一的API，但在当前版本（`1.0.0-M6`）中，针对聊天（Chat）、图像（Image）和音频（Audio）的调用封装仍存在差异。
    -   不同模型的Prompt、Message和Options封装各不相同，开发者需要针对性地进行适配。

3. **用户会话管理**
    -   当前的Controller实现中缺少用户绑定。计划使用`Map`来关联用户会话（`Key`为Token），并结合Redis的TTL（Time-To-Live）机制来管理会话生命周期，确保多用户场景下的隔离性。

4. **Java与Python生态结合**
    -   鉴于Python在AI领域的生态系统更为成熟，正在思考如何通过某种协议（如gRPC或HTTP）将Python的AI应用与Java后端服务进行高效结合，以弥补Java生态的不足。

## 已知问题与记录

1.  **Alibaba DashScope依赖问题**
    -   **模型支持不全**: `AI-Alibaba-Model`项目已停止，因为官方`DashScope`依赖对自家的Qwen系列模型支持不到位，特别是对多模态模型（如`qwen-vl-plus`）的支持缺失，导致无法进行完整的的多模态功能开发。

2.  **Spring AI客户端封装不一致**
    -   `spring-ai`为不同模型提供了统一的`ChatClient`抽象，但对于图像和音频功能，缺少类似的客户端封装，需要直接调用各自的`ImageModel`或`AudioModel`，增加了开发复杂性。

