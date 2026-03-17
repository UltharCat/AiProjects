package com.knowledge.agent.gateway.controller;

import com.knowledge.agent.api.request.UserLoginRequest;
import com.knowledge.agent.api.service.UserService;
import com.knowledge.agent.common.resp.Result;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class GatewayAuthController {

    @DubboReference(check = false)
    private UserService userService;

    @PostMapping("/login")
    public Result<String> login(@RequestBody UserLoginRequest request) {
        return userService.login(request);
    }
}
