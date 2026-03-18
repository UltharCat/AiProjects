package com.knowledge.agent.api.dto;

/**
 * 复习任务状态枚举。
 */
public enum ReviewTaskStatus {
    /**
     * 已生成但尚未开始处理。
     */
    PENDING,
    /**
     * 已派发到客户端或对话流程。
     */
    DISPATCHED,
    /**
     * 已完成复习并回写结果。
     */
    COMPLETED,
    /**
     * 已跳过，当前仅保留扩展位。
     */
    SKIPPED
}
