package com.ai.config;

import com.ai.agent.service.DiaryService;
import com.ai.agent.service.GuidService;
import com.ai.tools.TimeTools;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class AgentServiceFactory {

    private ChatMemoryProvider getChatMemoryProvider(StringRedisTemplate redisTemplate) {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                // Keep the last 100 messages in memory
                .maxMessages(100)
                .chatMemoryStore(new RedisChatMemoryStore(redisTemplate))
                .build();
    }

    @Bean
    public GuidService guidService(OllamaChatModel ollamaChatModel) {
        return AiServices.builder(GuidService.class)
                .chatModel(ollamaChatModel)
                .build();
    }

    @Bean
    public DiaryService diaryService(QwenChatModel qwenChatModel,
                                     StringRedisTemplate redisTemplate,
                                     TimeTools timeTools) {
        return AiServices.builder(DiaryService.class)
                .chatModel(qwenChatModel)
                .chatMemoryProvider(this.getChatMemoryProvider(redisTemplate))
                .tools(timeTools)
                .build();
    }

}
