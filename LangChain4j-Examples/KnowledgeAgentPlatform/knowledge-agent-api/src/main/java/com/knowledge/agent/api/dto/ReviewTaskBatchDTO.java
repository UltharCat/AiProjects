package com.knowledge.agent.api.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 复习任务批次传输对象，用于跨服务传递已生成或已持久化的批次信息。
 */
@Data
@Builder
public class ReviewTaskBatchDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 复习批次唯一标识。
     */
    private String batchId;

    /**
     * 批次所属用户 ID。
     */
    private Long userId;

    /**
     * 触发该批次的来源。
     */
    private ReviewTriggerSource triggerSource;

    /**
     * 批次请求时的期望任务数量。
     */
    private Integer requestedLimit;

    /**
     * 实际派发出的任务数量。
     */
    private Integer dispatchedCount;

    /**
     * 批次创建时间。
     */
    private LocalDateTime createdAt;

    /**
     * 批次中包含的复习任务列表。
     */
    private List<ReviewTaskDTO> tasks;
}
