package com.knowledge.agent.api.dto;

import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * Token-backed user login session returned by the User HTTP endpoint.
 */
@Builder
public record UserLoginSessionDTO(
        Long userId,
        String accessToken,
        String tokenType,
        Instant expiresAt,
        UserProfileDTO profile
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
