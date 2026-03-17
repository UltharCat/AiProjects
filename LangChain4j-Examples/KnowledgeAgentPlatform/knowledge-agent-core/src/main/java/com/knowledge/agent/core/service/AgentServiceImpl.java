package com.knowledge.agent.core.service;

import cn.hutool.core.util.StrUtil;
import com.knowledge.agent.api.dto.KnowledgeDTO;
import com.knowledge.agent.api.service.AgentService;
import com.knowledge.agent.common.exception.BizException;
import com.knowledge.agent.common.resp.Result;
import com.knowledge.agent.core.model.ConversationState;
import com.knowledge.agent.core.model.ConversationTurn;
import com.knowledge.agent.core.model.StateContext;
import com.knowledge.agent.core.prompt.AgentPromptService;
import com.knowledge.agent.core.store.InMemoryConversationStore;
import com.knowledge.agent.core.tool.AgentToolRouter;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

@Slf4j
@Service
@DubboService
public class AgentServiceImpl implements AgentService {

    private static final int DEFAULT_RETRIEVE_LIMIT = 3;

    private final InMemoryConversationStore conversationStore;
    private final AgentPromptService promptService;
    private final AgentToolRouter toolRouter;

    public AgentServiceImpl(InMemoryConversationStore conversationStore,
                            AgentPromptService promptService,
                            AgentToolRouter toolRouter) {
        this.conversationStore = conversationStore;
        this.promptService = promptService;
        this.toolRouter = toolRouter;
    }

    @Override
    public Result<String> chat(Long userId, String query) {
        if (userId == null || StrUtil.isBlank(query)) {
            throw new BizException(400, "userId and query must not be blank");
        }

        StateContext context = conversationStore.getOrCreate(userId);
        appendTurn(context, "user", query);

        String response;
        OptionalInt reviewQuality = extractReviewQuality(query);
        if (context.getCurrentState() == ConversationState.REVIEW && reviewQuality.isPresent() && context.getActiveReviewKnowledgeId() != null) {
            toolRouter.updateReviewStatus(context.getActiveReviewKnowledgeId(), reviewQuality.getAsInt());
            context.setCurrentState(ConversationState.IDLE);
            context.setActiveReviewKnowledgeId(null);
            response = "Review quality %d received. The next review time has been updated. Say 'review' to continue.".formatted(reviewQuality.getAsInt());
        } else {
            ConversationState nextState = determineNextState(context, query);
            context.setCurrentState(nextState);
            response = switch (nextState) {
                case IDLE -> buildIdleResponse(query);
                case TEACHING -> buildTeachingResponse(context, query);
                case REVIEW -> buildReviewResponse(context);
                case SUMMARY -> buildSummaryResponse(context, query);
            };
        }

        appendTurn(context, "assistant", response);
        conversationStore.save(context);
        return Result.success(response);
    }

    @Override
    public Result<Void> switchState(Long userId, String targetState) {
        if (userId == null || StrUtil.isBlank(targetState)) {
            throw new BizException(400, "userId and targetState must not be blank");
        }
        StateContext context = conversationStore.getOrCreate(userId);
        try {
            context.setCurrentState(ConversationState.valueOf(targetState.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new BizException(400, "Unsupported state: %s".formatted(targetState));
        }
        conversationStore.save(context);
        return Result.success(null);
    }

    private ConversationState determineNextState(StateContext context, String query) {
        if (containsAny(query, "summary", "summarize", "archive", "save knowledge")) {
            return ConversationState.SUMMARY;
        }
        if (containsAny(query, "review", "quiz me", "test me")) {
            return ConversationState.REVIEW;
        }
        if (containsAny(query, "why", "how", "explain", "introduce", "what", "?")) {
            return ConversationState.TEACHING;
        }
        if (context.getCurrentState() == ConversationState.TEACHING && context.getTurns().size() < 12) {
            return ConversationState.TEACHING;
        }
        return ConversationState.IDLE;
    }

    private String buildIdleResponse(String query) {
        return """
                Current state: IDLE
                Suggested actions: ask a knowledge question, say 'summary' to archive the current dialog, or say 'review' to start a review session.
                Last input: %s
                """.formatted(query.trim());
    }

    private String buildTeachingResponse(StateContext context, String query) {
        String profile = toolRouter.getUserProfile(context.getUserId());
        List<KnowledgeDTO> knowledgeList = toolRouter.searchKnowledge(context.getUserId(), query, DEFAULT_RETRIEVE_LIMIT);
        context.setActiveTopic(query);
        String instruction = promptService.buildInstruction(ConversationState.TEACHING, profile);
        return """
                Current state: TEACHING
                State instruction: %s
                User profile: %s
                Related knowledge:
                %s
                Next step: continue by narrowing '%s' into one or two smaller questions.
                """.formatted(instruction, profile, formatKnowledgeList(knowledgeList), query.trim());
    }

    private String buildReviewResponse(StateContext context) {
        List<KnowledgeDTO> reviews = toolRouter.listPendingReviews(context.getUserId(), 1);
        if (reviews.isEmpty()) {
            context.setCurrentState(ConversationState.IDLE);
            return """
                    Current state: REVIEW
                    There are no due review cards right now.
                    You can ask a new question or request a summary of the current dialog.
                    """;
        }

        KnowledgeDTO review = reviews.get(0);
        context.setActiveReviewKnowledgeId(review.getId());
        context.setActiveTopic(review.getSummary());
        return """
                Current state: REVIEW
                Review prompt: explain the following concept in your own words without looking at the answer.
                %s

                After answering, send a self-evaluated quality score from 0 to 5.
                """.formatted(review.getSummary());
    }

    private String buildSummaryResponse(StateContext context, String query) {
        String summary = buildSummaryText(context, query);
        boolean saved = toolRouter.saveKnowledge(context.getUserId(), summary, buildTags(context, query));
        context.setCurrentState(ConversationState.IDLE);
        return """
                Current state: SUMMARY
                Archive result: %s
                Knowledge summary:
                %s
                """.formatted(saved ? "saved to knowledge storage" : "save failed, please retry later", summary);
    }

    private String buildSummaryText(StateContext context, String query) {
        List<ConversationTurn> turns = context.getTurns();
        int fromIndex = Math.max(0, turns.size() - 6);
        String recent = turns.subList(fromIndex, turns.size()).stream()
                .map(turn -> turn.getRole() + ": " + turn.getContent())
                .reduce((left, right) -> left + "\n" + right)
                .orElse(query);
        return """
                Topic: %s
                Recent dialog:
                %s

                Current summary: %s
                """.formatted(StrUtil.blankToDefault(context.getActiveTopic(), "unnamed topic"), recent, query.trim());
    }

    private Set<String> buildTags(StateContext context, String query) {
        Set<String> tags = new LinkedHashSet<>();
        tags.add("summary");
        if (StrUtil.isNotBlank(context.getActiveTopic())) {
            tags.add(context.getActiveTopic().trim());
        }
        if (StrUtil.isNotBlank(query)) {
            tags.add(query.trim());
        }
        return tags;
    }

    private String formatKnowledgeList(List<KnowledgeDTO> knowledgeList) {
        if (knowledgeList == null || knowledgeList.isEmpty()) {
            return "- No matching knowledge cards were found. Continue with direct explanation.";
        }
        StringBuilder builder = new StringBuilder();
        for (KnowledgeDTO dto : knowledgeList) {
            builder.append("- [").append(dto.getId()).append("] ")
                    .append(StrUtil.blankToDefault(dto.getSummary(), "No summary"))
                    .append(System.lineSeparator());
        }
        return builder.toString().trim();
    }

    private OptionalInt extractReviewQuality(String query) {
        String trimmed = query == null ? "" : query.trim();
        if (trimmed.matches("[0-5]")) {
            return OptionalInt.of(Integer.parseInt(trimmed));
        }
        if (trimmed.matches(".*\\b([0-5])\\b.*")) {
            String value = trimmed.replaceAll(".*\\b([0-5])\\b.*", "$1");
            return OptionalInt.of(Integer.parseInt(value));
        }
        return OptionalInt.empty();
    }

    private boolean containsAny(String query, String... keywords) {
        if (StrUtil.isBlank(query)) {
            return false;
        }
        String lower = query.toLowerCase();
        for (String keyword : keywords) {
            if (lower.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private void appendTurn(StateContext context, String role, String content) {
        context.getTurns().add(ConversationTurn.builder()
                .role(role)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build());
        log.debug("conversation turn appended, userId={}, state={}, role={}", context.getUserId(), context.getCurrentState(), role);
    }
}
