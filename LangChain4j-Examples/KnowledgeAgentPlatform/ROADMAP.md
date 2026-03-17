# Knowledge Agent Platform Roadmap

This roadmap is calibrated against the current code, configuration, and verification results rather than vision-only descriptions.

> Scan date: 2026-03-17  
> Project path: `F:\JavaProjects\AiProjects\LangChain4j-Examples\KnowledgeAgentPlatform`  
> Build result: `mvn -DskipTests compile` passed  
> Verification note: full `mvn test` still depends on local `Nacos` and external services, so targeted tests are used to validate newly implemented behavior

## Status Legend

- `[x] Done`: implementation exists in code/config/scripts and can be located in the repository
- `[-] Partial`: a working foundation exists, but the feature is not yet production-complete
- `[ ] Planned`: no complete implementation yet
- `[~] Deferred`: intentionally postponed from the near-term delivery path

## Goal

Build a learning-oriented Knowledge Agent platform around the main loop of `teaching -> knowledge capture -> review scheduling`, landing a service-oriented MVP first and then strengthening orchestration and automation.

## Main Flows

### Conversation Flow

`Input` -> `Identity and profile` -> `Intent detection` -> `State selection (IDLE/TEACHING/REVIEW/SUMMARY)` -> `Knowledge retrieval` -> `Tool/LLM execution` -> `Response` -> `Optional archival`

### Knowledge Archival Flow

`Summary text` -> `Cleaning` -> `Chunking` -> `Embedding` -> `Milvus insert` -> `KnowledgeCard upsert` -> `Tags and metadata`

### Review Flow

`Login or scheduler trigger` -> `Due card query` -> `Review list generation` -> `Agent question` -> `Quality score (0..5)` -> `SM-2 update` -> `Deduplication`

### Event Strategy

- MVP stage: synchronous Dubbo calls first
- Stabilization stage: move archival, reminders, and analytics to RocketMQ-based async events

## Phase 0: Foundation and Runtime Baseline

- [x] Multi-module Maven structure: `common/api/user/rag/core/gateway`
- [x] `knowledge-agent-user` Flyway initialization
- [x] `knowledge-agent-rag` Flyway initialization
- [x] Base Spring/Dubbo/Nacos config for `user/rag/gateway`
- [x] Base runtime config for `knowledge-agent-core`
- [x] Milvus collection initialization with dense/sparse schema
- [x] `docker-compose.yml` baseline for Nacos/Redis/MySQL/RocketMQ
- [ ] Redis connection and cache integration
- [ ] RocketMQ topics, producer, and consumer wiring

Completion criteria:

- Items marked `[x]` must be backed by files in the repository
- Phase 0 is considered complete only when Redis and RocketMQ also have working baseline integrations

## Phase 1: Shared Contracts and Common Utilities

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
- [ ] Contract tests across service boundaries
- [ ] Unified error/status code conventions

Completion criteria:

- Shared types and interfaces are present in source form
- Contract-level tests still need to be added before this phase is fully hardened

## Phase 2: User Service

- [x] `sys_user` schema
- [x] `SysUser` entity and `SysUserMapper`
- [x] `login`
- [x] `getUserProfile`
- [ ] JWT-based authentication
- [ ] Login session model
- [ ] Real `UserController` endpoints
- [ ] Expanded user preference model

Completion criteria:

- Dubbo provider capabilities exist
- External auth and stable session handling are still missing

## Phase 3: RAG Service

- [x] `knowledge_card` schema
- [x] `KnowledgeCard` entity and mapper
- [x] Milvus hybrid schema initialization
- [x] Dense + sparse + RRF retrieval foundation
- [x] `saveKnowledge`
- [x] `updateReviewStatus`
- [x] Standard search API foundation: `searchKnowledge`
- [x] Review query foundation: `listPendingReviews`
- [-] Knowledge card metadata has been expanded with `userId/summary/tags`, but citation/source response structure is still missing
- [ ] Document import pipeline
- [ ] Tag filtering strategy
- [ ] Citation/source return payload
- [ ] Direct-answer strategy
- [ ] Batch import tooling

Completion criteria:

- The service now covers archival write, review status update, search, and pending-review query
- RAG is not considered complete until import, citation/source output, and richer retrieval response contracts are finished

## Phase 4: Agent Core MVP

- [x] `ConversationState`
- [x] `StateContext`
- [x] Prompt assembly service
- [-] Tool routing foundation via `AgentToolRouter` and Dubbo orchestration
- [x] `AgentService` provider implementation
- [x] User/RAG Dubbo integration
- [-] Session memory store implemented in-memory; durable persistence is still pending
- [-] Rule-based state transitions are working, but real LangChain4j model execution is still pending

Completion criteria:

- `chat` is implemented and reachable through Dubbo
- Base states `IDLE/TEACHING/REVIEW/SUMMARY` are supported
- User profile + RAG + response orchestration is closed-loop for the MVP path
- This phase remains partial until real model/tool execution and durable memory are added

## Phase 5: Gateway / BFF

- [x] Startup class and base config
- [x] HTTP controllers
- [x] Minimal SSE endpoint
- [x] Agent/User/RAG route aggregation
- [-] Authentication is still pass-through login only, not unified request auth
- [ ] External API documentation

Completion criteria:

- Gateway is no longer just a shell module
- This phase remains partial until unified auth and API documentation are in place

## Phase 6: Review Scheduling Loop

- [x] Due review card query
- [ ] `ReviewTask` model
- [ ] Login-triggered review reminder
- [ ] Scheduled review list generation
- [ ] Redis queue or dedup cache
- [x] Agent-driven review questioning within the conversation flow
- [x] Review result write-back

Completion criteria:

- The conversational review loop exists for manually triggered review sessions
- This phase remains incomplete until login/scheduler triggers and deduplication are implemented

## Phase 7: Enhancements and Long-Term Direction

- [~] Knowledge graph
- [~] Multimodal generation
- [~] Dynamic persona evolution
- [~] Complex workflow orchestration
- [~] Fully event-driven RocketMQ architecture

Completion criteria:

- These items must not block the MVP path
- They stay deferred until Agent Core, Gateway, and Review flow are stable

## Interface Layering Rules

### Implemented in Code

- `UserService.login`
- `UserService.getUserProfile`
- `RagService.saveKnowledge`
- `RagService.updateReviewStatus`
- `RagService.searchKnowledge`
- `RagService.listPendingReviews`
- `AgentService.chat`
- `AgentService.switchState`
- `KnowledgeDTO`
- `ChatRequest`
- `UserLoginRequest`
- `ConversationState`
- `StateContext`
- Gateway HTTP endpoints
- Gateway SSE endpoint

### Defined but Still Partial

- `AgentToolRouter`
- In-memory session memory
- Review flow inside conversational sessions

### Planned Only

- `ReviewTask`
- `ReminderEvent`
- Citation/source/direct-answer response schema
- Unified authentication contract for Gateway

## Next Implementation Order

1. Complete Gateway auth and external API documentation
2. Complete review triggers, scheduling, and deduplication
3. Replace rule-based Agent orchestration with real LangChain4j model/tool execution
4. Keep Phase 7 deferred

Rationale:

- The MVP path is now available end-to-end, so the highest-value gaps are hardening and automation
- Review triggering still lacks scheduler/login integration
- Agent Core still needs true model execution, but Phase 7 should remain out of scope for now

## Current Evidence Pointers

- User side: `SysUser`, `SysUserMapper`, `SysUserServiceImpl`, user Flyway script
- RAG side: `KnowledgeCard`, `KnowledgeCardMapper`, `RagServiceImpl`, `MilvusConfig`, `V2__enhance_knowledge_card.sql`
- Agent Core side: `ConversationState`, `StateContext`, `AgentServiceImpl`, `AgentPromptService`, `DubboAgentToolRouter`
- Gateway side: `GatewayAuthController`, `GatewayAgentController`, `GatewayRagController`
- Verification: targeted tests `AgentServiceImplTest` and `GatewayAgentControllerTest`

## Maintenance Rules

- Mark `[x]` only when code/config/scripts or verification results support it
- Do not treat roadmap ideas or README narratives as proof of completion
- Re-check the three layers `implemented / partial / planned` whenever roadmap status is updated
