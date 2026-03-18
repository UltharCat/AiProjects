package com.knowledge.agent.gateway.model;

import com.knowledge.agent.api.dto.ReviewTaskDTO;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

/**
 * Login response returned by the Gateway after successful authentication.
 */
@Builder
public record GatewayLoginResponse(
        Long userId,
        String accessToken,
        String tokenType,
        Instant expiresAt,
        Integer pendingReviewCount,
        List<ReviewTaskDTO> pendingReviewTasks
) {
}
