package com.knowledge.agent.core.service;

import com.knowledge.agent.api.dto.KnowledgeDTO;
import com.knowledge.agent.api.dto.UserProfileDTO;
import com.knowledge.agent.core.llm.LangChain4jAgentRuntime;
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
    private LangChain4jAgentRuntime agentRuntime;

    private AgentServiceImpl agentService;

    @BeforeEach
    void setUp() {
        toolRouter = Mockito.mock(AgentToolRouter.class);
        agentRuntime = Mockito.mock(LangChain4jAgentRuntime.class);
        agentService = new AgentServiceImpl(new InMemoryConversationStore(), new AgentPromptService(), toolRouter, agentRuntime);
        when(toolRouter.getUserProfile(anyLong())).thenReturn("SOCRATIC");
        when(toolRouter.getUserProfileDetail(anyLong())).thenReturn(UserProfileDTO.builder()
                .userId(1L)
                .learningStyle("SOCRATIC")
                .preferencesJson("{\"learningStyle\":\"SOCRATIC\"}")
                .build());
        when(toolRouter.searchKnowledge(anyLong(), anyString(), anyInt())).thenReturn(List.of(
                KnowledgeDTO.builder().id(1L).summary("Virtual threads are useful for high-concurrency IO workloads").build()
        ));
        when(toolRouter.listPendingReviews(anyLong(), anyInt())).thenReturn(List.of(
                KnowledgeDTO.builder().id(2L).summary("SM-2 updates the next review date from quality feedback").build()
        ));
        when(toolRouter.saveKnowledge(anyLong(), anyString(), anySet())).thenReturn(true);
        when(agentRuntime.generateTeachingReply(anyString(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyList(), anyString()))
                .thenReturn(java.util.Optional.empty());
        when(agentRuntime.summarize(anyString(), anyString())).thenReturn(java.util.Optional.empty());
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

    @Test
    void shouldUseLangChain4jTeachingReplyWhenAvailable() {
        when(agentRuntime.generateTeachingReply(anyString(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyList(), anyString()))
                .thenReturn(java.util.Optional.of("LLM powered explanation"));

        String response = agentService.chat(1L, "explain virtual threads").getData();
        assertTrue(response.contains("LLM powered explanation"));
    }
}
