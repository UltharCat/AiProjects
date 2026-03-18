package com.knowledge.agent.gateway.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledge.agent.api.dto.ReviewTriggerSource;
import com.knowledge.agent.gateway.model.ReviewTaskBatchResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedisReviewTaskBatchStoreTest {

    @Test
    void shouldFallbackToInMemoryStoreWhenRedisIsUnavailable() {
        RedisReviewTaskBatchStore batchStore = new RedisReviewTaskBatchStore(new ObjectMapper());
        ReflectionTestUtils.setField(batchStore, "batchTtl", Duration.ofHours(24));
        ReflectionTestUtils.setField(batchStore, "redisKeyPrefix", "knowledge-agent:review");

        ReviewTaskBatchResponse batch = ReviewTaskBatchResponse.builder()
                .userId(1L)
                .triggerSource(ReviewTriggerSource.SCHEDULED)
                .requestedLimit(5)
                .dispatchedCount(1)
                .tasks(List.of())
                .build();
        batchStore.saveBatch(batch);

        assertTrue(batchStore.findLatest(1L, ReviewTriggerSource.SCHEDULED).isPresent());
        assertEquals(1, batchStore.findLatest(1L, ReviewTriggerSource.SCHEDULED).orElseThrow().dispatchedCount());
    }
}
