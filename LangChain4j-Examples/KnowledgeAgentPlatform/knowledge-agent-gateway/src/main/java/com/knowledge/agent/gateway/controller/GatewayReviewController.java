package com.knowledge.agent.gateway.controller;

import com.knowledge.agent.api.dto.KnowledgeDTO;
import com.knowledge.agent.api.dto.ReviewTaskBatchDTO;
import com.knowledge.agent.api.dto.ReviewTaskStatus;
import com.knowledge.agent.api.dto.ReviewTriggerSource;
import com.knowledge.agent.api.service.RagService;
import com.knowledge.agent.common.exception.BizException;
import com.knowledge.agent.common.resp.Result;
import com.knowledge.agent.gateway.auth.GatewayUserContext;
import com.knowledge.agent.gateway.model.ReviewStatusUpdateRequest;
import com.knowledge.agent.gateway.model.ReviewTaskBatchResponse;
import com.knowledge.agent.gateway.review.ReviewTaskBatchStore;
import com.knowledge.agent.gateway.review.ReviewTaskDispatcher;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * review 相关对外接口，包括手动获取批次、查询最近调度结果和回写复习状态。
 */
@RestController
@RequestMapping("/api/reviews")
public class GatewayReviewController {

    @DubboReference(check = false)
    private RagService ragService;

    private final ReviewTaskDispatcher reviewTaskDispatcher;

    private final ReviewTaskBatchStore reviewTaskBatchStore;

    public GatewayReviewController(ReviewTaskDispatcher reviewTaskDispatcher,
                                   ReviewTaskBatchStore reviewTaskBatchStore) {
        this.reviewTaskDispatcher = reviewTaskDispatcher;
        this.reviewTaskBatchStore = reviewTaskBatchStore;
    }

    @GetMapping("/pending")
    public Result<ReviewTaskBatchResponse> pendingReviews(@RequestParam(defaultValue = "5") Integer limit) {
        return Result.success(reviewTaskDispatcher.dispatch(
                GatewayUserContext.requireUserId(),
                limit,
                ReviewTriggerSource.MANUAL
        ));
    }

    @GetMapping("/scheduled/latest")
    public Result<ReviewTaskBatchResponse> latestScheduledBatch() {
        Long userId = GatewayUserContext.requireUserId();

        // 优先读取持久化批次，降低最近调度结果对缓存命中的依赖。
        Result<ReviewTaskBatchDTO> persistedResult = ragService.findLatestReviewTaskBatch(userId, ReviewTriggerSource.SCHEDULED);
        if (persistedResult != null && Objects.equals(persistedResult.getCode(), 200) && persistedResult.getData() != null) {
            return Result.success(toResponse(persistedResult.getData()));
        }

        // 持久化批次缺失时，再退回到当前运行时缓存。
        return Result.success(reviewTaskBatchStore.findLatest(
                userId,
                ReviewTriggerSource.SCHEDULED
        ).orElse(ReviewTaskBatchResponse.builder()
                .userId(userId)
                .triggerSource(ReviewTriggerSource.SCHEDULED)
                .requestedLimit(0)
                .dispatchedCount(0)
                .createdAt(null)
                .tasks(List.of())
                .build()));
    }

    @PatchMapping("/status")
    public Result<Void> updateReviewStatus(@RequestBody ReviewStatusUpdateRequest request) {
        Long userId = GatewayUserContext.requireUserId();

        // 先校验知识项是否属于当前用户的待复习集合，避免越权写回。
        Result<List<KnowledgeDTO>> pendingReviews = ragService.listPendingReviews(userId, 200);
        boolean allowed = pendingReviews != null
                && Objects.equals(pendingReviews.getCode(), 200)
                && pendingReviews.getData() != null
                && pendingReviews.getData().stream().map(KnowledgeDTO::getId).anyMatch(request.knowledgeId()::equals);
        if (!allowed) {
            throw new BizException(403, "Review item is not available for the current user");
        }

        Result<Void> updateResult = ragService.updateReviewStatus(request.knowledgeId(), request.quality());
        if (!Objects.equals(updateResult.getCode(), 200)) {
            return updateResult;
        }

        // 复习参数更新成功后，再同步推进持久化任务状态。
        ragService.updateReviewTaskStatus(userId, request.knowledgeId(), ReviewTaskStatus.COMPLETED);
        return updateResult;
    }

    private ReviewTaskBatchResponse toResponse(ReviewTaskBatchDTO batch) {
        return ReviewTaskBatchResponse.builder()
                .batchId(batch.getBatchId())
                .userId(batch.getUserId())
                .triggerSource(batch.getTriggerSource())
                .requestedLimit(batch.getRequestedLimit())
                .dispatchedCount(batch.getDispatchedCount())
                .createdAt(batch.getCreatedAt())
                .tasks(batch.getTasks() == null ? List.of() : batch.getTasks())
                .build();
    }
}
