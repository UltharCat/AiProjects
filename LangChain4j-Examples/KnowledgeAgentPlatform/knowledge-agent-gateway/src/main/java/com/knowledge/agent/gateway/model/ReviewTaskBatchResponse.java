package com.knowledge.agent.gateway.model;

import com.knowledge.agent.api.dto.ReviewTaskDTO;
import com.knowledge.agent.api.dto.ReviewTriggerSource;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Review task batch exposed to clients and future scheduler integration.
 */
@Builder
public record ReviewTaskBatchResponse(
        String batchId,
        Long userId,
        ReviewTriggerSource triggerSource,
        Integer requestedLimit,
        Integer dispatchedCount,
        LocalDateTime createdAt,
        List<ReviewTaskDTO> tasks
) {
}
