package com.ai.config;

import com.ai.tools.OrderTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider timeTools(OrderTools orderTools) {
        return MethodToolCallbackProvider.builder().toolObjects(orderTools).build();
    }

}
