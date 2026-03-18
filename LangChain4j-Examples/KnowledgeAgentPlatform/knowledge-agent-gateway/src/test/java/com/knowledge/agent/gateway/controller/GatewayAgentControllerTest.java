package com.knowledge.agent.gateway.controller;

import com.knowledge.agent.api.service.AgentService;
import com.knowledge.agent.common.resp.Result;
import com.knowledge.agent.gateway.auth.GatewayUserContext;
import com.knowledge.agent.gateway.model.GatewayChatRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GatewayAgentControllerTest {

    @AfterEach
    void tearDown() {
        GatewayUserContext.clear();
    }

    @Test
    void shouldDelegateChatToAgentServiceWithAuthenticatedUser() {
        AgentService agentService = mock(AgentService.class);
        when(agentService.chat(eq(1L), eq("hello"))).thenReturn(Result.success("ok"));

        GatewayAgentController controller = new GatewayAgentController();
        ReflectionTestUtils.setField(controller, "agentService", agentService);
        GatewayUserContext.setUserId(1L);

        Result<String> result = controller.chat(new GatewayChatRequest("hello", "session-1"));
        assertEquals(200, result.getCode());
        assertEquals("ok", result.getData());
    }

    @Test
    void shouldReturnSseEmitterForAuthenticatedUser() throws Exception {
        AgentService agentService = mock(AgentService.class);
        when(agentService.chat(eq(1L), eq("stream"))).thenReturn(Result.success("payload"));

        GatewayAgentController controller = new GatewayAgentController();
        ReflectionTestUtils.setField(controller, "agentService", agentService);
        GatewayUserContext.setUserId(1L);

        assertNotNull(controller.stream("stream"));
    }
}
