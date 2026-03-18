package com.knowledge.agent.api.dto;

/**
 * Delivery lifecycle of a review task exposed to the client.
 */
public enum ReviewTaskStatus {
    PENDING,
    DISPATCHED,
    COMPLETED,
    SKIPPED
}
