package com.ai.rag.service.impl;

import com.ai.rag.service.VectorStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

@Service
@Slf4j
public class VectorStoreServiceImpl implements VectorStoreService {

    private final VectorStore vectorStore;

    public VectorStoreServiceImpl(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    @Transactional(transactionManager = "pgTransactionManager")
    public void addDocsToVectorStore(List<Document> docs) {
        vectorStore.add(docs);
    }

    @Override
    @Transactional(readOnly = true, transactionManager = "pgTransactionManager")
    public List<Document> searchSimilarDocs(String content, int topK) {
        Assert.hasText(content, "content is blank");
        Assert.isTrue(topK > 0, "topK must be > 0");

        // 构建查询请求
        SearchRequest searchRequest = SearchRequest.builder()
                .query(content)
                .topK(topK)
                .build();
        return vectorStore.similaritySearch(searchRequest);
    }

    @Override
    @Transactional(transactionManager = "pgTransactionManager")
    public void delDocsByExpression(Filter.Expression expression) {
        vectorStore.delete(expression);
    }
}
