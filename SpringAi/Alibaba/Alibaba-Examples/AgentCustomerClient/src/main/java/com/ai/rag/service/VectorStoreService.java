package com.ai.rag.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.filter.Filter;

import java.util.List;

public interface VectorStoreService {

    /**
     * 将文档列表添加到向量存储中。
     *
     * @param docs 要添加的文档列表
     */
    void addDocsToVectorStore(List<Document> docs);

    /**
     * 根据搜索请求在向量存储中搜索相似文档。
     *
     * @param content 搜索内容
     * @param topK  返回的相似文档数量
     * @return 相似文档列表
     */
    List<Document> searchSimilarDocs(String content, int topK);

    /**
     * 根据文档编号删除向量存储中的文档。
     *
     * @param expression 过滤表达式
     */
    void delDocsByExpression(Filter.Expression expression);

}
