package com.knowledge.agent.gateway.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledge.agent.api.dto.ReviewTriggerSource;
import com.knowledge.agent.gateway.model.ReviewTaskBatchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Saves generated review batches to Redis when available and falls back to local memory otherwise.
 */
@Slf4j
@Primary
@Component
public class RedisReviewTaskBatchStore implements ReviewTaskBatchStore {

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper;

    private final Map<String, ReviewTaskBatchResponse> fallbackStore = new ConcurrentHashMap<>();

    @Value("${knowledge-agent.review.batch-ttl:PT24H}")
    private Duration batchTtl;

    @Value("${knowledge-agent.review.redis-key-prefix:knowledge-agent:review}")
    private String redisKeyPrefix;

    public RedisReviewTaskBatchStore(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void saveBatch(ReviewTaskBatchResponse batch) {
        String key = buildBatchKey(batch.userId(), batch.triggerSource());
        if (stringRedisTemplate == null) {
            fallbackStore.put(key, batch);
            return;
        }

        try {
            // 关键步骤：调度产出的批次先落到 Redis，后续客户端或其他服务可以按用户读取最近一次结果。
            String payload = objectMapper.writeValueAsString(batch);
            stringRedisTemplate.opsForValue().set(key, payload, resolveTtl());
        } catch (Exception ex) {
            log.warn("Redis batch store unavailable, falling back to in-memory store. key={}", key, ex);
            fallbackStore.put(key, batch);
        }
    }

    @Override
    public Optional<ReviewTaskBatchResponse> findLatest(Long userId, ReviewTriggerSource triggerSource) {
        String key = buildBatchKey(userId, triggerSource);
        if (stringRedisTemplate == null) {
            return Optional.ofNullable(fallbackStore.get(key));
        }

        try {
            String payload = stringRedisTemplate.opsForValue().get(key);
            if (payload == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(payload, ReviewTaskBatchResponse.class));
        } catch (Exception ex) {
            log.warn("Redis batch read unavailable, falling back to in-memory store. key={}", key, ex);
            return Optional.ofNullable(fallbackStore.get(key));
        }
    }

    private String buildBatchKey(Long userId, ReviewTriggerSource triggerSource) {
        return "%s:batch:%s:%s".formatted(redisKeyPrefix, triggerSource, userId);
    }

    private Duration resolveTtl() {
        return batchTtl == null || batchTtl.isNegative() || batchTtl.isZero() ? Duration.ofHours(24) : batchTtl;
    }
}
