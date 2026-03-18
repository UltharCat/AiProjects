package com.knowledge.agent.core.messaging;

import com.knowledge.agent.api.event.KnowledgeArchivedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Minimal consumer used to validate the archived-knowledge event channel.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "knowledge-agent.messaging.enabled", havingValue = "true")
@RocketMQMessageListener(
        topic = "${knowledge-agent.messaging.knowledge-archived-topic:knowledge-agent-knowledge-archived}",
        consumerGroup = "${knowledge-agent.messaging.knowledge-archived-consumer-group:knowledge-agent-core-knowledge-consumer}"
)
public class KnowledgeArchivedEventConsumer implements RocketMQListener<KnowledgeArchivedEvent> {

    @Override
    public void onMessage(KnowledgeArchivedEvent event) {
        log.info("Consumed knowledge archived event. knowledgeId={}, userId={}, source={}",
                event.knowledgeId(), event.userId(), event.source());
    }
}
