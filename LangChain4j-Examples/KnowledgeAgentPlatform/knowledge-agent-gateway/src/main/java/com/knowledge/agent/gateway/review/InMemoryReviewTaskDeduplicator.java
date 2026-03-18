package com.knowledge.agent.gateway.review;

import com.knowledge.agent.api.dto.ReviewTriggerSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory deduplication used as a local stand-in for a future Redis-backed implementation.
 */
@Component
public class InMemoryReviewTaskDeduplicator implements ReviewTaskDeduplicator {

    @Value("${knowledge-agent.review.dedup-window:PT30M}")
    private Duration dedupWindow;

    private final Map<String, Instant> dedupStore = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquire(ReviewTriggerSource triggerSource, Long userId, Long knowledgeId) {
        if (triggerSource == ReviewTriggerSource.MANUAL) {
            return true;
        }

        cleanupExpiredKeys();
        String dedupKey = buildDedupKey(triggerSource, userId, knowledgeId);
        Instant now = Instant.now();
        final boolean[] acquired = {false};
        dedupStore.compute(dedupKey, (key, existingExpiry) -> {
            if (existingExpiry != null && existingExpiry.isAfter(now)) {
                return existingExpiry;
            }
            acquired[0] = true;
            return now.plus(resolveWindow());
        });
        return acquired[0];
    }

    String buildDedupKey(ReviewTriggerSource triggerSource, Long userId, Long knowledgeId) {
        return triggerSource + ":" + userId + ":" + knowledgeId;
    }

    private Duration resolveWindow() {
        return dedupWindow == null || dedupWindow.isNegative() || dedupWindow.isZero() ? Duration.ofMinutes(30) : dedupWindow;
    }

    private void cleanupExpiredKeys() {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, Instant>> iterator = dedupStore.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Instant> entry = iterator.next();
            if (entry.getValue() == null || !entry.getValue().isAfter(now)) {
                iterator.remove();
            }
        }
    }
}
