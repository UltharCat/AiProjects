package com.ai.tools;

import com.ai.rag.dto.RagDocDto;
import com.ai.rag.service.RagService;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class RagTools {

    private final RagService ragService;

    public RagTools(RagService ragService) {
        this.ragService = ragService;
    }

    @Tool(description = """
            【RAG-写入】插入一段文本到本地向量库（知识库）。

            用途：把一段文本进行向量化并入库，便于后续相似度检索。
            入参：RagDocAddRequest（仅使用 content，可选 documentNumber 作为业务关联）。
            返回：true=成功，false=失败。
            """)
    boolean insertContent(@ToolParam(description = """
            插入文本请求。
            - content(必填)：要入库的文本
            - documentNumber(可选)：业务文档编号，用于将文本与某个文档归档/关联
            """) RagDocDto dto) {
        return ragService.insertContent(dto);
    }

    @Tool(description = """
            【RAG-检索】根据查询文本在本地向量库中做相似度搜索，返回最相关的文档。

            入参：
            - content：查询文本（会被向量化后用于检索）
            - topK：返回的最多文档数
            返回：Map<类别, 文档列表>；类别来自向量库/文档元数据的分组逻辑。
            """)
    Map<String, List<Document>> searchDocuments(
            @ToolParam(description = "查询文本（将用于向量检索）") String content,
            @ToolParam(description = "返回条数上限（topK）") int topK) {
        return ragService.searchDocuments(content, topK);
    }

    @Tool(description = """
            【RAG-删除】按业务文档编号删除向量库中的相关内容。

            入参：documentNumber（业务文档编号）。
            返回：true=成功，false=失败。
            """)
    boolean deleteDocumentByNumber(@ToolParam(description = "业务文档编号（documentNumber）") String documentNumber) {
        return ragService.deleteDocumentByNumber(documentNumber);
    }

}
