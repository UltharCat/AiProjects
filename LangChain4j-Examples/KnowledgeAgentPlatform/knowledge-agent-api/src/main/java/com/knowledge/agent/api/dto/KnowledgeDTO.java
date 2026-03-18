package com.knowledge.agent.api.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 知识数据传输对象，用于 Agent、RAG、Gateway 之间共享知识卡片信息。
 */
@Data
@Builder
public class KnowledgeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 知识文档唯一标识，对应 Milvus 与 MySQL 中的同一条知识记录。
     */
    private Long id;

    /**
     * 知识所属用户 ID。
     */
    private Long userId;

    /**
     * 知识摘要内容。
     */
    private String summary;

    /**
     * 知识来源，例如手动录入、导入文件或对话总结。
     */
    private String source;

    /**
     * 用于检索和过滤的标签集合。
     */
    private Set<String> tags;

    /**
     * 检索结果得分。
     */
    private Double score;

    /**
     * 返回给客户端的引用标识。
     */
    private String citation;

    /**
     * 在高置信命中时直接返回的答案。
     */
    private String directAnswer;

    /**
     * 检索命中的文本片段。
     */
    private String matchedSegment;

    /**
     * SM-2 复习算法中的易度因子。
     */
    private Double easinessFactor;

    /**
     * 下一次复习前的间隔天数。
     */
    private Integer intervalDays;

    /**
     * 已成功复习的重复次数。
     */
    private Integer repetition;

    /**
     * 下一次复习时间。
     */
    private LocalDateTime nextReviewDate;
}
