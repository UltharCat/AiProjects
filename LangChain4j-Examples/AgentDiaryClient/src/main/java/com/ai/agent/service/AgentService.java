package com.ai.agent.service;

import com.ai.agent.request.AgentChatRequest;
import com.ai.agent.response.AgentChatResponse;
import dev.langchain4j.service.SystemMessage;

public interface AgentService {

    @SystemMessage(fromResource = "prompts/system-prompt.st")
    AgentChatResponse chat(AgentChatRequest request);

}
