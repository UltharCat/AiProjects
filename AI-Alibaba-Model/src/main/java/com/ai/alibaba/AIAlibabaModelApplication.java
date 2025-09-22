package com.ai.alibaba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.ai.alibaba.config")
public class AIAlibabaModelApplication {

    public static void main(String[] args) {
        SpringApplication.run(AIAlibabaModelApplication.class, args);
    }

}
