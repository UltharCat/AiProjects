package com.ai.service;

import reactor.core.publisher.Flux;

public interface AgentService {

    /**
     * 智能客服对话接口
     * @param threadId
     * @param userContent
     * @return
     */
    Flux<String> chat(String threadId, String userContent);

}
