package com.knowledge.agent.rag.messaging;

import com.knowledge.agent.api.dto.KnowledgeDTO;
import com.knowledge.agent.api.event.KnowledgeArchivedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Best-effort publisher for archived knowledge events.
 */
@Slf4j
@Component
public class KnowledgeArchivedEventPublisher {

    @Autowired(required = false)
    private RocketMQTemplate rocketMQTemplate;

    @Value("${knowledge-agent.messaging.enabled:false}")
    private boolean messagingEnabled;

    @Value("${knowledge-agent.messaging.knowledge-archived-topic:knowledge-agent-knowledge-archived}")
    private String knowledgeArchivedTopic;

    public void publish(KnowledgeDTO dto) {
        if (dto == null || dto.getId() == null || dto.getUserId() == null) {
            return;
        }
        if (!messagingEnabled || rocketMQTemplate == null) {
            log.debug("Skip knowledge archived event publishing because messaging is disabled or RocketMQTemplate is unavailable.");
            return;
        }

        KnowledgeArchivedEvent event = KnowledgeArchivedEvent.builder()
                .knowledgeId(dto.getId())
                .userId(dto.getUserId())
                .source(dto.getSource())
                .tags(dto.getTags())
                .occurredAt(Instant.now())
                .build();
        rocketMQTemplate.convertAndSend(knowledgeArchivedTopic, event);
        log.info("Published knowledge archived event. knowledgeId={}, userId={}", dto.getId(), dto.getUserId());
    }
}
