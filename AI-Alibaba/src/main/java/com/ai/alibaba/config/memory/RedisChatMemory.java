package com.ai.alibaba.config.memory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 精简版 Redis ChatMemory：
 * - Redis List 存储消息
 * - Lua 原子追加 + 可选裁剪 + 续 TTL
 * - 无本地二级缓存（减少依赖 & 复杂度）
 */
public record RedisChatMemory(StringRedisTemplate redis, ObjectMapper mapper, Duration ttl, String namespace,
                              int maxMessages) implements ChatMemory {

    // KEYS[1]=list
    // ARGV[1..n-2]=messages(json)  ARGV[n-1]=maxMessages  ARGV[n]=ttlMillis
    private static final String APPEND_SCRIPT = """
            local key = KEYS[1]
            local max = tonumber(ARGV[#ARGV-1])
            local expire = tonumber(ARGV[#ARGV])
            for i=1,#ARGV-2 do
               redis.call('RPUSH', key, ARGV[i])
            end
            if max and max > 0 then
               local len = redis.call('LLEN', key)
               if len > max then
                  local start = len - max
                  redis.call('LTRIM', key, start, -1)
               end
            end
            if expire > 0 then
               redis.call('PEXPIRE', key, expire)
            end
            return 1
            """;

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (CollectionUtils.isEmpty(messages)) return;
        String key = buildKey(conversationId);

        List<String> argv = new ArrayList<>(messages.size() + 2);
        for (Message m : messages) {
            try {
                argv.add(mapper.writeValueAsString(m));
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Message 序列化失败", e);
            }
        }
        argv.add(String.valueOf(maxMessages));
        argv.add(String.valueOf(ttl.toMillis()));

        redis.execute(connection -> {
            byte[] script = APPEND_SCRIPT.getBytes(StandardCharsets.UTF_8);
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            List<byte[]> args = new ArrayList<>();
            args.add(keyBytes);
            args.addAll(argv.stream().map(s -> s.getBytes(StandardCharsets.UTF_8)).toList());
            return connection.scriptingCommands().eval(
                    script,
                    ReturnType.INTEGER,
                    1,
                    args.toArray(new byte[0][])
            );
        }, true);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        String key = buildKey(conversationId);
        long start;
        long end = -1;
        if (lastN <= 0) {
            start = 0; // 全量
        } else {
            start = -lastN;
        }
        List<String> raw = redis.opsForList().range(key, start, end);
        if (raw == null || raw.isEmpty()) return Collections.emptyList();

        List<Message> result = new ArrayList<>(raw.size());
        for (String js : raw) {
            try {
                result.add(mapper.readValue(js, Message.class));
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    @Override
    public void clear(String conversationId) {
        redis.delete(buildKey(conversationId));
    }

    private String buildKey(String conversationId) {
        return namespace + ":" + conversationId;
    }
}
