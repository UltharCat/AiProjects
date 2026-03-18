package com.knowledge.agent.gateway.auth;

import cn.hutool.core.util.StrUtil;
import com.knowledge.agent.common.auth.AuthTokenClaims;
import com.knowledge.agent.common.auth.JwtTokenUtils;
import com.knowledge.agent.common.exception.BizException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Validates bearer tokens for all authenticated Gateway endpoints.
 */
@Component
public class GatewayAuthInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Value("${knowledge-agent.auth.token-issuer:knowledge-agent-platform}")
    private String tokenIssuer;

    @Value("${knowledge-agent.auth.token-secret:knowledge-agent-dev-secret}")
    private String tokenSecret;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (StrUtil.isBlank(authorization) || !StrUtil.startWithIgnoreCase(authorization, BEARER_PREFIX)) {
            throw new BizException(401, "Missing bearer token");
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        try {
            AuthTokenClaims claims = JwtTokenUtils.parseAndValidate(token, tokenIssuer, tokenSecret);
            GatewayUserContext.setUserId(claims.userId());
            return true;
        } catch (IllegalArgumentException ex) {
            throw new BizException(401, ex.getMessage());
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        GatewayUserContext.clear();
    }
}
