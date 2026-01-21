package com.ai.config;

import com.ai.agent.service.AgentService;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class AgentServiceFactory {

    @Bean
    public AgentService chatAgentService(QwenChatModel qwenChatModel,
                                         StringRedisTemplate redisTemplate) {

        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                // Keep the last 100 messages in memory
                .maxMessages(100)
                .chatMemoryStore(new RedisChatMemoryStore(redisTemplate))
                .build();

        return AiServices.builder(AgentService.class)
                .chatModel(qwenChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .build();
    }

}
