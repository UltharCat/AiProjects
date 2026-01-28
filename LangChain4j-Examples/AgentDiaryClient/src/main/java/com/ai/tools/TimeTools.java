package com.ai.tools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TimeTools {

    @Tool(name = "get_current_time", value = "获取当前的系统时间，格式为YYYY-MM-DDTHH:MM:SS")
    public String getCurrentTime() {
        return LocalDateTime.now().toString();
    }

}
