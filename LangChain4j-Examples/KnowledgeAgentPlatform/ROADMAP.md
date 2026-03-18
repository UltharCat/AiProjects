# Knowledge Agent Platform Roadmap

This roadmap is calibrated against the current code, configuration, and verification results rather than vision-only descriptions.

> Scan date: 2026-03-18  
> Project path: `F:\JavaProjects\AiProjects\LangChain4j-Examples\KnowledgeAgentPlatform`  
> Build result: `mvn -DskipTests compile` passed  
> Verification note: `mvn test` passed on 2026-03-18 after adding Gateway auth/review/scheduler tests

## Current Verdict

- `Phase 0-5`: complete by current code and verification evidence
- `Phase 6`: functionally closed-loop but still partial
- `Phase 7`: deferred by design

Reasoning:

- Review dispatch, deduplication, durable batch persistence, latest-batch recall, and review-status write-back are now all implemented.
- The remaining gap is not “missing review loop capability”; it is that Redis-backed deduplication and batch caching are still only partially completed and not yet fully hardened as a stable runtime layer.

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
- [-] Redis-backed cache integration with in-memory fallback
- [x] RocketMQ topics, producer, and consumer baseline wiring

Completion criteria:

- Items marked `[x]` must be backed by files in the repository
- RocketMQ baseline events are now wired for knowledge archival and review-batch publication/consumption
- Redis-backed deduplication, batch caching, and conversation persistence are available with explicit fallback behavior, but broader runtime validation is still incomplete

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
- [x] Contract tests across service boundaries
- [x] Unified error/status code conventions

Completion criteria:

- Shared types and interfaces are present in source form
- Shared DTOs, JWT helpers, error codes, and contract-level tests are all present in source form

## Phase 2: User Service

- [x] `sys_user` schema
- [x] `SysUser` entity and `SysUserMapper`
- [x] `login`
- [x] `getUserProfile`
- [x] JWT-based authentication
- [x] Login session model
- [x] Real Gateway-facing login session response with token metadata and pending reviews
- [x] Expanded user preference model

Completion criteria:

- Dubbo provider capabilities exist
- Token issuance is now based on signed JWTs
- User HTTP login/profile endpoints and structured preference DTOs are now available
- Token revocation and longer-lived session management remain future hardening work

## Phase 3: RAG Service

- [x] `knowledge_card` schema
- [x] `KnowledgeCard` entity and mapper
- [x] Milvus hybrid schema initialization
- [x] Dense + sparse + RRF retrieval foundation
- [x] `saveKnowledge`
- [x] `updateReviewStatus`
- [x] Standard search API foundation: `searchKnowledge`
- [x] Review query foundation: `listPendingReviews`
- [x] Knowledge card metadata expanded with `userId/summary/tags/source`
- [x] Document import pipeline
- [x] Tag filtering strategy
- [x] Citation/source return payload
- [x] Direct-answer strategy
- [x] Batch import tooling

Completion criteria:

- The service now covers archival write, review status update, search, and pending-review query
- RAG now exposes import, tag-filtered search, and richer retrieval response metadata
- Further hardening can focus on retrieval quality and larger-batch operational tooling instead of missing core contracts

## Phase 4: Agent Core MVP

- [x] `ConversationState`
- [x] `StateContext`
- [x] Prompt assembly service
- [-] Tool routing foundation via `AgentToolRouter` and Dubbo orchestration
- [x] `AgentService` provider implementation
- [x] User/RAG Dubbo integration
- [-] Session memory store now prefers Redis with in-memory fallback, but cross-instance replay semantics still need broader validation
- [-] Rule-based state transitions now have LangChain4j teaching/summary runtime integration with fallback behavior

Completion criteria:

- `chat` is implemented and reachable through Dubbo
- Base states `IDLE/TEACHING/REVIEW/SUMMARY` are supported
- User profile + RAG + response orchestration is closed-loop for the MVP path
- This phase remains partial until model/tool orchestration goes beyond the current teaching/summary entrypoints and Redis behavior is validated in fuller runtime scenarios

## Phase 5: Gateway / BFF

- [x] Startup class and base config
- [x] HTTP controllers
- [x] Minimal SSE endpoint
- [x] Agent/User/RAG route aggregation
- [x] Unified bearer-token authentication for non-login endpoints
- [x] External API documentation

Completion criteria:

- Gateway is no longer just a shell module
- The Gateway hardening milestone is complete for the current stage

## Phase 6: Review Scheduling Loop

- [x] Due review card query
- [x] `ReviewTask` model
- [x] Login-triggered review reminder
- [x] Scheduled review list generation entrypoint
- [-] Redis-backed dedup, durable batch persistence, and scheduler loop with event publication
- [x] Agent-driven review questioning within the conversation flow
- [x] Review result write-back

Completion criteria:

- The conversational review loop exists for manually triggered review sessions
- Login-triggered review dispatch, scheduler registration, and dedup hooks now exist
- Review task batches are now persisted durably and can be read back by the latest scheduled-batch query
- This phase remains incomplete because Redis-backed deduplication and batch caching are still marked partial in the roadmap itself and are not yet fully validated as a stable runtime layer

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

- `ReminderEvent`
- Citation/source/direct-answer response schema

## Next Implementation Order

1. Harden Redis-backed review deduplication, batch caching, and cache-to-persistence boundaries
2. Add clearer recovery and observability behavior around review batch generation and status write-back
3. Extend LangChain4j runtime integration from teaching/summary fallback to fuller tool-driven orchestration
4. Keep Phase 7 deferred

Rationale:

- The MVP path is now available end-to-end, so the highest-value gaps are hardening and automation
- Review triggering now has login integration, scheduler execution, Redis-first dedup/batch caching, durable batch persistence, and event publication hooks
- Agent Core now has initial LangChain4j runtime integration, but fuller tool-driven orchestration is still ahead

## Next Stage Development Goals

Current next-stage focus: finish the unfinished Redis-backed review runtime baseline and improve the current LangChain4j-powered orchestration layer.

### Goal A: Keep the Gateway hardening baseline stable

- Maintain unified request authentication for all non-login endpoints
- Keep the token/header convention stable across login, chat, search, and review APIs
- Evolve external API documentation alongside endpoint changes
- Continue normalizing request/response and error handling at the Gateway boundary

### Goal B: Keep the review flow stable after closing the basic trigger loop

- Keep durable review task persistence stable for login/manual/scheduled batches
- Clarify runtime boundaries between Redis dedup/cache and durable batch persistence
- Validate the fallback path when Redis is unavailable and the system falls back to in-memory behavior
- Keep login-triggered pending review lookup aligned with scheduled dispatch behavior

### Goal C: Keep Agent Core stable while deferring full model execution

- Preserve the current Dubbo orchestration path as the fallback implementation
- Avoid expanding Phase 7 scope into graph, multimodal, or workflow features
- Keep LangChain4j runtime integration moving from point features to fuller tool-driven execution without dragging in Phase 7 scope

### Exit Criteria for the Next Stage

- Gateway endpoints except login are guarded by a unified auth mechanism
- The project exposes a documented external API surface for login/chat/search/review
- A user can trigger pending review retrieval immediately after login
- Review trigger and scheduling contracts are persisted durably enough for the following stage
- Latest scheduled review batches can be recalled from durable storage without relying solely on cache
- `mvn test` stays green after each hardening change

## API Verification Cases

### Login

1. Call `POST /api/auth/login`
Expected result: returns `accessToken`, `pendingReviewCount`, and `pendingReviewTasks`.

### Pending Review Batch

1. Call `GET /api/reviews/pending?limit=5` with `Authorization: Bearer <accessToken>`
Expected result: returns a `batchId`, `triggerSource=MANUAL`, and a task list.

### Latest Scheduled Batch

1. Call `GET /api/reviews/scheduled/latest` with `Authorization: Bearer <accessToken>`
Expected result: returns the latest `SCHEDULED` batch when present, or an empty batch structure when absent.

2. Disable or bypass Redis, then call the same endpoint after a batch is generated
Expected result: the endpoint can still fall back to durable storage or the current runtime fallback path without crashing.

### Review Status Write-back

1. Call `PATCH /api/reviews/status` with a valid `knowledgeId` and `quality`
Expected result: review parameters are updated and the corresponding review task is marked as completed.

### RAG Search

1. Call `GET /api/rag/search?query=sm2&limit=3&tags=memory` with `Authorization: Bearer <accessToken>`
Expected result: each result can include `source`, `citation`, `directAnswer`, and `matchedSegment`, and tag filtering works.

### Agent Chat

1. Call `POST /api/agent/chat`
2. Call `GET /api/agent/chat/stream?prompt=review`
Expected result: standard chat returns the unified response model and the SSE endpoint emits at least one `message` event.

## Current Evidence Pointers

- User side: `SysUser`, `SysUserMapper`, `SysUserServiceImpl`, user Flyway script
- RAG side: `KnowledgeCard`, `KnowledgeCardMapper`, `ReviewTaskRecord`, `ReviewTaskRecordMapper`, `RagServiceImpl`, `MilvusConfig`, `V2__enhance_knowledge_card.sql`, `V3__add_knowledge_source.sql`, `V4__add_review_task_record.sql`
- Agent Core side: `ConversationState`, `StateContext`, `AgentServiceImpl`, `AgentPromptService`, `DubboAgentToolRouter`, `LangChain4jAgentRuntime`, `RedisConversationStore`
- Gateway side: `GatewayAuthController`, `GatewayAgentController`, `GatewayRagController`, `GatewayReviewController`, auth interceptor/config, review dispatcher, scheduler, Redis-backed dedup/batch store, review-batch event publisher
- Documentation: `docs/gateway-api.zh-CN.md`
- Verification: `mvn test`, plus Gateway auth/login/review unit tests

## Maintenance Rules

- Mark `[x]` only when code/config/scripts or verification results support it
- Do not treat roadmap ideas or README narratives as proof of completion
- Re-check the three layers `implemented / partial / planned` whenever roadmap status is updated
