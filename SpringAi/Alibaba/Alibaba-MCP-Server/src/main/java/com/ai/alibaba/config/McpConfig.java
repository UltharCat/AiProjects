package com.ai.alibaba.config;

import com.ai.alibaba.tools.OrderTools;
import com.ai.alibaba.tools.TimeTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider timeTools(TimeTool timeTool, OrderTools orderTools) {
        return MethodToolCallbackProvider.builder().toolObjects(timeTool, orderTools).build();
    }

}
