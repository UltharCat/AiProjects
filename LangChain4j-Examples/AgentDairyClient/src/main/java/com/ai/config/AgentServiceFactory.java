package com.ai.config;

import com.ai.agent.service.AgentService;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentServiceFactory {

    @Bean
    public AgentService agentService(QwenChatModel qwenChatModel) {
        return AiServices.create(AgentService.class, qwenChatModel);
    }

}
