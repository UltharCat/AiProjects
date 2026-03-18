package com.knowledge.agent.common.auth;

import java.time.Instant;

/**
 * Parsed claims extracted from a signed access token.
 */
public record AuthTokenClaims(
        Long userId,
        String username,
        String learningStyle,
        String issuer,
        Instant issuedAt,
        Instant expiresAt
) {
}
