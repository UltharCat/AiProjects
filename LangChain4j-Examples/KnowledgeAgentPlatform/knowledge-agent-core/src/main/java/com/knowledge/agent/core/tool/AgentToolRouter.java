package com.knowledge.agent.core.tool;

import com.knowledge.agent.api.dto.KnowledgeDTO;

import java.util.List;
import java.util.Set;

public interface AgentToolRouter {

    String getUserProfile(Long userId);

    List<KnowledgeDTO> searchKnowledge(Long userId, String query, int limit);

    List<KnowledgeDTO> listPendingReviews(Long userId, int limit);

    boolean saveKnowledge(Long userId, String summary, Set<String> tags);

    void updateReviewStatus(Long knowledgeId, int quality);
}
