package com.ai.alibaba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.ai.alibaba.config")
public class AlibabaModelApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlibabaModelApplication.class, args);
    }

}
