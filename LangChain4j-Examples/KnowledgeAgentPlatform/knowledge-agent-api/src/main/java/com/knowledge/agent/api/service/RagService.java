package com.knowledge.agent.api.service;

import com.knowledge.agent.api.dto.KnowledgeDTO;
import com.knowledge.agent.api.dto.ReviewTaskBatchDTO;
import com.knowledge.agent.api.dto.ReviewTaskStatus;
import com.knowledge.agent.api.dto.ReviewTriggerSource;
import com.knowledge.agent.common.resp.Result;

import java.util.List;
import java.util.Set;

/**
 * RAG 服务的 Dubbo 接口，负责知识写入、检索与复习相关能力。
 */
public interface RagService {

    /**
     * 保存知识内容到持久化存储与向量存储。
     */
    Result<Boolean> saveKnowledge(KnowledgeDTO dto);

    /**
     * 根据 SM-2 评分更新复习状态。
     */
    Result<Void> updateReviewStatus(Long id, int quality);

    /**
     * 为指定用户检索相关知识。
     */
    Result<List<KnowledgeDTO>> searchKnowledge(Long userId, String query, Integer limit);

    /**
     * 为指定用户检索相关知识，并支持标签过滤。
     */
    Result<List<KnowledgeDTO>> searchKnowledgeWithFilters(Long userId, String query, Integer limit, Set<String> tags);

    /**
     * 查询指定用户当前待复习的知识项。
     */
    Result<List<KnowledgeDTO>> listPendingReviews(Long userId, Integer limit);

    /**
     * 同步导入一小批知识文档。
     */
    Result<List<Long>> importKnowledgeBatch(Long userId, List<KnowledgeDTO> documents);

    /**
     * 持久化已生成的复习任务批次，供后续回查。
     */
    Result<Boolean> saveReviewTaskBatch(ReviewTaskBatchDTO batch);

    /**
     * 查询指定用户在某个触发来源下最近一次持久化的复习批次。
     */
    Result<ReviewTaskBatchDTO> findLatestReviewTaskBatch(Long userId, ReviewTriggerSource triggerSource);

    /**
     * 在复习结果回写后同步更新持久化任务状态。
     */
    Result<Boolean> updateReviewTaskStatus(Long userId, Long knowledgeId, ReviewTaskStatus status);
}
