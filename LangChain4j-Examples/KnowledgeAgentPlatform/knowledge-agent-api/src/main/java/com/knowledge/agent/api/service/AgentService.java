package com.knowledge.agent.api.service;

import com.knowledge.agent.common.resp.Result;

/**
 * Agent 核心交互服务
 * Dubbo 接口
 */
public interface AgentService {

    /**
     * 用户对话入口
     * @param userId 用户ID
     * @param query 用户输入
     * @return Agent 回复
     */
    Result<String> chat(Long userId, String query);

    /**
     * 强制状态流转 (用于 Function Calling 或 测试)
     * @param userId 用户ID
     * @param targetState 目标状态 (IDLE, TEACHING, REVIEW, SUMMARY)
     * @return void
     */
    Result<Void> switchState(Long userId, String targetState);
}
