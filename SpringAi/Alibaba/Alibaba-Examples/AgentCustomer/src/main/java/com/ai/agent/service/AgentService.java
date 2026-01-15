package com.ai.agent.service;

import com.ai.agent.request.AgentChatRequest;
import reactor.core.publisher.Flux;

public interface AgentService {

    /**
     * 智能客服对话接口
     * @param request
     * @return
     */
    Flux<String> chat(AgentChatRequest request);

}
