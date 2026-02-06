package com.knowledge.agent.api.dto;

import java.io.Serializable;

public record UserLoginRequest(
        String username,
        String password
) implements Serializable {

}
