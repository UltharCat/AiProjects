package com.knowledge.agent.gateway.review;

import com.knowledge.agent.api.dto.ReviewTriggerSource;
import com.knowledge.agent.api.service.UserService;
import com.knowledge.agent.common.resp.Result;
import com.knowledge.agent.gateway.model.ReviewTaskBatchResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 定时为活跃用户生成复习批次。
 */
@Slf4j
@Component
public class ReviewTaskScheduler {

    @DubboReference(check = false)
    private UserService userService;

    private final ReviewTaskDispatcher reviewTaskDispatcher;

    @Value("${knowledge-agent.review.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    @Value("${knowledge-agent.review.scheduler.batch-limit:5}")
    private Integer batchLimit;

    public ReviewTaskScheduler(ReviewTaskDispatcher reviewTaskDispatcher) {
        this.reviewTaskDispatcher = reviewTaskDispatcher;
    }

    @Scheduled(
            initialDelayString = "${knowledge-agent.review.scheduler.initial-delay-ms:30000}",
            fixedDelayString = "${knowledge-agent.review.scheduler.fixed-delay-ms:900000}"
    )
    public void generateScheduledReviewTasks() {
        if (!schedulerEnabled) {
            return;
        }

        Result<List<Long>> result = userService.listActiveUserIds();
        if (result == null || !Objects.equals(result.getCode(), 200)) {
            log.warn("Skip scheduled review generation because user list is unavailable. code={}, message={}",
                    result == null ? null : result.getCode(),
                    result == null ? null : result.getMessage());
            return;
        }

        List<Long> userIds = result.getData() == null ? Collections.emptyList() : result.getData();
        for (Long userId : userIds) {
            // 关键步骤：调度器逐个用户生成 SCHEDULED 批次，把到期知识卡片转换成可执行的复习任务。
            ReviewTaskBatchResponse batch = reviewTaskDispatcher.dispatch(userId, batchLimit, ReviewTriggerSource.SCHEDULED);
            if (batch.dispatchedCount() > 0) {
                log.info("Generated scheduled review batch for userId={}, taskCount={}", userId, batch.dispatchedCount());
            }
        }
    }
}
