package com.knowledge.agent.api.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * Structured user profile shared by User, Gateway, and Agent modules.
 */
@Data
@Builder
public class UserProfileDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    private String username;

    private String learningStyle;

    private String preferencesJson;

    private Map<String, String> preferences;
}
