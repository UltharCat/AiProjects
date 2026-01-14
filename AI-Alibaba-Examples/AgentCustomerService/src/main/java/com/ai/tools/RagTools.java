package com.ai.tools;

import com.ai.rag.request.RagDocAddRequest;
import com.ai.rag.service.RagService;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class RagTools {

    private final RagService ragService;

    public RagTools(RagService ragService) {
        this.ragService = ragService;
    }

    @Tool(description = """
            该方法向本地向量库中插入文本内容
            输入参数为 RagDocAddRequest 对象
            该方法会将提供的文本内容进行向量化处理，并存储到本地向量库中，以便后续的检索和查询操作。
            该方法适用于需要动态添加文本内容到向量库的场景，如用户生成内容、文档管理等。
            """)
    boolean insertContent(@ToolParam(description = """
            插入文本请求参数，类型为 RagDocAddRequest 对象，包含以下字段：
            - content: 要插入的文本内容，类型为 String
            - documentNumber: 文档业务编号，类型为 String，可选输入，若有则将文本内容关联到该编号对应的文档
            """) RagDocAddRequest request) {
        return ragService.insertContent(request);
    }

    @Tool(description = """
            该方法用于将上传的文件内容添加到本地向量库中。
            输入参数为 RagDocAddRequest 对象
            该方法返回一个布尔值，表示文件上传和内容添加操作是否成功。
            该方法会读取上传的文件内容，进行向量化处理，并将生成的向量存储到本地向量库中，以便后续的检索和查询操作。
            该方法适用于需要将文件内容动态添加到向量库的场景，如文档管理、知识库构建等。
            """)
    boolean uploadFile(@ToolParam(description = """
            上传文档请求参数，类型为 RagDocAddRequest 对象，包含以下字段：
            - file: 要上传的文件，类型为 MultipartFile
            - documentNumber: 文档业务编号，类型为 String，可选输入，若有则将文本内容关联到该编号对应的文档
            """) RagDocAddRequest request) throws IOException {
        return ragService.uploadFile(request);
    }

    @Tool(description = """
            该方法用于在本地向量库中搜索与给定内容相关的文档。
            输入参数包括要搜索的内容和返回的文档数量限制。
            该方法返回一个包含搜索结果的映射，映射的键为文档类别，值为对应类别下的文档列表。
            该方法会将输入的内容进行向量化处理，并在本地向量库中进行相似度搜索，找出与输入内容最相关的文档。
            该方法适用于需要从本地向量库中检索相关文档的场景，如知识库查询、文档检索等。
            """)
    Map<String, List<Document>> searchDocuments(@ToolParam(description = "查询请求文档内容，类型为String") String content,
                                                @ToolParam(description = "查询请求文档限制，类型为int，限制返回的文档数量") int topK) {
        return ragService.searchDocuments(content, topK);
    }

    @Tool(description = """
            该方法用于根据文档业务编号删除本地向量库中的文档。
            输入参数为文档业务编号。
            该方法返回一个布尔值，表示删除操作是否成功。
            该方法会在本地向量库中查找与提供的文档业务编号相关联的文档，并将其删除。
            该方法适用于需要根据特定标识符删除文档的场景，如文档管理、数据清理等。
            """)
    boolean deleteDocumentByNumber(@ToolParam(description = "文档业务编号，类型为String") String documentNumber) {
        return ragService.deleteDocumentByNumber(documentNumber);
    }

}
