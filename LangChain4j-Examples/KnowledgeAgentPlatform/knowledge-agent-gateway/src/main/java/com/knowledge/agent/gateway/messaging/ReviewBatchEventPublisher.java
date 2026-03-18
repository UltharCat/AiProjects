package com.knowledge.agent.gateway.messaging;

import com.knowledge.agent.api.dto.ReviewTriggerSource;
import com.knowledge.agent.api.event.ReviewBatchGeneratedEvent;
import com.knowledge.agent.gateway.model.ReviewTaskBatchResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Best-effort publisher for review batch events.
 */
@Slf4j
@Component
public class ReviewBatchEventPublisher {

    @Autowired(required = false)
    private RocketMQTemplate rocketMQTemplate;

    @Value("${knowledge-agent.messaging.enabled:false}")
    private boolean messagingEnabled;

    @Value("${knowledge-agent.messaging.review-batch-topic:knowledge-agent-review-batch}")
    private String reviewBatchTopic;

    public void publish(ReviewTaskBatchResponse batch) {
        if (batch == null || batch.userId() == null || batch.triggerSource() == null) {
            return;
        }
        if (!messagingEnabled || rocketMQTemplate == null) {
            log.debug("Skip review batch event publishing because messaging is disabled or RocketMQTemplate is unavailable.");
            return;
        }

        ReviewBatchGeneratedEvent event = ReviewBatchGeneratedEvent.builder()
                .userId(batch.userId())
                .triggerSource(batch.triggerSource())
                .requestedLimit(batch.requestedLimit())
                .dispatchedCount(batch.dispatchedCount())
                .tasks(batch.tasks())
                .occurredAt(Instant.now())
                .build();
        rocketMQTemplate.convertAndSend(reviewBatchTopic, event);
        log.info("Published review batch event. triggerSource={}, userId={}, dispatchedCount={}",
                batch.triggerSource(), batch.userId(), batch.dispatchedCount());
    }

    public boolean supports(ReviewTriggerSource triggerSource) {
        return triggerSource != null;
    }
}
