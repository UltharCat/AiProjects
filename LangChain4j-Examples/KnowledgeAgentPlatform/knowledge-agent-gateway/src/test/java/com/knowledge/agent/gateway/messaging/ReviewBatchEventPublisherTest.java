package com.knowledge.agent.gateway.messaging;

import com.knowledge.agent.api.dto.ReviewTriggerSource;
import com.knowledge.agent.gateway.model.ReviewTaskBatchResponse;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ReviewBatchEventPublisherTest {

    @Test
    void shouldPublishReviewBatchEventWhenMessagingIsEnabled() {
        RocketMQTemplate rocketMQTemplate = mock(RocketMQTemplate.class);
        ReviewBatchEventPublisher publisher = new ReviewBatchEventPublisher();
        ReflectionTestUtils.setField(publisher, "rocketMQTemplate", rocketMQTemplate);
        ReflectionTestUtils.setField(publisher, "messagingEnabled", true);
        ReflectionTestUtils.setField(publisher, "reviewBatchTopic", "knowledge-agent-review-batch");

        publisher.publish(ReviewTaskBatchResponse.builder()
                .userId(1L)
                .triggerSource(ReviewTriggerSource.SCHEDULED)
                .requestedLimit(5)
                .dispatchedCount(1)
                .tasks(List.of())
                .build());

        verify(rocketMQTemplate).convertAndSend(eq("knowledge-agent-review-batch"), isA(Object.class));
    }
}
