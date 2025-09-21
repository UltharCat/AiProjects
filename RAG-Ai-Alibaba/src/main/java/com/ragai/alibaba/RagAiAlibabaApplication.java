package com.ragai.alibaba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.ragai.alibaba.config")
public class RagAiAlibabaApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagAiAlibabaApplication.class, args);
    }

}
