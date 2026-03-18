package com.knowledge.agent.gateway.model;

import java.io.Serializable;

/**
 * External chat request accepted by the Gateway.
 */
public record GatewayChatRequest(
        String prompt,
        String sessionId
) implements Serializable {
}
