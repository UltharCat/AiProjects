package com.knowledge.agent.rag.service;

import com.knowledge.agent.api.dto.ReviewTaskBatchDTO;
import com.knowledge.agent.api.dto.ReviewTaskDTO;
import com.knowledge.agent.api.dto.ReviewTaskStatus;
import com.knowledge.agent.api.dto.ReviewTriggerSource;
import com.knowledge.agent.common.resp.Result;
import com.knowledge.agent.rag.entity.ReviewTaskRecord;
import com.knowledge.agent.rag.mapper.KnowledgeCardMapper;
import com.knowledge.agent.rag.mapper.ReviewTaskRecordMapper;
import com.knowledge.agent.rag.messaging.KnowledgeArchivedEventPublisher;
import io.milvus.v2.client.MilvusClientV2;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RagServiceImplReviewTaskTest {

    @Test
    void shouldPersistReviewTaskBatch() {
        ReviewTaskRecordMapper reviewTaskRecordMapper = mock(ReviewTaskRecordMapper.class);
        RagServiceImpl service = new RagServiceImpl(
                mock(EmbeddingModel.class),
                mock(MilvusClientV2.class),
                mock(KnowledgeCardMapper.class),
                reviewTaskRecordMapper,
                mock(KnowledgeArchivedEventPublisher.class)
        );
        when(reviewTaskRecordMapper.insert(any(ReviewTaskRecord.class))).thenReturn(1);

        Result<Boolean> result = service.saveReviewTaskBatch(ReviewTaskBatchDTO.builder()
                .batchId("batch-1")
                .userId(1L)
                .triggerSource(ReviewTriggerSource.SCHEDULED)
                .requestedLimit(5)
                .createdAt(LocalDateTime.of(2026, 3, 18, 10, 0))
                .tasks(List.of(ReviewTaskDTO.builder()
                        .taskId("task-1")
                        .userId(1L)
                        .knowledgeId(10L)
                        .summary("Review JVM memory")
                        .dueAt(LocalDateTime.of(2026, 3, 18, 11, 0))
                        .triggerSource(ReviewTriggerSource.SCHEDULED)
                        .status(ReviewTaskStatus.DISPATCHED)
                        .dedupKey("SCHEDULED:1:10")
                        .build()))
                .build());

        assertEquals(200, result.getCode());
        verify(reviewTaskRecordMapper).delete(any());
        verify(reviewTaskRecordMapper).insert(any(ReviewTaskRecord.class));
    }
}
