package com.knowledge.agent.api.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Review task contract exposed by the Gateway for triggerable review flows.
 */
@Data
@Builder
public class ReviewTaskDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String taskId;

    private Long userId;

    private Long knowledgeId;

    private String summary;

    private LocalDateTime dueAt;

    private ReviewTriggerSource triggerSource;

    private ReviewTaskStatus status;

    private String dedupKey;
}
