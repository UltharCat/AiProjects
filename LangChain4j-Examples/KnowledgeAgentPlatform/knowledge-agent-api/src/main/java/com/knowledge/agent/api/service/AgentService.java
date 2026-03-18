package com.knowledge.agent.api.service;

import com.knowledge.agent.common.resp.Result;

/**
 * Agent 核心交互服务的 Dubbo 接口。
 */
public interface AgentService {

    /**
     * 用户对话入口。
     *
     * @param userId 用户 ID
     * @param query 用户输入
     * @return Agent 回复内容
     */
    Result<String> chat(Long userId, String query);

    /**
     * 强制切换当前会话状态。
     *
     * @param userId 用户 ID
     * @param targetState 目标状态，例如 IDLE、TEACHING、REVIEW、SUMMARY
     * @return 切换结果
     */
    Result<Void> switchState(Long userId, String targetState);
}
