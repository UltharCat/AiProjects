package com.knowledge.agent.api.dto;

import java.io.Serializable;


public record ChatRequest(
        Long userId, // 用户id
        String prompt, // 用户输入的问题
        String sessionId // 会话id
) implements Serializable {

}
