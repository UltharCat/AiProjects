package com.knowledge.agent.api.service;

import com.knowledge.agent.api.dto.ChatRequest;
import com.knowledge.agent.common.resp.Result;

public interface AgentService {

    /**
     * 定义聊天接口
     * @param request
     * @return
     */
    Result<String> chat(ChatRequest request);
}
