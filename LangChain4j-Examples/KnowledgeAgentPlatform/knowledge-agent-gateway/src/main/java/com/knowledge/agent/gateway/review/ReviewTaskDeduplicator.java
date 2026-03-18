package com.knowledge.agent.gateway.review;

import com.knowledge.agent.api.dto.ReviewTriggerSource;

/**
 * Deduplication hook for trigger-driven review task dispatching.
 */
public interface ReviewTaskDeduplicator {

    boolean tryAcquire(ReviewTriggerSource triggerSource, Long userId, Long knowledgeId);
}
