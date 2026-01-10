package com.ai.service;

import com.ai.request.AgentChatRequest;
import reactor.core.publisher.Flux;

public interface AgentService {

    /**
     * 智能客服对话接口
     * @param request
     * @return
     */
    Flux<String> chat(AgentChatRequest request);

}
