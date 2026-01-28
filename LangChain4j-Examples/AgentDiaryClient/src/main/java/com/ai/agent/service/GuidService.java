package com.ai.agent.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface GuidService {

    @SystemMessage("""
            你是一个智能助手，负责判断用户的消息内容是否属于问候语，如果是问候语，就返回true，否则返回false。
            """)
    boolean isGreeting(@UserMessage String content);

}
