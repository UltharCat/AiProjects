package com.knowledge.agent.gateway.review;

import com.knowledge.agent.api.dto.KnowledgeDTO;
import com.knowledge.agent.api.dto.ReviewTaskDTO;
import com.knowledge.agent.api.dto.ReviewTriggerSource;
import com.knowledge.agent.api.service.RagService;
import com.knowledge.agent.common.exception.BizException;
import com.knowledge.agent.common.resp.Result;
import com.knowledge.agent.gateway.model.ReviewTaskBatchResponse;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Builds Gateway-facing review task batches from due knowledge cards.
 */
@Service
public class ReviewTaskDispatcher {

    @DubboReference(check = false)
    private RagService ragService;

    private final ReviewTaskDeduplicator reviewTaskDeduplicator;

    private final ReviewTaskBatchStore reviewTaskBatchStore;

    public ReviewTaskDispatcher(ReviewTaskDeduplicator reviewTaskDeduplicator,
                                ReviewTaskBatchStore reviewTaskBatchStore) {
        this.reviewTaskDeduplicator = reviewTaskDeduplicator;
        this.reviewTaskBatchStore = reviewTaskBatchStore;
    }

    public ReviewTaskBatchResponse dispatch(Long userId, Integer limit, ReviewTriggerSource triggerSource) {
        int requestedLimit = limit == null ? 5 : Math.max(1, limit);
        Result<List<KnowledgeDTO>> result = ragService.listPendingReviews(userId, requestedLimit);
        if (result == null) {
            throw new BizException(500, "RAG service returned no response");
        }
        if (!Objects.equals(result.getCode(), 200)) {
            throw new BizException(result.getCode(), result.getMessage());
        }

        List<ReviewTaskDTO> tasks = result.getData() == null ? Collections.emptyList() : result.getData().stream()
                .filter(dto -> dto.getId() != null)
                .filter(dto -> reviewTaskDeduplicator.tryAcquire(triggerSource, userId, dto.getId()))
                .map(dto -> toTask(dto, userId, triggerSource))
                .collect(Collectors.toList());

        ReviewTaskBatchResponse batch = ReviewTaskBatchResponse.builder()
                .userId(userId)
                .triggerSource(triggerSource)
                .requestedLimit(requestedLimit)
                .dispatchedCount(tasks.size())
                .tasks(tasks)
                .build();
        if (triggerSource != ReviewTriggerSource.MANUAL) {
            // 关键步骤：把登录/调度触发的批次缓存下来，后续可以按来源回查最近一次结果。
            reviewTaskBatchStore.saveBatch(batch);
        }
        return batch;
    }

    private ReviewTaskDTO toTask(KnowledgeDTO dto, Long userId, ReviewTriggerSource triggerSource) {
        return ReviewTaskDTO.builder()
                .taskId(UUID.randomUUID().toString())
                .userId(userId)
                .knowledgeId(dto.getId())
                .summary(dto.getSummary())
                .dueAt(dto.getNextReviewDate())
                .triggerSource(triggerSource)
                .status("PENDING")
                .dedupKey(triggerSource + ":" + userId + ":" + dto.getId())
                .build();
    }
}
