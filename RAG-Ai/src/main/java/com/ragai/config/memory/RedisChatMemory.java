package com.ragai.config.memory;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.List;

public record RedisChatMemory(StringRedisTemplate redisTemplate, Duration ttl, String namespace) implements ChatMemory {

    @Override
    public void add(String conversationId, List<Message> messages) {
        // 实现 Redis 存储逻辑
        String key = namespace + ":" + conversationId;
        // 这里需要根据您的需求实现序列化和存储逻辑
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        // 实现 Redis 获取逻辑
        String key = namespace + ":" + conversationId;
        // 这里需要根据您的需求实现反序列化和获取逻辑
        return List.of();
    }

    @Override
    public void clear(String conversationId) {
        String key = namespace + ":" + conversationId;
        redisTemplate.delete(key);
    }

}
