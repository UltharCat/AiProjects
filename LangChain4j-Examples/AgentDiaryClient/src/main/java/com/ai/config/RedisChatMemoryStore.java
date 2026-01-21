package com.ai.config;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.List;

import static dev.langchain4j.data.message.ChatMessageDeserializer.messagesFromJson;
import static dev.langchain4j.data.message.ChatMessageSerializer.messagesToJson;

public class RedisChatMemoryStore implements ChatMemoryStore {

    public static final String REDIS_NAMESPACE = "chat_memory:";

    private final StringRedisTemplate redisTemplate;
    private final Duration baseTtl;

    public RedisChatMemoryStore(StringRedisTemplate redisTemplate) {
        this(redisTemplate, Duration.ofDays(30)); // 默认TTL为30天
    }

    public RedisChatMemoryStore(StringRedisTemplate redisTemplate, Duration baseTtl) {
        this.redisTemplate = redisTemplate;
        this.baseTtl = baseTtl;
    }

    /**
     * 创建Redis键
     * @param memoryId
     * @return
     */
    private String createKey(Object memoryId) {
        return REDIS_NAMESPACE + memoryId.toString();
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String json = redisTemplate.opsForValue().get(this.createKey(memoryId));
        return json == null ? List.of() : messagesFromJson(json);
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String json = messagesToJson(messages);
        // 为了防止大量key在同一时间过期，增加一个随机的TTL偏移量
        Duration ttl = baseTtl.plusSeconds(RandomUtils.insecure().randomLong(0, 3600));
        redisTemplate.opsForValue().set(this.createKey(memoryId), json, ttl);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        redisTemplate.delete(this.createKey(memoryId));
    }

}
