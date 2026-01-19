package com.ai.agent.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;


public record AgentChatRequest(
        @NotBlank(message = "用户对话id不能为空") String conversationId, // 用户对话id
        @NotBlank(message = "用户输入内容不能为空") String content, // 用户输入内容
        List<String> imgUrls // 图片URL列表
) {

}
