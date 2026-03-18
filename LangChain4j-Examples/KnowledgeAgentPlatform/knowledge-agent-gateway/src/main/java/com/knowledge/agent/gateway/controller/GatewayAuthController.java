package com.knowledge.agent.gateway.controller;

import com.knowledge.agent.api.dto.ReviewTriggerSource;
import com.knowledge.agent.api.request.UserLoginRequest;
import com.knowledge.agent.api.service.UserService;
import com.knowledge.agent.common.auth.AuthTokenClaims;
import com.knowledge.agent.common.auth.JwtTokenUtils;
import com.knowledge.agent.common.exception.BizException;
import com.knowledge.agent.common.resp.Result;
import com.knowledge.agent.gateway.model.GatewayLoginResponse;
import com.knowledge.agent.gateway.model.ReviewTaskBatchResponse;
import com.knowledge.agent.gateway.review.ReviewTaskDispatcher;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 网关登录入口，负责统一登录返回体并触发登录后的待复习任务派发。
 */
@RestController
@RequestMapping("/api/auth")
public class GatewayAuthController {

    @DubboReference(check = false)
    private UserService userService;

    private final ReviewTaskDispatcher reviewTaskDispatcher;

    @Value("${knowledge-agent.auth.token-issuer:knowledge-agent-platform}")
    private String tokenIssuer;

    @Value("${knowledge-agent.auth.token-secret:knowledge-agent-dev-secret}")
    private String tokenSecret;

    public GatewayAuthController(ReviewTaskDispatcher reviewTaskDispatcher) {
        this.reviewTaskDispatcher = reviewTaskDispatcher;
    }

    @PostMapping("/login")
    public Result<GatewayLoginResponse> login(@RequestBody UserLoginRequest request) {
        // 第一步：委托 User 服务完成用户校验与 token 签发。
        Result<String> loginResult = userService.login(request);
        if (loginResult == null) {
            throw new BizException(500, "User service returned no response");
        }
        if (!Objects.equals(loginResult.getCode(), 200) || loginResult.getData() == null) {
            throw new BizException(loginResult.getCode(), loginResult.getMessage());
        }

        // 第二步：从 token 中解析用户身份，并同步派发登录触发的待复习任务。
        AuthTokenClaims claims = JwtTokenUtils.parseAndValidate(loginResult.getData(), tokenIssuer, tokenSecret);
        ReviewTaskBatchResponse reviewTasks = reviewTaskDispatcher.dispatch(claims.userId(), 5, ReviewTriggerSource.LOGIN);

        // 第三步：统一封装登录返回体，附带待复习任务摘要。
        return Result.success(GatewayLoginResponse.builder()
                .userId(claims.userId())
                .accessToken(loginResult.getData())
                .tokenType("Bearer")
                .expiresAt(claims.expiresAt())
                .pendingReviewCount(reviewTasks.dispatchedCount())
                .pendingReviewTasks(reviewTasks.tasks())
                .build());
    }
}
