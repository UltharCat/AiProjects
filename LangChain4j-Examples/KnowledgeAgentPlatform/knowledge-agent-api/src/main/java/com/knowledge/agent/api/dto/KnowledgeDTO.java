package com.knowledge.agent.api.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 知识点核心传输对象
 * 用于 Agent 与 RAG 模块之间的数据交互
 */
@Data
@Builder
public class KnowledgeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 向量ID (关联 Milvus 中的向量记录，代表切片后的知识点)
     */
    private Long id;

    /**
     * 对话知识总结
     */
    private String summary;

    /**
     * 标签集合 (用于混合检索或过滤)
     */
    private Set<String> tags;

    // --- 艾宾浩斯记忆参数 ---

    /**
     * 难度因子 (Easiness Factor)，默认 2.5
     */
    private Double easinessFactor;

    /**
     * 复习间隔天数
     */
    private Integer intervalDays;

    /**
     * 已复习次数
     */
    private Integer repetition;

    /**
     * 下次复习时间
     */
    private LocalDateTime nextReviewDate;
}
