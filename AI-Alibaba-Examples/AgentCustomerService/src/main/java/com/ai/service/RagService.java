package com.ai.service;

import org.springframework.ai.document.Document;

import java.io.File;
import java.util.List;

public interface RagService {

    /**
     * 插入文本内容到知识库
     * @param content
     * @return
     */
    boolean insertContent(String content);

    /**
     * 上传文件到知识库
     * @param file
     * @return
     */
    boolean uploadFile(File file);

    /**
     * 搜索知识库中的相关文档
     * @param query 查询字符串
     * @param topK 返回的相似文档数量
     * @return
     */
    List<Document> searchDocuments(String query, int topK);

}
