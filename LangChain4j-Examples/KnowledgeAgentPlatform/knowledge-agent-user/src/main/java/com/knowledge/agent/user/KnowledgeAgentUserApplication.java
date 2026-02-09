package com.knowledge.agent.user;

import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDubbo
@SpringBootApplication
@NacosConfigurationProperties(dataId = "user", autoRefreshed = true)
public class KnowledgeAgentUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeAgentUserApplication.class, args);
    }

}
