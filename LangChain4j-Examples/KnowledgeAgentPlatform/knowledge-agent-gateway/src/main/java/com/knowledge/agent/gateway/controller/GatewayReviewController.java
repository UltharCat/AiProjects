package com.knowledge.agent.gateway.controller;

import com.knowledge.agent.api.dto.KnowledgeDTO;
import com.knowledge.agent.api.dto.ReviewTriggerSource;
import com.knowledge.agent.api.service.RagService;
import com.knowledge.agent.common.exception.BizException;
import com.knowledge.agent.common.resp.Result;
import com.knowledge.agent.gateway.auth.GatewayUserContext;
import com.knowledge.agent.gateway.model.ReviewStatusUpdateRequest;
import com.knowledge.agent.gateway.model.ReviewTaskBatchResponse;
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

@RestController
@RequestMapping("/api/reviews")
public class GatewayReviewController {

    @DubboReference(check = false)
    private RagService ragService;

    private final ReviewTaskDispatcher reviewTaskDispatcher;

    public GatewayReviewController(ReviewTaskDispatcher reviewTaskDispatcher) {
        this.reviewTaskDispatcher = reviewTaskDispatcher;
    }

    @GetMapping("/pending")
    public Result<ReviewTaskBatchResponse> pendingReviews(@RequestParam(defaultValue = "5") Integer limit) {
        return Result.success(reviewTaskDispatcher.dispatch(
                GatewayUserContext.requireUserId(),
                limit,
                ReviewTriggerSource.MANUAL
        ));
    }

    @PatchMapping("/status")
    public Result<Void> updateReviewStatus(@RequestBody ReviewStatusUpdateRequest request) {
        Long userId = GatewayUserContext.requireUserId();
        Result<List<KnowledgeDTO>> pendingReviews = ragService.listPendingReviews(userId, 200);
        boolean allowed = pendingReviews != null
                && Objects.equals(pendingReviews.getCode(), 200)
                && pendingReviews.getData() != null
                && pendingReviews.getData().stream().map(KnowledgeDTO::getId).anyMatch(request.knowledgeId()::equals);
        if (!allowed) {
            throw new BizException(403, "Review item is not available for the current user");
        }
        return ragService.updateReviewStatus(request.knowledgeId(), request.quality());
    }
}
