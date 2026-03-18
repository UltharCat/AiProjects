package com.knowledge.agent.core.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledge.agent.core.model.ConversationState;
import com.knowledge.agent.core.model.StateContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Redis-first conversation store with transparent in-memory fallback.
 */
@Slf4j
@Primary
@Component
public class RedisConversationStore implements ConversationStore {

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    private final InMemoryConversationStore fallbackStore;
    private final ObjectMapper objectMapper;

    public RedisConversationStore(InMemoryConversationStore fallbackStore) {
        this.fallbackStore = fallbackStore;
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Override
    public StateContext getOrCreate(Long userId) {
        if (stringRedisTemplate == null) {
            return fallbackStore.getOrCreate(userId);
        }
        try {
            String value = stringRedisTemplate.opsForValue().get(buildKey(userId));
            if (value == null) {
                return fallbackStore.getOrCreate(userId);
            }
            StateContext context = objectMapper.readValue(value, StateContext.class);
            if (context.getTurns() == null) {
                context.setTurns(new java.util.ArrayList<>());
            }
            return context;
        } catch (Exception ex) {
            log.warn("Redis conversation read failed, falling back to in-memory store. userId={}", userId, ex);
            return fallbackStore.getOrCreate(userId);
        }
    }

    @Override
    public void save(StateContext context) {
        context.setUpdatedAt(LocalDateTime.now());
        fallbackStore.save(context);
        if (stringRedisTemplate == null) {
            return;
        }
        try {
            stringRedisTemplate.opsForValue().set(buildKey(context.getUserId()), objectMapper.writeValueAsString(context), Duration.ofHours(12));
        } catch (Exception ex) {
            log.warn("Redis conversation write failed, keeping in-memory fallback only. userId={}", context.getUserId(), ex);
        }
    }

    private String buildKey(Long userId) {
        return "knowledge-agent:conversation:" + userId;
    }
}
