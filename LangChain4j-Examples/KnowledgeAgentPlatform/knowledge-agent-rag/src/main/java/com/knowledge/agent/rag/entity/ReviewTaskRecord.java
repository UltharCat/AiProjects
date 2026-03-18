package com.knowledge.agent.rag.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Durable review-task record persisted for login/manual/scheduled batches.
 */
@Data
@Builder
@TableName("review_task_record")
public class ReviewTaskRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private String taskId;

    private String batchId;

    private Long userId;

    private Long knowledgeId;

    private String summary;

    private LocalDateTime dueAt;

    private String triggerSource;

    private String status;

    private String dedupKey;

    private Integer requestedLimit;

    private LocalDateTime batchCreatedAt;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
