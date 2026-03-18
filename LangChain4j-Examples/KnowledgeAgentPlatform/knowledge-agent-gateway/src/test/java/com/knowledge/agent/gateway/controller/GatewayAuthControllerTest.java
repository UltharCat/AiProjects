package com.knowledge.agent.gateway.controller;

import com.knowledge.agent.api.dto.ReviewTaskDTO;
import com.knowledge.agent.api.dto.ReviewTriggerSource;
import com.knowledge.agent.api.request.UserLoginRequest;
import com.knowledge.agent.api.service.UserService;
import com.knowledge.agent.common.auth.JwtTokenUtils;
import com.knowledge.agent.common.resp.Result;
import com.knowledge.agent.gateway.model.GatewayLoginResponse;
import com.knowledge.agent.gateway.model.ReviewTaskBatchResponse;
import com.knowledge.agent.gateway.review.ReviewTaskDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GatewayAuthControllerTest {

    @Test
    void shouldReturnGatewayLoginPayloadWithPendingReviews() {
        UserService userService = mock(UserService.class);
        ReviewTaskDispatcher dispatcher = mock(ReviewTaskDispatcher.class);
        String token = JwtTokenUtils.generateToken(1L, "alice", "visual", "knowledge-agent-platform", "test-secret", Duration.ofHours(12));
        when(userService.login(eq(new UserLoginRequest("alice", "password")))).thenReturn(Result.success(token));
        when(dispatcher.dispatch(1L, 5, ReviewTriggerSource.LOGIN)).thenReturn(ReviewTaskBatchResponse.builder()
                .userId(1L)
                .triggerSource(ReviewTriggerSource.LOGIN)
                .requestedLimit(5)
                .dispatchedCount(1)
                .tasks(List.of(ReviewTaskDTO.builder()
                        .taskId("task-1")
                        .userId(1L)
                        .knowledgeId(99L)
                        .summary("SM-2 overview")
                        .dueAt(LocalDateTime.now())
                        .triggerSource(ReviewTriggerSource.LOGIN)
                        .status("PENDING")
                        .dedupKey("LOGIN:1:99")
                        .build()))
                .build());

        GatewayAuthController controller = new GatewayAuthController(dispatcher);
        ReflectionTestUtils.setField(controller, "userService", userService);
        ReflectionTestUtils.setField(controller, "tokenIssuer", "knowledge-agent-platform");
        ReflectionTestUtils.setField(controller, "tokenSecret", "test-secret");

        Result<GatewayLoginResponse> result = controller.login(new UserLoginRequest("alice", "password"));
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("Bearer", result.getData().tokenType());
        assertEquals(1, result.getData().pendingReviewCount());
        assertEquals(99L, result.getData().pendingReviewTasks().getFirst().getKnowledgeId());
    }
}
