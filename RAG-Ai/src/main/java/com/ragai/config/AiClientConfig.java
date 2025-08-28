package com.ragai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * ai-client配置类
 */
@Configuration
public class AiClientConfig {

    @Value("${client.type:ollama}")
    private String CLIENT_TYPE;

    /**
     * 通过依赖中的自动装配类加载ChatModel，并最后注入到SpringIOC容器中，可以通过Map<String, ChatModel>进行注入
     * spring-ai-openai-spring-boot-starter 的 org/springframework/ai/autoconfigure/openai/OpenAiAutoConfiguration
     * com.alibaba.cloud.ai 的 com/alibaba/cloud/ai/autoconfigure/dashscope/DashScopeAutoConfiguration
     * spring-ai-ollama-spring-boot-starter 的 org/springframework/ai/autoconfigure/ollama/OllamaAutoConfiguration
     * 以上三个starter都自动装配了各自的ChatModel
     * @param allModels
     * @return
     */
    @Bean("chatClient")
    public ChatClient chatClient(Map<String, ChatModel> allModels) {
        String type = CLIENT_TYPE.trim();

        // 按命名约定查找：typeChatModel
        String targetBeanName = type + "ChatModel";
        ChatModel model = allModels.get(targetBeanName);
        if (model == null) {
            // fallback 到第一个可用的
            model = allModels.values().iterator().next();
        }

        return ChatClient
                .builder(model)
                .defaultSystem("你是一个程序员，使用{language}回答问题")
                .build();
    }

}
