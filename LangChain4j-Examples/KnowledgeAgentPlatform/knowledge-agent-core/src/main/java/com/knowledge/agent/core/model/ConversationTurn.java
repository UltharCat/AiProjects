package com.knowledge.agent.core.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ConversationTurn {

    private String role;

    private String content;

    private LocalDateTime createdAt;
}
