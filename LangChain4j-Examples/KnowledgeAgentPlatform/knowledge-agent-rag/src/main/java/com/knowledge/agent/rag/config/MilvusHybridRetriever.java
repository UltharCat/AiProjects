package com.knowledge.agent.rag.config;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.AnnSearchReq;
import io.milvus.v2.service.vector.request.HybridSearchReq;
import io.milvus.v2.service.vector.request.data.BaseVector;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.request.ranker.RRFRanker;
import io.milvus.v2.service.vector.response.SearchResp;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MilvusHybridRetriever implements ContentRetriever {

    private final String COLLECTION_NAME;

    private final EmbeddingModel embeddingModel;

    private final MilvusClientV2 milvusClientV2;

    public MilvusHybridRetriever(String collectionName,
                                 EmbeddingModel embeddingModel,
                                 MilvusClientV2 milvusClientV2) {
        this.COLLECTION_NAME = collectionName;
        this.embeddingModel = embeddingModel;
        this.milvusClientV2 = milvusClientV2;
    }

    @Override
    public List<Content> retrieve(Query query) {
        List<BaseVector> queryVec = Collections.singletonList(
                new FloatVec(this.embeddingModel.embed(query.text()).content().vector()));

        HybridSearchReq searchReq = HybridSearchReq.builder()
                .collectionName(COLLECTION_NAME)
                .searchRequests(List.of(
                        AnnSearchReq.builder()
                                .vectorFieldName("text_dense")
                                .vectors(queryVec)
                                .params("{\"ef\": 10}") // 密集向量参数，控制密集向量的搜索效果和效率，ef值越大搜索结果越准确但速度越慢
                                .topK(2)
                                .build(),
                        AnnSearchReq.builder()
                                .vectorFieldName("text_sparse")
                                .vectors(queryVec)
                                .params("{\"drop_ratio_search\": 0.2}") // 稀疏向量参数，控制稀疏向量的搜索结果占比，0.2表示舍弃20%的稀疏向量搜索结果，保留80%的密集向量搜索结果
                                .topK(2)
                                .build()
                ))
                .ranker(new RRFRanker(60))
                .outFields(List.of("text", "metadata"))
                .build();
        // 因为只做了一次查询，所以直接获取第一个搜索结果列表
        List<SearchResp.SearchResult> results = this.milvusClientV2.hybridSearch(searchReq).getSearchResults().getFirst();
        return results.stream()
                .<Content>mapMulti((r, consumer) -> {
                    try {
                        Map<String, Object> entity = r.getEntity();
                        if (entity == null) return;
                        Object txt = entity.get("text");
                        if (txt == null) return;
                        String text = (String) txt;
                        Map<String, ?> metadata = (Map<String, ?>) entity.get("metadata");
                        if (metadata == null) return;
                        consumer.accept(Content.from(TextSegment.from(text, Metadata.from(metadata))));
                    } catch (Exception ignored) {
                        // 出错则不输出该项
                    }
                })
                .collect(Collectors.toList());
    }
}
