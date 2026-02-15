package com.knowledge.agent.rag.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.knowledge.agent.api.dto.KnowledgeDTO;
import com.knowledge.agent.api.service.RagService;
import com.knowledge.agent.common.resp.Result;
import com.knowledge.agent.common.utils.EbbinghausUtils;
import com.knowledge.agent.rag.config.MilvusHybridRetriever;
import com.knowledge.agent.rag.entity.KnowledgeCard;
import com.knowledge.agent.rag.mapper.KnowledgeCardMapper;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@DubboService
public class RagServiceImpl implements RagService {

    @Value("${milvus.cloud.collection-name}")
    private String COLLECTION_NAME;

    private final EmbeddingModel embeddingModel;

    private final MilvusClientV2 milvusClientV2;

    private final KnowledgeCardMapper knowledgeCardMapper;

    public RagServiceImpl(EmbeddingModel embeddingModel,
                          MilvusClientV2 milvusClientV2,
                          KnowledgeCardMapper knowledgeCardMapper) {
        this.embeddingModel = embeddingModel;
        this.milvusClientV2 = milvusClientV2;
        this.knowledgeCardMapper = knowledgeCardMapper;
    }

    @Override
    @Transactional
    public Result<Boolean> saveKnowledge(KnowledgeDTO dto) {
        // 1.参数校验
        if (dto == null || StrUtil.isBlank(dto.getSummary())) {
            return Result.error(400, "无效的知识数据");
        }
        // 已有知识检索
        List<Content> retrieve = new MilvusHybridRetriever(COLLECTION_NAME, embeddingModel, milvusClientV2)
                .retrieve(Query.from(dto.getSummary()));
        if (CollUtil.isNotEmpty(retrieve)) {
            // 检索到相关知识，则将本次总结内容添加入相关知识文档下
            Set<Long> docIds = retrieve.stream().map(content-> content.textSegment().metadata().getLong("doc_id")).collect(Collectors.toSet());
            docIds.forEach(id -> {
                dto.setId(id);
                saveMilvusKnowledge(dto);
            });
        } else {
            // 生成文档id
            dto.setId(IdUtil.getSnowflakeNextId());
            // 保存milvus新知识
            saveMilvusKnowledge(dto);
            // 保存mysql复习记录
            knowledgeCardMapper.insert(
                    KnowledgeCard.builder()
                            .docId(dto.getId())
                            .build()
            );
        }
        return Result.success(Boolean.TRUE);
    }

    /**
     * 保存知识到Milvus
     * @param dto
     */
    private void saveMilvusKnowledge(KnowledgeDTO dto) {
        var metadata = Map.of(
                "doc_id", dto.getId(),
                "tags", JSON.toJSONString(dto.getTags())
        );
        // 文本切割
        Document knowledgeDoc = Document.from(
                dto.getSummary(),
                Metadata.from(metadata)
        );
        // 文本向量化
        List<TextSegment> split = DocumentSplitters
                .recursive(1000, 100) // 递归切割器，切割后文本长度不超过1000，重叠部分100
                .split(knowledgeDoc);
        // 构造向量存储数据结构
        var rows = split.stream().map(textSegment -> {
            String text = textSegment.text();
            float[] vector = embeddingModel.embed(text).content().vector();
            JsonObject row = new JsonObject();
            row.addProperty("text", text);
            row.add("text_dense", new Gson().toJsonTree(vector));
            row.addProperty("metadata", JSON.toJSONString(metadata));
            return row;
        }).collect(Collectors.toList());
        // milvus向量存储
        milvusClientV2.insert(InsertReq.builder().collectionName(COLLECTION_NAME).data(rows).build());
    }

    @Override
    @Transactional
    public Result<Void> updateReviewStatus(Long id, int quality) {
        // 查询已有知识卡片
        KnowledgeCard card = knowledgeCardMapper.selectOne(new LambdaQueryWrapper<KnowledgeCard>()
                .eq(KnowledgeCard::getDocId, id));
        // 计算艾宾浩斯参数
        EbbinghausUtils.ReviewResult compute = EbbinghausUtils.compute(quality, card.getEasinessFactor(), card.getIntervalDays(), card.getRepetition(), LocalDate.now());
        // 更新复习状态
        knowledgeCardMapper.updateById(
                KnowledgeCard.builder()
                        .docId(id)
                        .easinessFactor(compute.newEf())
                        .intervalDays(compute.newInterval())
                        .nextReviewDate(compute.nextReviewDate().atStartOfDay())
                        .build()
        );
        return Result.success(null);
    }
}
