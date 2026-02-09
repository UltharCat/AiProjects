package com.knowledge.agent.api.service;

import com.knowledge.agent.api.request.UserLoginRequest;
import com.knowledge.agent.common.resp.Result;

public interface UserService {

    /**
     * 定义登录接口，返回 JWT token 或 用户信息
     * @param request
     * @return
     */
    Result<String> login(UserLoginRequest request);

    /**
     * 获取用户偏好设置
     * @param userId
     * @return
     */
    Result<String> getUserProfile(Long userId);
}
