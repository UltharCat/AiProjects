package com.knowledge.agent.api.dto;

import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * 用户登录会话对象，用于对外返回登录结果与基础会话信息。
 */
@Builder
public record UserLoginSessionDTO(
        /**
         * 登录用户 ID。
         */
        Long userId,
        /**
         * 访问令牌。
         */
        String accessToken,
        /**
         * 令牌类型，当前为 Bearer。
         */
        String tokenType,
        /**
         * 令牌过期时间。
         */
        Instant expiresAt,
        /**
         * 登录用户的结构化画像。
         */
        UserProfileDTO profile
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
