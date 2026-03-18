package com.knowledge.agent.api.event;

import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

/**
 * Event emitted after knowledge content has been archived successfully.
 */
@Builder
public record KnowledgeArchivedEvent(
        Long knowledgeId,
        Long userId,
        String source,
        Set<String> tags,
        Instant occurredAt
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
