package com.knowledge.agent.api.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Core knowledge transfer object shared between Agent and RAG modules.
 */
@Data
@Builder
public class KnowledgeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Vector document id associated with Milvus content.
     */
    private Long id;

    /**
     * Owner user id.
     */
    private Long userId;

    /**
     * Summarized knowledge content.
     */
    private String summary;

    /**
     * Original or normalized source descriptor for the knowledge item.
     */
    private String source;

    /**
     * Tags used for retrieval or filtering.
     */
    private Set<String> tags;

    /**
     * Retrieval score for ranked results.
     */
    private Double score;

    /**
     * Citation content returned to the client.
     */
    private String citation;

    /**
     * Direct answer generated from the highest-confidence match.
     */
    private String directAnswer;

    /**
     * Segment text matched during retrieval.
     */
    private String matchedSegment;

    /**
     * Easiness factor used by the SM-2 algorithm.
     */
    private Double easinessFactor;

    /**
     * Review interval in days.
     */
    private Integer intervalDays;

    /**
     * Review repetition count.
     */
    private Integer repetition;

    /**
     * Scheduled time for the next review.
     */
    private LocalDateTime nextReviewDate;
}
