package com.knowledge.agent.gateway.controller;

import com.knowledge.agent.api.dto.KnowledgeDTO;
import com.knowledge.agent.api.dto.ReviewTriggerSource;
import com.knowledge.agent.api.service.RagService;
import com.knowledge.agent.common.resp.Result;
import com.knowledge.agent.gateway.auth.GatewayUserContext;
import com.knowledge.agent.gateway.model.ReviewStatusUpdateRequest;
import com.knowledge.agent.gateway.model.ReviewTaskBatchResponse;
import com.knowledge.agent.gateway.review.ReviewTaskBatchStore;
import com.knowledge.agent.gateway.review.ReviewTaskDispatcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GatewayReviewControllerTest {

    @AfterEach
    void tearDown() {
        GatewayUserContext.clear();
    }

    @Test
    void shouldReturnManualReviewTaskBatchForAuthenticatedUser() {
        ReviewTaskDispatcher dispatcher = mock(ReviewTaskDispatcher.class);
        ReviewTaskBatchStore batchStore = mock(ReviewTaskBatchStore.class);
        when(dispatcher.dispatch(1L, 5, ReviewTriggerSource.MANUAL)).thenReturn(ReviewTaskBatchResponse.builder()
                .userId(1L)
                .triggerSource(ReviewTriggerSource.MANUAL)
                .requestedLimit(5)
                .dispatchedCount(0)
                .tasks(java.util.List.of())
                .build());

        GatewayReviewController controller = new GatewayReviewController(dispatcher, batchStore);
        GatewayUserContext.setUserId(1L);

        Result<ReviewTaskBatchResponse> result = controller.pendingReviews(5);
        assertEquals(200, result.getCode());
        assertEquals(ReviewTriggerSource.MANUAL, result.getData().triggerSource());
    }

    @Test
    void shouldDelegateReviewStatusUpdate() {
        RagService ragService = mock(RagService.class);
        ReviewTaskBatchStore batchStore = mock(ReviewTaskBatchStore.class);
        when(ragService.listPendingReviews(eq(1L), eq(200))).thenReturn(Result.success(java.util.List.of(
                KnowledgeDTO.builder().id(10L).summary("SM-2 overview").build()
        )));
        when(ragService.updateReviewStatus(eq(10L), eq(4))).thenReturn(Result.success(null));

        GatewayReviewController controller = new GatewayReviewController(mock(ReviewTaskDispatcher.class), batchStore);
        ReflectionTestUtils.setField(controller, "ragService", ragService);
        GatewayUserContext.setUserId(1L);

        Result<Void> result = controller.updateReviewStatus(new ReviewStatusUpdateRequest(10L, 4));
        assertEquals(200, result.getCode());
    }

    @Test
    void shouldReturnLatestScheduledBatch() {
        ReviewTaskBatchStore batchStore = mock(ReviewTaskBatchStore.class);
        when(batchStore.findLatest(1L, ReviewTriggerSource.SCHEDULED)).thenReturn(Optional.of(ReviewTaskBatchResponse.builder()
                .userId(1L)
                .triggerSource(ReviewTriggerSource.SCHEDULED)
                .requestedLimit(5)
                .dispatchedCount(1)
                .tasks(java.util.List.of())
                .build()));

        GatewayReviewController controller = new GatewayReviewController(mock(ReviewTaskDispatcher.class), batchStore);
        GatewayUserContext.setUserId(1L);

        Result<ReviewTaskBatchResponse> result = controller.latestScheduledBatch();
        assertEquals(200, result.getCode());
        assertEquals(ReviewTriggerSource.SCHEDULED, result.getData().triggerSource());
    }
}
