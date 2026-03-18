package com.knowledge.agent.rag.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.knowledge.agent.api.dto.KnowledgeDTO;
import com.knowledge.agent.api.dto.ReviewTaskBatchDTO;
import com.knowledge.agent.api.dto.ReviewTaskDTO;
import com.knowledge.agent.api.dto.ReviewTaskStatus;
import com.knowledge.agent.api.dto.ReviewTriggerSource;
import com.knowledge.agent.api.service.RagService;
import com.knowledge.agent.common.exception.BizException;
import com.knowledge.agent.common.resp.Result;
import com.knowledge.agent.common.utils.EbbinghausUtils;
import com.knowledge.agent.rag.config.MilvusHybridRetriever;
import com.knowledge.agent.rag.entity.KnowledgeCard;
import com.knowledge.agent.rag.entity.ReviewTaskRecord;
import com.knowledge.agent.rag.mapper.KnowledgeCardMapper;
import com.knowledge.agent.rag.mapper.ReviewTaskRecordMapper;
import com.knowledge.agent.rag.messaging.KnowledgeArchivedEventPublisher;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.query.Query;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.InsertReq;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@DubboService
public class RagServiceImpl implements RagService {

    @Value("${milvus.cloud.collection-name}")
    private String collectionName;

    private final EmbeddingModel embeddingModel;
    private final MilvusClientV2 milvusClientV2;
    private final KnowledgeCardMapper knowledgeCardMapper;
    private final ReviewTaskRecordMapper reviewTaskRecordMapper;
    private final KnowledgeArchivedEventPublisher knowledgeArchivedEventPublisher;

    public RagServiceImpl(EmbeddingModel embeddingModel,
                          MilvusClientV2 milvusClientV2,
                          KnowledgeCardMapper knowledgeCardMapper,
                          ReviewTaskRecordMapper reviewTaskRecordMapper,
                          KnowledgeArchivedEventPublisher knowledgeArchivedEventPublisher) {
        this.embeddingModel = embeddingModel;
        this.milvusClientV2 = milvusClientV2;
        this.knowledgeCardMapper = knowledgeCardMapper;
        this.reviewTaskRecordMapper = reviewTaskRecordMapper;
        this.knowledgeArchivedEventPublisher = knowledgeArchivedEventPublisher;
    }

    @Override
    @Transactional
    public Result<Boolean> saveKnowledge(KnowledgeDTO dto) {
        if (dto == null || StrUtil.isBlank(dto.getSummary())) {
            return Result.error(400, "Invalid knowledge payload");
        }
        dto.setSummary(normalizeSummary(dto.getSummary()));
        dto.setSource(normalizeSource(dto.getSource()));
        dto.setTags(normalizeTags(dto.getTags()));

        List<Content> retrieve = new MilvusHybridRetriever(collectionName, embeddingModel, milvusClientV2, 2)
                .retrieve(Query.from(dto.getSummary()));
        if (CollUtil.isNotEmpty(retrieve)) {
            Set<Long> docIds = retrieve.stream()
                    .map(content -> content.textSegment().metadata().getLong("doc_id"))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            for (Long id : docIds) {
                dto.setId(id);
                saveMilvusKnowledge(dto);
                upsertKnowledgeCard(dto);
                knowledgeArchivedEventPublisher.publish(dto);
            }
        } else {
            dto.setId(IdUtil.getSnowflakeNextId());
            saveMilvusKnowledge(dto);
            upsertKnowledgeCard(dto);
            knowledgeArchivedEventPublisher.publish(dto);
        }
        return Result.success(Boolean.TRUE);
    }

    private void saveMilvusKnowledge(KnowledgeDTO dto) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("doc_id", dto.getId());
        metadata.put("user_id", dto.getUserId());
        metadata.put("tags", JSON.toJSONString(dto.getTags()));
        metadata.put("source", dto.getSource());

        Document knowledgeDoc = Document.from(dto.getSummary(), Metadata.from(metadata));
        List<TextSegment> segments = DocumentSplitters.recursive(1000, 100).split(knowledgeDoc);
        Gson gson = new Gson();
        var rows = segments.stream().map(segment -> {
            String text = segment.text();
            float[] vector = embeddingModel.embed(text).content().vector();
            JsonObject row = new JsonObject();
            row.addProperty("text", text);
            row.add("text_dense", gson.toJsonTree(vector));
            row.add("metadata", gson.toJsonTree(metadata));
            return row;
        }).collect(Collectors.toList());
        milvusClientV2.insert(InsertReq.builder().collectionName(collectionName).data(rows).build());
    }

    private void upsertKnowledgeCard(KnowledgeDTO dto) {
        KnowledgeCard entity = KnowledgeCard.builder()
                .docId(dto.getId())
                .userId(dto.getUserId())
                .summary(dto.getSummary())
                .source(dto.getSource())
                .tagsJson(dto.getTags() == null ? null : JSON.toJSONString(dto.getTags()))
                .easinessFactor(dto.getEasinessFactor() != null ? dto.getEasinessFactor() : 2.5d)
                .intervalDays(dto.getIntervalDays() != null ? dto.getIntervalDays() : 1)
                .repetition(dto.getRepetition() != null ? dto.getRepetition() : 0)
                .nextReviewDate(dto.getNextReviewDate() != null ? dto.getNextReviewDate() : LocalDate.now().plusDays(1).atStartOfDay())
                .build();
        KnowledgeCard existing = knowledgeCardMapper.selectById(dto.getId());
        if (existing == null) {
            knowledgeCardMapper.insert(entity);
        } else {
            knowledgeCardMapper.updateById(entity);
        }
    }

    @Override
    @Transactional
    public Result<Void> updateReviewStatus(Long id, int quality) {
        KnowledgeCard card = knowledgeCardMapper.selectById(id);
        if (card == null) {
            throw new BizException(404, "Knowledge card not found");
        }

        EbbinghausUtils.ReviewResult compute = EbbinghausUtils.compute(
                quality,
                card.getEasinessFactor(),
                card.getIntervalDays(),
                card.getRepetition(),
                LocalDate.now()
        );
        knowledgeCardMapper.updateById(
                KnowledgeCard.builder()
                        .docId(id)
                        .easinessFactor(compute.newEf())
                        .intervalDays(compute.newInterval())
                        .repetition(compute.newRepetitions())
                        .nextReviewDate(compute.nextReviewDate().atStartOfDay())
                        .build()
        );
        return Result.success(null);
    }

    @Override
    public Result<List<KnowledgeDTO>> searchKnowledge(Long userId, String query, Integer limit) {
        return searchKnowledgeWithFilters(userId, query, limit, null);
    }

    @Override
    public Result<List<KnowledgeDTO>> searchKnowledgeWithFilters(Long userId, String query, Integer limit, Set<String> tags) {
        if (StrUtil.isBlank(query)) {
            return Result.success(Collections.emptyList());
        }
        int size = limit == null ? 3 : Math.max(1, limit);
        List<Content> retrieve = new MilvusHybridRetriever(collectionName, embeddingModel, milvusClientV2, size)
                .retrieve(Query.from(query));
        if (CollUtil.isEmpty(retrieve)) {
            return Result.success(Collections.emptyList());
        }

        Map<Long, Integer> rankMap = new HashMap<>();
        Map<Long, String> textMap = new HashMap<>();
        for (Content content : retrieve) {
            Long docId = content.textSegment().metadata().getLong("doc_id");
            if (docId == null || rankMap.containsKey(docId)) {
                continue;
            }
            rankMap.put(docId, rankMap.size());
            textMap.put(docId, content.textSegment().text());
        }
        if (rankMap.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        Map<Long, KnowledgeCard> cardMap = knowledgeCardMapper.selectList(
                        Wrappers.<KnowledgeCard>lambdaQuery()
                                .in(KnowledgeCard::getDocId, rankMap.keySet())
                                .eq(userId != null, KnowledgeCard::getUserId, userId)
                ).stream()
                .collect(Collectors.toMap(KnowledgeCard::getDocId, card -> card));

        List<KnowledgeDTO> result = rankMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(entry -> toKnowledgeDTO(cardMap.get(entry.getKey()), entry.getKey(), textMap.get(entry.getKey()), entry.getValue()))
                .filter(dto -> matchesRequestedTags(dto, tags))
                .filter(Objects::nonNull)
                .limit(size)
                .toList();
        return Result.success(result);
    }

    @Override
    public Result<List<KnowledgeDTO>> listPendingReviews(Long userId, Integer limit) {
        int size = limit == null ? 5 : Math.max(1, limit);
        List<KnowledgeDTO> data = knowledgeCardMapper.selectList(
                        Wrappers.<KnowledgeCard>lambdaQuery()
                                .eq(userId != null, KnowledgeCard::getUserId, userId)
                                .eq(KnowledgeCard::getDeleted, 0)
                                .and(wrapper -> wrapper.isNull(KnowledgeCard::getNextReviewDate)
                                        .or()
                                        .le(KnowledgeCard::getNextReviewDate, LocalDateTime.now()))
                                .orderByAsc(KnowledgeCard::getNextReviewDate)
                                .last("limit " + size)
                ).stream()
                .map(card -> toKnowledgeDTO(card, card.getDocId(), card.getSummary(), null))
                .toList();
        return Result.success(data);
    }

    @Override
    @Transactional
    public Result<List<Long>> importKnowledgeBatch(Long userId, List<KnowledgeDTO> documents) {
        if (CollUtil.isEmpty(documents)) {
            return Result.success(Collections.emptyList());
        }
        List<Long> ids = documents.stream()
                .filter(Objects::nonNull)
                .map(document -> {
                    document.setUserId(document.getUserId() == null ? userId : document.getUserId());
                    document.setSummary(normalizeSummary(document.getSummary()));
                    document.setSource(normalizeSource(document.getSource()));
                    document.setTags(normalizeTags(document.getTags()));
                    saveKnowledge(document);
                    return document.getId();
                })
                .filter(Objects::nonNull)
                .toList();
        return Result.success(ids);
    }

    @Override
    @Transactional
    public Result<Boolean> saveReviewTaskBatch(ReviewTaskBatchDTO batch) {
        if (batch == null || StrUtil.isBlank(batch.getBatchId()) || batch.getUserId() == null) {
            return Result.error(400, "Invalid review batch payload");
        }
        List<ReviewTaskDTO> tasks = batch.getTasks() == null ? Collections.emptyList() : batch.getTasks();
        reviewTaskRecordMapper.delete(Wrappers.<ReviewTaskRecord>lambdaQuery()
                .eq(ReviewTaskRecord::getBatchId, batch.getBatchId()));
        for (ReviewTaskDTO task : tasks) {
            if (task == null || StrUtil.isBlank(task.getTaskId()) || task.getKnowledgeId() == null) {
                continue;
            }
            reviewTaskRecordMapper.insert(ReviewTaskRecord.builder()
                    .taskId(task.getTaskId())
                    .batchId(batch.getBatchId())
                    .userId(batch.getUserId())
                    .knowledgeId(task.getKnowledgeId())
                    .summary(task.getSummary())
                    .dueAt(task.getDueAt())
                    .triggerSource(task.getTriggerSource() == null ? batch.getTriggerSource().name() : task.getTriggerSource().name())
                    .status(task.getStatus() == null ? ReviewTaskStatus.PENDING.name() : task.getStatus().name())
                    .dedupKey(task.getDedupKey())
                    .requestedLimit(batch.getRequestedLimit())
                    .batchCreatedAt(batch.getCreatedAt() == null ? LocalDateTime.now() : batch.getCreatedAt())
                    .build());
        }
        return Result.success(Boolean.TRUE);
    }

    @Override
    public Result<ReviewTaskBatchDTO> findLatestReviewTaskBatch(Long userId, ReviewTriggerSource triggerSource) {
        if (userId == null || triggerSource == null) {
            return Result.success(null);
        }
        ReviewTaskRecord latestRecord = reviewTaskRecordMapper.selectOne(Wrappers.<ReviewTaskRecord>lambdaQuery()
                .eq(ReviewTaskRecord::getUserId, userId)
                .eq(ReviewTaskRecord::getTriggerSource, triggerSource.name())
                .orderByDesc(ReviewTaskRecord::getBatchCreatedAt)
                .last("limit 1"));
        if (latestRecord == null) {
            return Result.success(null);
        }
        List<ReviewTaskRecord> records = reviewTaskRecordMapper.selectList(Wrappers.<ReviewTaskRecord>lambdaQuery()
                .eq(ReviewTaskRecord::getBatchId, latestRecord.getBatchId())
                .orderByAsc(ReviewTaskRecord::getCreateTime));
        return Result.success(toReviewTaskBatchDTO(latestRecord, records));
    }

    @Override
    @Transactional
    public Result<Boolean> updateReviewTaskStatus(Long userId, Long knowledgeId, ReviewTaskStatus status) {
        if (userId == null || knowledgeId == null || status == null) {
            return Result.error(400, "Invalid review task status update");
        }
        List<ReviewTaskRecord> records = reviewTaskRecordMapper.selectList(Wrappers.<ReviewTaskRecord>lambdaQuery()
                .eq(ReviewTaskRecord::getUserId, userId)
                .eq(ReviewTaskRecord::getKnowledgeId, knowledgeId)
                .in(ReviewTaskRecord::getStatus, ReviewTaskStatus.PENDING.name(), ReviewTaskStatus.DISPATCHED.name())
                .orderByDesc(ReviewTaskRecord::getBatchCreatedAt));
        if (CollUtil.isEmpty(records)) {
            return Result.success(Boolean.FALSE);
        }
        for (ReviewTaskRecord record : records) {
            reviewTaskRecordMapper.updateById(ReviewTaskRecord.builder()
                    .taskId(record.getTaskId())
                    .status(status.name())
                    .build());
        }
        return Result.success(Boolean.TRUE);
    }

    private KnowledgeDTO toKnowledgeDTO(KnowledgeCard card, Long docId, String fallbackSummary, Integer rankIndex) {
        if (card == null && StrUtil.isBlank(fallbackSummary)) {
            return null;
        }
        String summary = card != null && StrUtil.isNotBlank(card.getSummary()) ? card.getSummary() : fallbackSummary;
        String source = card == null ? "manual" : normalizeSource(card.getSource());
        Double score = rankIndex == null ? null : 1d / (rankIndex + 1);
        return KnowledgeDTO.builder()
                .id(docId)
                .userId(card == null ? null : card.getUserId())
                .summary(summary)
                .source(source)
                .tags(parseTags(card == null ? null : card.getTagsJson()))
                .score(score)
                .citation(buildCitation(source, docId))
                .directAnswer(rankIndex != null && rankIndex == 0 ? summary : null)
                .matchedSegment(fallbackSummary)
                .easinessFactor(card == null ? null : card.getEasinessFactor())
                .intervalDays(card == null ? null : card.getIntervalDays())
                .repetition(card == null ? null : card.getRepetition())
                .nextReviewDate(card == null ? null : card.getNextReviewDate())
                .build();
    }

    private Set<String> parseTags(String tagsJson) {
        if (StrUtil.isBlank(tagsJson)) {
            return Collections.emptySet();
        }
        return new LinkedHashSet<>(JSON.parseArray(tagsJson, String.class));
    }

    private String normalizeSummary(String summary) {
        return summary == null ? null : summary.replaceAll("\\s+", " ").trim();
    }

    private String normalizeSource(String source) {
        return StrUtil.blankToDefault(StrUtil.trim(source), "manual");
    }

    private Set<String> normalizeTags(Set<String> tags) {
        if (CollUtil.isEmpty(tags)) {
            return Collections.emptySet();
        }
        return tags.stream()
                .filter(StrUtil::isNotBlank)
                .map(StrUtil::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private boolean matchesRequestedTags(KnowledgeDTO dto, Set<String> requestedTags) {
        if (dto == null) {
            return false;
        }
        if (CollUtil.isEmpty(requestedTags)) {
            return true;
        }
        Set<String> normalizedRequestedTags = normalizeTags(requestedTags);
        return dto.getTags() != null && dto.getTags().containsAll(normalizedRequestedTags);
    }

    private String buildCitation(String source, Long docId) {
        return "%s#%s".formatted(normalizeSource(source), docId);
    }

    private ReviewTaskBatchDTO toReviewTaskBatchDTO(ReviewTaskRecord latestRecord, List<ReviewTaskRecord> records) {
        List<ReviewTaskDTO> tasks = records.stream()
                .map(record -> ReviewTaskDTO.builder()
                        .taskId(record.getTaskId())
                        .userId(record.getUserId())
                        .knowledgeId(record.getKnowledgeId())
                        .summary(record.getSummary())
                        .dueAt(record.getDueAt())
                        .triggerSource(ReviewTriggerSource.valueOf(record.getTriggerSource()))
                        .status(ReviewTaskStatus.valueOf(record.getStatus()))
                        .dedupKey(record.getDedupKey())
                        .build())
                .toList();
        return ReviewTaskBatchDTO.builder()
                .batchId(latestRecord.getBatchId())
                .userId(latestRecord.getUserId())
                .triggerSource(ReviewTriggerSource.valueOf(latestRecord.getTriggerSource()))
                .requestedLimit(latestRecord.getRequestedLimit())
                .dispatchedCount(tasks.size())
                .createdAt(latestRecord.getBatchCreatedAt())
                .tasks(tasks)
                .build();
    }
}
