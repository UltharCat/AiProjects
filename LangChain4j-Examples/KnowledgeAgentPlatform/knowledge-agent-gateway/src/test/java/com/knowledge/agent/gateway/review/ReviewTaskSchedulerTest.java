package com.knowledge.agent.gateway.review;

import com.knowledge.agent.api.service.UserService;
import com.knowledge.agent.common.resp.Result;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviewTaskSchedulerTest {

    @Test
    void shouldGenerateScheduledBatchesForActiveUsers() {
        UserService userService = mock(UserService.class);
        ReviewTaskDispatcher dispatcher = mock(ReviewTaskDispatcher.class);
        when(userService.listActiveUserIds()).thenReturn(Result.success(List.of(1L, 2L)));

        ReviewTaskScheduler scheduler = new ReviewTaskScheduler(dispatcher);
        ReflectionTestUtils.setField(scheduler, "userService", userService);
        ReflectionTestUtils.setField(scheduler, "schedulerEnabled", true);
        ReflectionTestUtils.setField(scheduler, "batchLimit", 5);
        when(dispatcher.dispatch(1L, 5, com.knowledge.agent.api.dto.ReviewTriggerSource.SCHEDULED)).thenReturn(
                com.knowledge.agent.gateway.model.ReviewTaskBatchResponse.builder()
                        .userId(1L)
                        .triggerSource(com.knowledge.agent.api.dto.ReviewTriggerSource.SCHEDULED)
                        .requestedLimit(5)
                        .dispatchedCount(1)
                        .tasks(List.of())
                        .build()
        );
        when(dispatcher.dispatch(2L, 5, com.knowledge.agent.api.dto.ReviewTriggerSource.SCHEDULED)).thenReturn(
                com.knowledge.agent.gateway.model.ReviewTaskBatchResponse.builder()
                        .userId(2L)
                        .triggerSource(com.knowledge.agent.api.dto.ReviewTriggerSource.SCHEDULED)
                        .requestedLimit(5)
                        .dispatchedCount(0)
                        .tasks(List.of())
                        .build()
        );

        scheduler.generateScheduledReviewTasks();

        verify(dispatcher, times(1)).dispatch(1L, 5, com.knowledge.agent.api.dto.ReviewTriggerSource.SCHEDULED);
        verify(dispatcher, times(1)).dispatch(2L, 5, com.knowledge.agent.api.dto.ReviewTriggerSource.SCHEDULED);
    }
}
