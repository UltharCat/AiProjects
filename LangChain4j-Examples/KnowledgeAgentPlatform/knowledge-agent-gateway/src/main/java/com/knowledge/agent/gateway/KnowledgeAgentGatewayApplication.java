package com.knowledge.agent.gateway;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableDubbo
@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.knowledge.agent")
public class KnowledgeAgentGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeAgentGatewayApplication.class, args);
    }

}
