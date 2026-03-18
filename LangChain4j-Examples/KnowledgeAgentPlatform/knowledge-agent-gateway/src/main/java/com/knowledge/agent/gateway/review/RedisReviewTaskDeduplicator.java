package com.knowledge.agent.gateway.review;

import com.knowledge.agent.api.dto.ReviewTriggerSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Prefer Redis for cross-instance deduplication and fall back to in-memory behavior when Redis is unavailable.
 */
@Slf4j
@Primary
@Component
public class RedisReviewTaskDeduplicator implements ReviewTaskDeduplicator {

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Value("${knowledge-agent.review.dedup-window:PT30M}")
    private Duration dedupWindow;

    @Value("${knowledge-agent.review.redis-key-prefix:knowledge-agent:review:dedup}")
    private String redisKeyPrefix;

    private volatile InMemoryReviewTaskDeduplicator fallbackDeduplicator;

    @Override
    public boolean tryAcquire(ReviewTriggerSource triggerSource, Long userId, Long knowledgeId) {
        Duration window = resolveWindow();
        if (triggerSource == ReviewTriggerSource.MANUAL) {
            return true;
        }

        InMemoryReviewTaskDeduplicator fallback = fallbackDeduplicator(window);
        if (stringRedisTemplate == null) {
            return fallback.tryAcquire(triggerSource, userId, knowledgeId);
        }

        String key = buildDedupKey(triggerSource, userId, knowledgeId);
        try {
            // 关键步骤：用 Redis 的 setIfAbsent + TTL 做跨实例去重，避免多节点重复派发。
            Boolean acquired = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", window);
            return Boolean.TRUE.equals(acquired);
        } catch (Exception ex) {
            log.warn("Redis dedup unavailable, falling back to in-memory dedup. key={}", key, ex);
            return fallback.tryAcquire(triggerSource, userId, knowledgeId);
        }
    }

    private String buildDedupKey(ReviewTriggerSource triggerSource, Long userId, Long knowledgeId) {
        return "%s:%s:%s:%s".formatted(redisKeyPrefix, triggerSource, userId, knowledgeId);
    }

    private Duration resolveWindow() {
        return dedupWindow == null || dedupWindow.isNegative() || dedupWindow.isZero() ? Duration.ofMinutes(30) : dedupWindow;
    }

    private InMemoryReviewTaskDeduplicator fallbackDeduplicator(Duration window) {
        if (fallbackDeduplicator == null) {
            synchronized (this) {
                if (fallbackDeduplicator == null) {
                    fallbackDeduplicator = new InMemoryReviewTaskDeduplicator(window);
                }
            }
        }
        return fallbackDeduplicator;
    }
}
