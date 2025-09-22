package com.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.ai.config")
public class AIChatClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(AIChatClientApplication.class, args);
    }

}
