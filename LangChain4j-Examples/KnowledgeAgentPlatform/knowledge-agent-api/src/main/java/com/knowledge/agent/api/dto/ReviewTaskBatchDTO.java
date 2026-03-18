package com.knowledge.agent.api.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Cross-service contract for persisted review task batches.
 */
@Data
@Builder
public class ReviewTaskBatchDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String batchId;

    private Long userId;

    private ReviewTriggerSource triggerSource;

    private Integer requestedLimit;

    private Integer dispatchedCount;

    private LocalDateTime createdAt;

    private List<ReviewTaskDTO> tasks;
}
