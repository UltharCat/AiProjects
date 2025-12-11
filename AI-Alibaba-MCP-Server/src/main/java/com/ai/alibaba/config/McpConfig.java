package com.ai.alibaba.config;

import com.ai.alibaba.tools.TimeTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider timeTools(TimeTool timeTool) {
        return MethodToolCallbackProvider.builder().toolObjects(timeTool).build();
    }

}
