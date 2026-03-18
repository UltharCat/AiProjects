package com.knowledge.agent.api.request;

import java.io.Serializable;

/**
 * 用户登录请求对象。
 */
public record UserLoginRequest(
        /**
         * 用户名。
         */
        String username,
        /**
         * 登录密码。
         */
        String password
) implements Serializable {

}
