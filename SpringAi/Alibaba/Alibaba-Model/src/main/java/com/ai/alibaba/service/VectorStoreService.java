package com.ai.alibaba.service;

import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.web.multipart.MultipartFile;

public interface VectorStoreService {

    /**
     * 判断是否将文件保存到向量存储
     *
     * @param file 上传的文件
     * @param indexName 向量索引名称
     * @return 是否保存到远程向量存储
     */
    boolean saveFileToVectorStore(MultipartFile file, String indexName);

    /**
     * 创建文档检索器
     *
     * @param indexName 向量索引名称
     * @return 文档检索器
     */
    DocumentRetriever createDocumentRetriever(String indexName);

}
