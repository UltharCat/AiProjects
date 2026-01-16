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
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
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

    private static final String META_DOCUMENT_NUMBER = "documentNumber";

    private static final String META_FILE_NAME = "fileName";

    private final RagDocumentRepository repo;

    private final VectorStore vectorStore;

    public RagServiceImpl(RagDocumentRepository repo, VectorStore vectorStore) {
        this.repo = repo;
        this.vectorStore = vectorStore;
    }

    @Override
    @Transactional
    public boolean insertContent(RagDocAddRequest request) {
        Assert.notNull(request, "request is null");
        Assert.hasText(request.getContent(), "content is blank");

        var docNum = StringUtil.isNotBlank(request.getDocumentNumber()) ? request.getDocumentNumber() : UUID.randomUUID().toString();

        // 查询本地库内是否已经存在数据（优先用业务键查询；Example 方式保留兼容，但不再依赖）
        TRagDocument entity = repo.findByDocumentNumber(docNum).orElse(null);

        var fileName = Objects.nonNull(entity) ? entity.getFileName() : "content-insert-" + UUID.randomUUID();

        // 构建metadata
        Map<String, Object> metadata = Map.of(
                META_DOCUMENT_NUMBER, docNum,
                META_FILE_NAME, fileName
        );

        // 拆分文本内容
        List<Document> splitDocs = TokenTextSplitter.builder()
                .build()
                .apply(List.of(Document.builder().text(request.getContent()).build()));

        splitDocs.forEach(d -> d.getMetadata().putAll(metadata));

        // 文档插入向量库
        this.addDocsToVectorStore(splitDocs);

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
        Assert.notNull(request, "request is null");
        var file = request.getFile();
        Assert.notNull(file, "file is null");
        Assert.isTrue(!file.isEmpty(), "file is empty");

        var fileName = file.getOriginalFilename();
        Assert.hasText(fileName, "fileName is blank");

        // 构建临时文件：Windows 下 suffix 更安全；originalFilename 可能包含路径分隔符，这里做最小处理
        String safeSuffix = ".tmp";
        int idx = fileName.lastIndexOf('.');
        if (idx >= 0 && idx < fileName.length() - 1) {
            safeSuffix = fileName.substring(idx);
        }

        Path tempFile = Files.createTempFile("upload-", safeSuffix);

        try {
            // copy 输入流（让底层自动关闭）
            try (var in = file.getInputStream()) {
                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            List<Document> docs;
            // 拆分文档
            if (fileName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
                PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(tempFile.toUri().toString());
                docs = pdfReader.get();
            } else {
                TikaDocumentReader tikaReader = new TikaDocumentReader(tempFile.toUri().toString());
                docs = tikaReader.get();
            }

            // 按句拆分文档
            var splitDocs = TokenTextSplitter.builder().build().apply(docs);

            // 设置文档metadata
            var docNum = StringUtil.isNotBlank(request.getDocumentNumber()) ? request.getDocumentNumber() : UUID.randomUUID().toString();
            Map<String, Object> metadata = Map.of(
                    META_DOCUMENT_NUMBER, docNum,
                    META_FILE_NAME, fileName
            );

            splitDocs.forEach(d -> d.getMetadata().putAll(metadata));

            // 文档插入向量库
            this.addDocsToVectorStore(splitDocs);

            // 记录文档数据（若同 documentNumber 已存在则只更新时间/文件名）
            repo.findByDocumentNumber(docNum).ifPresentOrElse(existing -> existing.setModifyTime(Instant.now()),
                    () -> repo.save(TRagDocument.builder()
                    .documentNumber(docNum)
                    .fileName(fileName)
                    .build()));

            return true;
        } finally {
            // 删除临时文件（确保异常时也不会泄漏）
            Files.deleteIfExists(tempFile);
        }
    }

    @Transactional(transactionManager = "pgTransactionManager")
    public void addDocsToVectorStore(List<Document> docs) {
        vectorStore.add(docs);
    }

    @Override
    public Map<String, List<Document>> searchDocuments(String content, int topK) {
        Assert.hasText(content, "content is blank");
        Assert.isTrue(topK > 0, "topK must be > 0");

        // 构建查询请求
        SearchRequest searchRequest = SearchRequest.builder()
                .query(content)
                .topK(topK)
                .build();

        List<Document> docs = vectorStore.similaritySearch(searchRequest);

        // fileName -> docs
        Map<String, List<Document>> docMap = new HashMap<>();
        for (Document d : docs) {
            Object fileNameObj = d.getMetadata().get(META_FILE_NAME);
            String key = (fileNameObj == null) ? "NoFileName" : String.valueOf(fileNameObj);
            docMap.computeIfAbsent(key, k -> new ArrayList<>()).add(d);
        }
        return docMap;
    }

    @Override
    @Transactional
    public boolean deleteDocumentByNumber(String documentNumber) {
        Assert.hasText(documentNumber, "documentNumber is blank");

        // 删除本地向量库记录
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        var expression = b.eq(META_DOCUMENT_NUMBER, documentNumber).build();
        this.delDocsFromVectorStore(expression);

        // 删除本地持久化记录（按业务键删除）
        return repo.deleteByDocumentNumber(documentNumber);
    }

    @Transactional(transactionManager = "pgTransactionManager")
    public void delDocsFromVectorStore(Filter.Expression expression) {
        vectorStore.delete(expression);
    }

}
