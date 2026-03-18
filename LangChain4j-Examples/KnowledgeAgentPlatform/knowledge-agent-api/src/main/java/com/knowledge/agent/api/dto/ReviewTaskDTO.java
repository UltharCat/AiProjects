package com.knowledge.agent.api.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 复习任务传输对象，描述单条待复习任务的核心信息。
 */
@Data
@Builder
public class ReviewTaskDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 复习任务唯一标识。
     */
    private String taskId;

    /**
     * 任务所属用户 ID。
     */
    private Long userId;

    /**
     * 关联的知识卡片 ID。
     */
    private Long knowledgeId;

    /**
     * 复习时展示的知识摘要。
     */
    private String summary;

    /**
     * 任务到期时间。
     */
    private LocalDateTime dueAt;

    /**
     * 任务触发来源。
     */
    private ReviewTriggerSource triggerSource;

    /**
     * 当前任务状态。
     */
    private ReviewTaskStatus status;

    /**
     * 用于去重的业务键。
     */
    private String dedupKey;
}
