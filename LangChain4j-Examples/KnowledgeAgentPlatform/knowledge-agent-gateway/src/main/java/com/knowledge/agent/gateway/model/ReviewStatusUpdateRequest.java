package com.knowledge.agent.gateway.model;

import java.io.Serializable;

/**
 * External request for updating review quality feedback.
 */
public record ReviewStatusUpdateRequest(
        Long knowledgeId,
        Integer quality
) implements Serializable {
}
