package com.knowledge.agent.core;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDubbo
@SpringBootApplication
public class KnowledgeAgentCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeAgentCoreApplication.class, args);
    }

}
