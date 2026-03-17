package com.knowledge.agent.core.service;

import com.knowledge.agent.api.dto.KnowledgeDTO;
import com.knowledge.agent.core.prompt.AgentPromptService;
import com.knowledge.agent.core.store.InMemoryConversationStore;
import com.knowledge.agent.core.tool.AgentToolRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentServiceImplTest {

    private AgentToolRouter toolRouter;

    private AgentServiceImpl agentService;

    @BeforeEach
    void setUp() {
        toolRouter = Mockito.mock(AgentToolRouter.class);
        agentService = new AgentServiceImpl(new InMemoryConversationStore(), new AgentPromptService(), toolRouter);
        when(toolRouter.getUserProfile(anyLong())).thenReturn("SOCRATIC");
        when(toolRouter.searchKnowledge(anyLong(), anyString(), anyInt())).thenReturn(List.of(
                KnowledgeDTO.builder().id(1L).summary("Virtual threads are useful for high-concurrency IO workloads").build()
        ));
        when(toolRouter.listPendingReviews(anyLong(), anyInt())).thenReturn(List.of(
                KnowledgeDTO.builder().id(2L).summary("SM-2 updates the next review date from quality feedback").build()
        ));
        when(toolRouter.saveKnowledge(anyLong(), anyString(), anySet())).thenReturn(true);
    }

    @Test
    void shouldBuildTeachingResponse() {
        String response = agentService.chat(1L, "how do virtual threads work").getData();
        assertNotNull(response);
        assertTrue(response.contains("TEACHING"));
        verify(toolRouter).searchKnowledge(anyLong(), anyString(), anyInt());
    }

    @Test
    void shouldArchiveSummary() {
        agentService.chat(1L, "how does SM-2 work");
        String response = agentService.chat(1L, "summary").getData();
        assertNotNull(response);
        verify(toolRouter).saveKnowledge(anyLong(), anyString(), anySet());
    }

    @Test
    void shouldHandleReviewAndUpdateQuality() {
        String review = agentService.chat(1L, "review").getData();
        assertNotNull(review);
        String result = agentService.chat(1L, "4").getData();
        assertNotNull(result);
        verify(toolRouter).updateReviewStatus(2L, 4);
    }
}
