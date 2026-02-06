package com.knowledge.agent.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChatRequest implements Serializable {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户输入的问题
     */
    private String prompt;

    /**
     * 会话id
     */
    private String sessionId;

}
