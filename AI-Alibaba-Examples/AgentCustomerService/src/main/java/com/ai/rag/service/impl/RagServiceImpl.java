package com.ai.rag.service.impl;

import com.ai.rag.domain.TRagDocument;
import com.ai.rag.repo.RagDocumentRepository;
import com.ai.rag.request.RagDocAddRequest;
import com.ai.rag.service.RagService;
import jodd.util.StringUtil;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;

@Service
public class RagServiceImpl implements RagService {

    private final RagDocumentRepository repo;

    private final VectorStore vectorStore;

    public RagServiceImpl(RagDocumentRepository repo, VectorStore vectorStore) {
        this.repo = repo;
        this.vectorStore = vectorStore;
    }

    @Override
    @Transactional
    public boolean insertContent(RagDocAddRequest request) {
        var docNum = StringUtil.isNotBlank(request.getDocumentNumber()) ? request.getDocumentNumber() : UUID.randomUUID().toString();
        // 查询本地库内是否已经存在数据
        TRagDocument entity = repo.findOne(Example.of(TRagDocument.builder()
                .documentNumber(docNum)
                .build())
        ).orElse(null);
        var fileName = Objects.nonNull(entity) ? entity.getFileName() : "content-insert-" + UUID.randomUUID();
        // 构建metadata
        var metadata = Map.of("documentNumber", docNum,
                "fileName", fileName);
        // 拆分文本内容
        List<Document> splitDocs = TokenTextSplitter.builder()
                .build()
                .apply(
                        List.of(Document.builder()
                                .text(request.getContent())
                                .build())
                );
        splitDocs.forEach(d -> d.getMetadata().putAll(metadata));
        // 文档插入向量库
        vectorStore.add(splitDocs);
        // 如果存在托管实体，修改持久化字段以触发脏检测和 @PreUpdate
        if (entity != null) {
            entity.setModifyTime(Instant.now());
        } else {
            repo.save(TRagDocument.builder()
                    .documentNumber(docNum)
                    .fileName(fileName)
                    .build());
        }
        return true;
    }

    @Override
    @Transactional
    public boolean uploadFile(RagDocAddRequest request) throws IOException {
        var file = request.getFile();
        // 构建临时文件
        Path tempFile = Files.createTempFile("upload-", file.getOriginalFilename());
        Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

        List<Document> docs;
        var fileName = file.getOriginalFilename();
        Assert.notNull(fileName, "fileName is null");
        // 拆分文档
        if (fileName.toLowerCase().endsWith("pdf")) {
            // PDF文件处理逻辑
            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(tempFile.toUri().toString());
            docs = pdfReader.get();
        } else {
            // 其他文件处理逻辑
            TikaDocumentReader tikaReader = new TikaDocumentReader(tempFile.toUri().toString());
            docs = tikaReader.get();
        }
        // 按句拆分文档
        var splitDocs = TokenTextSplitter.builder().build().apply(docs);
        // 设置文档metadata
        var docNum = StringUtil.isNotBlank(request.getDocumentNumber()) ? request.getDocumentNumber() : UUID.randomUUID().toString();
        var fileMetadata = Map.of("documentNumber", docNum,
                "fileName", fileName);
        splitDocs.forEach(d -> d.getMetadata().putAll(fileMetadata));
        // 文档插入向量库
        vectorStore.add(splitDocs);
        // 记录文档数据
        repo.save(TRagDocument.builder()
                .documentNumber(docNum)
                .fileName(fileName)
                .build());
        // 删除临时文件
        Files.deleteIfExists(tempFile);
        return true;
    }

    @Override
    public Map<String, List<Document>> searchDocuments(String content, int topK) {
        // 构建查询请求
        SearchRequest searchRequest = SearchRequest.builder()
                .query(content)
                .topK(topK)
                .build();
        List<Document> docs = vectorStore.similaritySearch(searchRequest);
        // fileName,List<Document> map
        Map<String, List<Document>> docMap = new HashMap<>();
        docs.forEach(d->{
            docMap.compute(d.getMetadata().get("fileName").toString(), (k, v) -> {
                if (Objects.isNull(v)) {
                    return Collections.singletonList(d);
                } else {
                    v.add(d);
                    return v;
                }
            });
        });
        return docMap;
    }

    @Override
    public boolean deleteDocumentByNumber(String documentNumber) {
        // 删除本地向量库记录
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        var expression = b.eq("documentNumber", documentNumber).build();
        vectorStore.delete(expression);
        // 删除本地持久化记录
        repo.delete(TRagDocument.builder()
                .documentNumber(documentNumber)
                .build());
        return true;
    }

}
