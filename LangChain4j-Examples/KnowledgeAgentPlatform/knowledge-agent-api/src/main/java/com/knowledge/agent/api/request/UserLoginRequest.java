package com.knowledge.agent.api.request;

import java.io.Serializable;

public record UserLoginRequest(
        String username,
        String password
) implements Serializable {

}
