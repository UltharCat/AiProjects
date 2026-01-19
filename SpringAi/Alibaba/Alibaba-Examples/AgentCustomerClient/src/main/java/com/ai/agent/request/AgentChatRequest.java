package com.ai.agent.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class AgentChatRequest {

    /**
     * 用户对话id
     */
    @NotBlank(message = "用户对话id不能为空")
    private String conversationId;

    /**
     * 用户输入内容
     */
    @NotBlank(message = "用户输入内容不能为空")
    private String content;

}
