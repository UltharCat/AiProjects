package com.ragai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ai-client配置类
 */
@Configuration
public class AiClientConfig {

    // 绑定到 OpenAI：依赖 openAiChatModel（openAiChatModel这个类由 spring-ai-openai-spring-boot-starter 的org/springframework/ai/autoconfigure/openai/OpenAiAutoConfiguration自动装配）
    @Bean("openAiChatClient")
    public ChatClient openAiChatClient(@Qualifier("openAiChatModel") ChatModel model) {
        return ChatClient.builder(model).build();
    }

    // 绑定到 Ollama：依赖 ollamaChatModel（ollamaChatModel这个类由 spring-ai-ollama-spring-boot-starter 的org/springframework/ai/autoconfigure/ollama/OllamaAutoConfiguration自动装配）
    @Bean("ollamaChatClient")
    public ChatClient ollamaChatClient(@Qualifier("ollamaChatModel") ChatModel model) {
        return ChatClient.builder(model).build();
    }

}
