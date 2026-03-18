package com.knowledge.agent.api.service;

import com.knowledge.agent.api.dto.UserProfileDTO;
import com.knowledge.agent.api.request.UserLoginRequest;
import com.knowledge.agent.common.resp.Result;

import java.util.List;

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

    /**
     * Return the structured user profile for HTTP or richer orchestration usage.
     * @param userId target user id
     * @return structured user profile
     */
    Result<UserProfileDTO> getUserProfileDetail(Long userId);

    /**
     * Return all active user ids for review scheduling.
     * @return active user ids
     */
    Result<List<Long>> listActiveUserIds();
}
