package com.knowledge.agent.api.event;

import com.knowledge.agent.api.dto.ReviewTaskDTO;
import com.knowledge.agent.api.dto.ReviewTriggerSource;
import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * Gateway 生成复习批次后发布的事件。
 */
@Builder
public record ReviewBatchGeneratedEvent(
        /**
         * 批次所属用户 ID。
         */
        Long userId,
        /**
         * 触发来源。
         */
        ReviewTriggerSource triggerSource,
        /**
         * 请求时的期望任务数。
         */
        Integer requestedLimit,
        /**
         * 实际派发的任务数。
         */
        Integer dispatchedCount,
        /**
         * 本次批次中的任务列表。
         */
        List<ReviewTaskDTO> tasks,
        /**
         * 事件发生时间。
         */
        Instant occurredAt
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
