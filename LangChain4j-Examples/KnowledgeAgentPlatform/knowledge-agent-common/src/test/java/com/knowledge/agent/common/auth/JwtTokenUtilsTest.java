package com.knowledge.agent.common.auth;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenUtilsTest {

    @Test
    void shouldGenerateAndParseToken() {
        String token = JwtTokenUtils.generateToken(
                7L,
                "alice",
                "SOCRATIC",
                "knowledge-agent-platform",
                "test-secret",
                Duration.ofHours(2)
        );

        AuthTokenClaims claims = JwtTokenUtils.parseAndValidate(token, "knowledge-agent-platform", "test-secret");
        assertEquals(7L, claims.userId());
        assertEquals("alice", claims.username());
        assertEquals("SOCRATIC", claims.learningStyle());
    }

    @Test
    void shouldRejectTokenWithUnexpectedIssuer() {
        String token = JwtTokenUtils.generateToken(
                7L,
                "alice",
                "SOCRATIC",
                "knowledge-agent-platform",
                "test-secret",
                Duration.ofHours(2)
        );

        assertThrows(IllegalArgumentException.class,
                () -> JwtTokenUtils.parseAndValidate(token, "other-issuer", "test-secret"));
    }
}
