package com.knowledge.agent.api.service;

import com.knowledge.agent.api.dto.KnowledgeDTO;
import com.knowledge.agent.common.resp.Result;

import java.util.List;
import java.util.Set;

/**
 * Dubbo contract for knowledge storage and retrieval.
 */
public interface RagService {

    /**
     * Save knowledge into both persistent metadata storage and vector storage.
     */
    Result<Boolean> saveKnowledge(KnowledgeDTO dto);

    /**
     * Update review status using SM-2 feedback.
     */
    Result<Void> updateReviewStatus(Long id, int quality);

    /**
     * Search relevant knowledge for a user.
     */
    Result<List<KnowledgeDTO>> searchKnowledge(Long userId, String query, Integer limit);

    /**
     * Search relevant knowledge for a user with optional tag filters.
     */
    Result<List<KnowledgeDTO>> searchKnowledgeWithFilters(Long userId, String query, Integer limit, Set<String> tags);

    /**
     * List pending review items for a user.
     */
    Result<List<KnowledgeDTO>> listPendingReviews(Long userId, Integer limit);

    /**
     * Import a small batch of knowledge documents synchronously.
     */
    Result<List<Long>> importKnowledgeBatch(Long userId, List<KnowledgeDTO> documents);
}
