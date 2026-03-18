package com.knowledge.agent.api.dto;

/**
 * 复习任务批次的触发来源枚举。
 */
public enum ReviewTriggerSource {
    /**
     * 用户登录后触发。
     */
    LOGIN,
    /**
     * 用户手动主动触发。
     */
    MANUAL,
    /**
     * 定时调度触发。
     */
    SCHEDULED
}
