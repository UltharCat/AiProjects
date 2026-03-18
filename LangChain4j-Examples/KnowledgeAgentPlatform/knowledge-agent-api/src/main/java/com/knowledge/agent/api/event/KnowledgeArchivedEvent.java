package com.knowledge.agent.api.event;

import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

/**
 * 知识归档成功后发布的事件。
 */
@Builder
public record KnowledgeArchivedEvent(
        /**
         * 归档后的知识 ID。
         */
        Long knowledgeId,
        /**
         * 知识所属用户 ID。
         */
        Long userId,
        /**
         * 知识来源。
         */
        String source,
        /**
         * 知识标签集合。
         */
        Set<String> tags,
        /**
         * 事件发生时间。
         */
        Instant occurredAt
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
