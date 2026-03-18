package com.knowledge.agent.gateway.auth;

import com.knowledge.agent.common.auth.JwtTokenUtils;
import com.knowledge.agent.common.exception.BizException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GatewayAuthInterceptorTest {

    @AfterEach
    void tearDown() {
        GatewayUserContext.clear();
    }

    @Test
    void shouldAcceptValidBearerToken() {
        GatewayAuthInterceptor interceptor = new GatewayAuthInterceptor();
        ReflectionTestUtils.setField(interceptor, "tokenIssuer", "knowledge-agent-platform");
        ReflectionTestUtils.setField(interceptor, "tokenSecret", "test-secret");

        String token = JwtTokenUtils.generateToken(9L, "alice", "visual", "knowledge-agent-platform", "test-secret", Duration.ofHours(2));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        boolean result = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());
        assertTrue(result);
        assertEquals(9L, GatewayUserContext.requireUserId());
    }

    @Test
    void shouldRejectMissingBearerToken() {
        GatewayAuthInterceptor interceptor = new GatewayAuthInterceptor();
        ReflectionTestUtils.setField(interceptor, "tokenIssuer", "knowledge-agent-platform");
        ReflectionTestUtils.setField(interceptor, "tokenSecret", "test-secret");

        BizException exception = assertThrows(BizException.class,
                () -> interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object()));
        assertEquals(401, exception.getCode());
    }
}
