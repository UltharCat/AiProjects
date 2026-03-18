package com.knowledge.agent.user.controller;

import cn.hutool.core.util.StrUtil;
import com.knowledge.agent.api.dto.UserLoginSessionDTO;
import com.knowledge.agent.api.dto.UserProfileDTO;
import com.knowledge.agent.api.request.UserLoginRequest;
import com.knowledge.agent.api.service.UserService;
import com.knowledge.agent.common.auth.AuthTokenClaims;
import com.knowledge.agent.common.auth.JwtTokenUtils;
import com.knowledge.agent.common.exception.BizException;
import com.knowledge.agent.common.resp.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/user")
public class UserController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final UserService userService;

    @Value("${knowledge-agent.auth.token-issuer:knowledge-agent-platform}")
    private String tokenIssuer;

    @Value("${knowledge-agent.auth.token-secret:knowledge-agent-dev-secret}")
    private String tokenSecret;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<UserLoginSessionDTO> login(@RequestBody UserLoginRequest request) {
        Result<String> loginResult = userService.login(request);
        if (loginResult == null || !Objects.equals(loginResult.getCode(), 200) || StrUtil.isBlank(loginResult.getData())) {
            throw new BizException(loginResult == null ? 500 : loginResult.getCode(), loginResult == null ? "User service returned no response" : loginResult.getMessage());
        }

        AuthTokenClaims claims = JwtTokenUtils.parseAndValidate(loginResult.getData(), tokenIssuer, tokenSecret);
        Result<UserProfileDTO> profileResult = userService.getUserProfileDetail(claims.userId());
        return Result.success(UserLoginSessionDTO.builder()
                .userId(claims.userId())
                .accessToken(loginResult.getData())
                .tokenType("Bearer")
                .expiresAt(claims.expiresAt())
                .profile(profileResult == null ? null : profileResult.getData())
                .build());
    }

    @GetMapping("/profile")
    public Result<UserProfileDTO> profile(@RequestHeader("Authorization") String authorization) {
        if (StrUtil.isBlank(authorization) || !StrUtil.startWithIgnoreCase(authorization, BEARER_PREFIX)) {
            throw new BizException(401, "Missing bearer token");
        }
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        AuthTokenClaims claims = JwtTokenUtils.parseAndValidate(token, tokenIssuer, tokenSecret);
        return userService.getUserProfileDetail(claims.userId());
    }
}
