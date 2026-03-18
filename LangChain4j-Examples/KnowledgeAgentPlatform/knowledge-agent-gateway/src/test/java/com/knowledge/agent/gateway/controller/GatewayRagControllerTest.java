package com.knowledge.agent.gateway.controller;

import com.knowledge.agent.api.dto.KnowledgeDTO;
import com.knowledge.agent.api.service.RagService;
import com.knowledge.agent.common.resp.Result;
import com.knowledge.agent.gateway.auth.GatewayUserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GatewayRagControllerTest {

    @AfterEach
    void tearDown() {
        GatewayUserContext.clear();
    }

    @Test
    void shouldDelegateSearchWithTagFiltersForAuthenticatedUser() {
        RagService ragService = mock(RagService.class);
        when(ragService.searchKnowledgeWithFilters(eq(1L), eq("sm2"), eq(3), eq(Set.of("memory"))))
                .thenReturn(Result.success(List.of(KnowledgeDTO.builder().id(1L).summary("SM-2").build())));

        GatewayRagController controller = new GatewayRagController();
        ReflectionTestUtils.setField(controller, "ragService", ragService);
        GatewayUserContext.setUserId(1L);

        Result<List<KnowledgeDTO>> result = controller.search("sm2", 3, Set.of("memory"));
        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
    }
}
