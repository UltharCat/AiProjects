package com.ragai.config;

import com.ragai.config.memory.RedisChatMemory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
    @ConditionalOnProperty(prefix = "app.chat.memory", name = "type", havingValue = "redis", matchIfMissing = false)
    @ConditionalOnBean(StringRedisTemplate.class)
    public ChatMemory redisChatMemory(StringRedisTemplate stringRedisTemplate,
                                           ChatMemoryProperties props) {
        return new RedisChatMemory(stringRedisTemplate, props.getTtl(), props.getNamespace());
    }

    @Bean
    @ConditionalOnMissingBean(ChatMemory.class)
    public ChatMemory inMemoryChatMemory() {
        return new InMemoryChatMemory();
    }

}
