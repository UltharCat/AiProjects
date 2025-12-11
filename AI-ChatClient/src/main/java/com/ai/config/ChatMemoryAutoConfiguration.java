package com.ai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ai.config.memory.RedisChatMemory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@EnableConfigurationProperties(ChatMemoryProperties.class)
public class ChatMemoryAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "x-project.app.chat.memory", name = "type", havingValue = "redis")
    public ChatMemory redisChatMemory(StringRedisTemplate stringRedisTemplate,
                                           ObjectMapper objectMapper,
                                           ChatMemoryProperties props) {
        return new RedisChatMemory(stringRedisTemplate, objectMapper, props.getTtl(), props.getNamespace(), props.getMaxMessages());
    }

    @Bean
    @ConditionalOnMissingBean(ChatMemory.class)
    public ChatMemory inMemoryChatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository()).build();
    }

}
