package com.knowledge.agent.api.request;

import java.io.Serializable;

/**
 * 对话请求对象。
 */
public record ChatRequest(
        /**
         * 发起请求的用户 ID。
         */
        Long userId,
        /**
         * 用户输入的问题或指令。
         */
        String prompt,
        /**
         * 会话 ID，用于关联多轮对话。
         */
        String sessionId
) implements Serializable {

}
