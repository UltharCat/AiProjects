package com.knowledge.agent.gateway.auth;

import com.knowledge.agent.common.exception.BizException;

/**
 * Request-scoped authenticated user context backed by a ThreadLocal.
 */
public final class GatewayUserContext {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    private GatewayUserContext() {
    }

    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    public static Long requireUserId() {
        Long userId = getUserId();
        if (userId == null) {
            throw new BizException(401, "Authentication is required");
        }
        return userId;
    }

    public static void clear() {
        USER_ID_HOLDER.remove();
    }
}
