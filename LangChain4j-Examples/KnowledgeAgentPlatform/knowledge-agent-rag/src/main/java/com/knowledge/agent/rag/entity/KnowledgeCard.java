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
 * Persistent review card mapped to the knowledge_card table.
 */
@Data
@Builder
@TableName(value = "knowledge_card", autoResultMap = true)
public class KnowledgeCard implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Document id associated with vector records.
     */
    @TableId(type = IdType.INPUT)
    private Long docId;

    /**
     * Owner user id.
     */
    private Long userId;

    /**
     * Normalized knowledge summary.
     */
    private String summary;

    /**
     * Source descriptor such as manual summary or imported document path.
     */
    private String source;

    /**
     * Tags encoded as JSON.
     */
    private String tagsJson;

    /**
     * SM-2 easiness factor.
     */
    private Double easinessFactor;

    /**
     * Review interval in days.
     */
    private Integer intervalDays;

    /**
     * Number of successful repetitions.
     */
    private Integer repetition;

    /**
     * Next review timestamp.
     */
    private LocalDateTime nextReviewDate;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * Logical delete flag.
     */
    private Integer deleted;
}
