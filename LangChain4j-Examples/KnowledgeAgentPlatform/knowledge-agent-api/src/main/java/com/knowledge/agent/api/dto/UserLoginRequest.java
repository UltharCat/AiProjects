package com.knowledge.agent.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {

    private String username;

    private String password;

}
