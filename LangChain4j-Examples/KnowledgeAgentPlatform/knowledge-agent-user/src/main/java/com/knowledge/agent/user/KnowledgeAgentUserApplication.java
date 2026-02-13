package com.knowledge.agent.user;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDubbo
@SpringBootApplication
public class KnowledgeAgentUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeAgentUserApplication.class, args);
    }

}
