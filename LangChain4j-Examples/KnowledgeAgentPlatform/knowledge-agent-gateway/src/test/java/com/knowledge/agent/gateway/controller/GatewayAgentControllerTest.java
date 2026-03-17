package com.knowledge.agent.gateway.controller;

import com.knowledge.agent.api.request.ChatRequest;
import com.knowledge.agent.api.service.AgentService;
import com.knowledge.agent.common.resp.Result;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GatewayAgentControllerTest {

    @Test
    void shouldDelegateChatToAgentService() {
        AgentService agentService = mock(AgentService.class);
        when(agentService.chat(eq(1L), eq("hello"))).thenReturn(Result.success("ok"));

        GatewayAgentController controller = new GatewayAgentController();
        ReflectionTestUtils.setField(controller, "agentService", agentService);

        Result<String> result = controller.chat(new ChatRequest(1L, "hello", "session-1"));
        assertEquals(200, result.getCode());
        assertEquals("ok", result.getData());
    }

    @Test
    void shouldReturnSseEmitter() throws Exception {
        AgentService agentService = mock(AgentService.class);
        when(agentService.chat(eq(1L), eq("stream"))).thenReturn(Result.success("payload"));

        GatewayAgentController controller = new GatewayAgentController();
        ReflectionTestUtils.setField(controller, "agentService", agentService);

        assertNotNull(controller.stream(1L, "stream"));
    }
}
