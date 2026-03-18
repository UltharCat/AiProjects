package com.knowledge.agent.rag.messaging;

import com.knowledge.agent.api.dto.KnowledgeDTO;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class KnowledgeArchivedEventPublisherTest {

    @Test
    void shouldPublishKnowledgeArchivedEventWhenMessagingIsEnabled() {
        RocketMQTemplate rocketMQTemplate = mock(RocketMQTemplate.class);
        KnowledgeArchivedEventPublisher publisher = new KnowledgeArchivedEventPublisher();
        ReflectionTestUtils.setField(publisher, "rocketMQTemplate", rocketMQTemplate);
        ReflectionTestUtils.setField(publisher, "messagingEnabled", true);
        ReflectionTestUtils.setField(publisher, "knowledgeArchivedTopic", "knowledge-agent-knowledge-archived");

        publisher.publish(KnowledgeDTO.builder()
                .id(9L)
                .userId(1L)
                .source("manual")
                .tags(Set.of("summary"))
                .build());

        verify(rocketMQTemplate).convertAndSend(eq("knowledge-agent-knowledge-archived"), isA(Object.class));
    }
}
