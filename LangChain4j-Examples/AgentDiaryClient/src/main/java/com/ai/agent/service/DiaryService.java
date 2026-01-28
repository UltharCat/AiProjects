package com.ai.agent.service;

import com.ai.agent.response.AgentChatResponse;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface DiaryService {

    @SystemMessage(fromResource = "prompts/system-diary-prompt.st")
    AgentChatResponse chat(@MemoryId String memoryId, @UserMessage String content);

}
