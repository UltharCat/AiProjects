package com.knowledge.agent.gateway.review;

import com.knowledge.agent.api.dto.ReviewTriggerSource;
import com.knowledge.agent.gateway.model.ReviewTaskBatchResponse;

import java.util.Optional;

/**
 * Stores generated review batches for later retrieval.
 */
public interface ReviewTaskBatchStore {

    void saveBatch(ReviewTaskBatchResponse batch);

    Optional<ReviewTaskBatchResponse> findLatest(Long userId, ReviewTriggerSource triggerSource);
}
