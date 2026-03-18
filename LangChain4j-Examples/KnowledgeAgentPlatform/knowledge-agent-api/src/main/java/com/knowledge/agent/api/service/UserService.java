package com.knowledge.agent.api.service;

import com.knowledge.agent.api.dto.UserProfileDTO;
import com.knowledge.agent.api.request.UserLoginRequest;
import com.knowledge.agent.common.resp.Result;

import java.util.List;

/**
 * 用户服务的 Dubbo 接口。
 */
public interface UserService {

    /**
     * 用户登录接口，返回访问令牌。
     *
     * @param request 登录请求
     * @return 登录结果
     */
    Result<String> login(UserLoginRequest request);

    /**
     * 获取用户偏好信息的 JSON 表达。
     *
     * @param userId 用户 ID
     * @return 用户偏好 JSON
     */
    Result<String> getUserProfile(Long userId);

    /**
     * 获取结构化用户画像。
     *
     * @param userId 用户 ID
     * @return 结构化用户画像
     */
    Result<UserProfileDTO> getUserProfileDetail(Long userId);

    /**
     * 查询所有活跃用户 ID，用于 review 调度。
     *
     * @return 活跃用户 ID 列表
     */
    Result<List<Long>> listActiveUserIds();
}
