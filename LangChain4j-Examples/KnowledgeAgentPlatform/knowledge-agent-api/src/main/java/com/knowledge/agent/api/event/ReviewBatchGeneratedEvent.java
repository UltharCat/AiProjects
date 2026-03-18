package com.knowledge.agent.api.event;

import com.knowledge.agent.api.dto.ReviewTaskDTO;
import com.knowledge.agent.api.dto.ReviewTriggerSource;
import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * Event emitted when the Gateway generates a review batch for a user.
 */
@Builder
public record ReviewBatchGeneratedEvent(
        Long userId,
        ReviewTriggerSource triggerSource,
        Integer requestedLimit,
        Integer dispatchedCount,
        List<ReviewTaskDTO> tasks,
        Instant occurredAt
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
