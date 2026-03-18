package com.knowledge.agent.core.messaging;

import com.knowledge.agent.api.event.ReviewBatchGeneratedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Minimal consumer used to validate the review-batch event channel.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "knowledge-agent.messaging.enabled", havingValue = "true")
@RocketMQMessageListener(
        topic = "${knowledge-agent.messaging.review-batch-topic:knowledge-agent-review-batch}",
        consumerGroup = "${knowledge-agent.messaging.review-batch-consumer-group:knowledge-agent-core-review-consumer}"
)
public class ReviewBatchGeneratedEventConsumer implements RocketMQListener<ReviewBatchGeneratedEvent> {

    @Override
    public void onMessage(ReviewBatchGeneratedEvent event) {
        log.info("Consumed review batch event. triggerSource={}, userId={}, dispatchedCount={}",
                event.triggerSource(), event.userId(), event.dispatchedCount());
    }
}
