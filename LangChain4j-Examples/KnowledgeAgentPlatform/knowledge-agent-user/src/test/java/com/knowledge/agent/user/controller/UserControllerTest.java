package com.knowledge.agent.user.controller;

import com.knowledge.agent.api.dto.UserLoginSessionDTO;
import com.knowledge.agent.api.dto.UserProfileDTO;
import com.knowledge.agent.api.request.UserLoginRequest;
import com.knowledge.agent.api.service.UserService;
import com.knowledge.agent.common.auth.JwtTokenUtils;
import com.knowledge.agent.common.resp.Result;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserControllerTest {

    @Test
    void shouldBuildLoginSessionFromToken() {
        UserService userService = mock(UserService.class);
        String token = JwtTokenUtils.generateToken(1L, "alice", "SOCRATIC", "knowledge-agent-platform", "test-secret", Duration.ofHours(2));
        when(userService.login(eq(new UserLoginRequest("alice", "password")))).thenReturn(Result.success(token));
        when(userService.getUserProfileDetail(1L)).thenReturn(Result.success(UserProfileDTO.builder()
                .userId(1L)
                .username("alice")
                .learningStyle("SOCRATIC")
                .preferencesJson("{\"learningStyle\":\"SOCRATIC\"}")
                .preferences(Map.of("learningStyle", "SOCRATIC"))
                .build()));

        UserController controller = new UserController(userService);
        ReflectionTestUtils.setField(controller, "tokenIssuer", "knowledge-agent-platform");
        ReflectionTestUtils.setField(controller, "tokenSecret", "test-secret");

        Result<UserLoginSessionDTO> result = controller.login(new UserLoginRequest("alice", "password"));
        assertEquals(200, result.getCode());
        assertEquals("Bearer", result.getData().tokenType());
        assertNotNull(result.getData().profile());
    }

    @Test
    void shouldResolveCurrentProfileFromBearerToken() {
        UserService userService = mock(UserService.class);
        String token = JwtTokenUtils.generateToken(1L, "alice", "SOCRATIC", "knowledge-agent-platform", "test-secret", Duration.ofHours(2));
        when(userService.getUserProfileDetail(1L)).thenReturn(Result.success(UserProfileDTO.builder()
                .userId(1L)
                .username("alice")
                .learningStyle("SOCRATIC")
                .preferencesJson("{\"learningStyle\":\"SOCRATIC\"}")
                .preferences(Map.of("learningStyle", "SOCRATIC"))
                .build()));

        UserController controller = new UserController(userService);
        ReflectionTestUtils.setField(controller, "tokenIssuer", "knowledge-agent-platform");
        ReflectionTestUtils.setField(controller, "tokenSecret", "test-secret");

        Result<UserProfileDTO> result = controller.profile("Bearer " + token);
        assertEquals(1L, result.getData().getUserId());
        assertEquals("SOCRATIC", result.getData().getLearningStyle());
    }
}
