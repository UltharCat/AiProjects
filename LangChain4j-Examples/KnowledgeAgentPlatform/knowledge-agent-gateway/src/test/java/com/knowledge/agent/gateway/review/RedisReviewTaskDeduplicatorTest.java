package com.knowledge.agent.gateway.review;

import com.knowledge.agent.api.dto.ReviewTriggerSource;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedisReviewTaskDeduplicatorTest {

    @Test
    void shouldFallbackToInMemoryDedupWhenRedisIsUnavailable() {
        RedisReviewTaskDeduplicator deduplicator = new RedisReviewTaskDeduplicator();
        ReflectionTestUtils.setField(deduplicator, "dedupWindow", Duration.ofMinutes(30));
        ReflectionTestUtils.setField(deduplicator, "redisKeyPrefix", "knowledge-agent:review");

        assertTrue(deduplicator.tryAcquire(ReviewTriggerSource.SCHEDULED, 1L, 9L));
        assertFalse(deduplicator.tryAcquire(ReviewTriggerSource.SCHEDULED, 1L, 9L));
        assertTrue(deduplicator.tryAcquire(ReviewTriggerSource.MANUAL, 1L, 9L));
    }
}
