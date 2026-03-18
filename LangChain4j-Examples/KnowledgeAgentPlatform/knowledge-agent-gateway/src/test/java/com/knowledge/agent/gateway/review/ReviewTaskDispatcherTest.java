package com.knowledge.agent.gateway.review;

import com.knowledge.agent.api.dto.KnowledgeDTO;
import com.knowledge.agent.api.dto.ReviewTaskBatchDTO;
import com.knowledge.agent.api.dto.ReviewTriggerSource;
import com.knowledge.agent.api.service.RagService;
import com.knowledge.agent.common.resp.Result;
import com.knowledge.agent.gateway.messaging.ReviewBatchEventPublisher;
import com.knowledge.agent.gateway.model.ReviewTaskBatchResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviewTaskDispatcherTest {

    @Test
    void shouldPersistBatchAfterDispatch() {
        RagService ragService = mock(RagService.class);
        ReviewTaskDeduplicator deduplicator = mock(ReviewTaskDeduplicator.class);
        ReviewTaskBatchStore batchStore = mock(ReviewTaskBatchStore.class);
        ReviewBatchEventPublisher eventPublisher = mock(ReviewBatchEventPublisher.class);
        when(ragService.listPendingReviews(1L, 5)).thenReturn(Result.success(List.of(
                KnowledgeDTO.builder().id(11L).summary("Redis review").build()
        )));
        when(ragService.saveReviewTaskBatch(any(ReviewTaskBatchDTO.class))).thenReturn(Result.success(Boolean.TRUE));
        when(deduplicator.tryAcquire(ReviewTriggerSource.SCHEDULED, 1L, 11L)).thenReturn(true);

        ReviewTaskDispatcher dispatcher = new ReviewTaskDispatcher(deduplicator, batchStore, eventPublisher);
        ReflectionTestUtils.setField(dispatcher, "ragService", ragService);

        ReviewTaskBatchResponse response = dispatcher.dispatch(1L, 5, ReviewTriggerSource.SCHEDULED);

        assertEquals(1, response.dispatchedCount());
        verify(ragService).saveReviewTaskBatch(any(ReviewTaskBatchDTO.class));
        verify(batchStore).saveBatch(any(ReviewTaskBatchResponse.class));
        verify(eventPublisher).publish(any(ReviewTaskBatchResponse.class));
    }
}
