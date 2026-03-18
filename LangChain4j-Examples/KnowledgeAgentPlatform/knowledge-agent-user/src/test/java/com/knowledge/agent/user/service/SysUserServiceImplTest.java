package com.knowledge.agent.user.service;

import com.knowledge.agent.api.dto.UserProfileDTO;
import com.knowledge.agent.api.request.UserLoginRequest;
import com.knowledge.agent.common.exception.BizException;
import com.knowledge.agent.user.entity.SysUser;
import com.knowledge.agent.user.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SysUserServiceImplTest {

    @Test
    void shouldGenerateJwtForSuccessfulLogin() {
        SysUserMapper mapper = mock(SysUserMapper.class);
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("alice");
        user.setPassword(new BCryptPasswordEncoder().encode("password"));
        user.setLearningStyle("SOCRATIC");
        user.setPreferencesJson("{\"learningStyle\":\"SOCRATIC\"}");
        user.setDeleted(0);
        when(mapper.selectOne(any())).thenReturn(user);

        SysUserServiceImpl service = new SysUserServiceImpl(mapper);
        ReflectionTestUtils.setField(service, "tokenIssuer", "knowledge-agent-platform");
        ReflectionTestUtils.setField(service, "tokenSecret", "test-secret");
        ReflectionTestUtils.setField(service, "tokenTtl", Duration.ofHours(2));

        String token = service.login(new UserLoginRequest("alice", "password")).getData();
        assertNotNull(token);
    }

    @Test
    void shouldRejectDeletedUserDuringLogin() {
        SysUserMapper mapper = mock(SysUserMapper.class);
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("alice");
        user.setPassword(new BCryptPasswordEncoder().encode("password"));
        user.setDeleted(1);
        when(mapper.selectOne(any())).thenReturn(user);

        SysUserServiceImpl service = new SysUserServiceImpl(mapper);
        assertThrows(BizException.class, () -> service.login(new UserLoginRequest("alice", "password")));
    }

    @Test
    void shouldReturnStructuredUserProfile() {
        SysUserMapper mapper = mock(SysUserMapper.class);
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("alice");
        user.setLearningStyle("SOCRATIC");
        user.setPreferencesJson("{\"learningStyle\":\"SOCRATIC\"}");
        user.setDeleted(0);
        when(mapper.selectOne(any())).thenReturn(user);

        SysUserServiceImpl service = new SysUserServiceImpl(mapper);
        UserProfileDTO profile = service.getUserProfileDetail(1L).getData();

        assertEquals("alice", profile.getUsername());
        assertEquals("SOCRATIC", profile.getPreferences().get("learningStyle"));
    }
}
