package com.ragai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.ragai.config")
public class RagAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagAiApplication.class, args);
    }

}
