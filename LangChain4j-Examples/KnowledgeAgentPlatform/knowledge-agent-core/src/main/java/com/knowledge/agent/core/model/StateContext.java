package com.knowledge.agent.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StateContext {

    private Long userId;

    private ConversationState currentState;

    private String activeTopic;

    private Long activeReviewKnowledgeId;

    @Builder.Default
    private List<ConversationTurn> turns = new ArrayList<>();

    private LocalDateTime updatedAt;
}
