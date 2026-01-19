package com.ai.rag.service;

import com.ai.rag.dto.RagDocDto;
import org.springframework.ai.document.Document;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface RagService {

    /**
     * 插入文本内容到知识库
     * @param dto
     * @return
     */
    boolean insertContent(RagDocDto dto);

    /**
     * 上传文件到知识库
     * @param dto
     * @return
     */
    boolean uploadFile(RagDocDto dto) throws IOException;

    /**
     * 搜索知识库中的相关文档
     * @param content 查询字符串
     * @param topK 返回的相似文档数量
     * @return
     */
    Map<String, List<Document>> searchDocuments(String content, int topK);

    /**
     * 根据文档编号删除知识库中的文档
     * @param documentNumber 文档编号
     * @return
     */
    boolean deleteDocumentByNumber(String documentNumber);

}
