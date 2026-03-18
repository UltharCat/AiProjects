package com.knowledge.agent.api.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReviewTaskDtoSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void shouldSerializeAndDeserializeReviewTaskDto() throws Exception {
        ReviewTaskDTO task = ReviewTaskDTO.builder()
                .taskId("task-1")
                .userId(1L)
                .knowledgeId(9L)
                .summary("SM-2 overview")
                .dueAt(LocalDateTime.of(2026, 3, 18, 10, 0))
                .triggerSource(ReviewTriggerSource.LOGIN)
                .status(ReviewTaskStatus.PENDING)
                .dedupKey("LOGIN:1:9")
                .build();

        String json = objectMapper.writeValueAsString(task);
        ReviewTaskDTO restored = objectMapper.readValue(json, ReviewTaskDTO.class);

        assertEquals(task.getTaskId(), restored.getTaskId());
        assertEquals(task.getTriggerSource(), restored.getTriggerSource());
        assertEquals(task.getDedupKey(), restored.getDedupKey());
    }
}
