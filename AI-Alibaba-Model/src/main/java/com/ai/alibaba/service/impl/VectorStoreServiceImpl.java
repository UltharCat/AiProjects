package com.ai.alibaba.service.impl;

import com.ai.alibaba.service.VectorStoreService;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
@Slf4j
public class VectorStoreServiceImpl implements VectorStoreService {

    private final ApplicationContext context;

    private final String type;

    private final DashScopeApi dashScopeApi;

    public VectorStoreServiceImpl(ApplicationContext context,
                                  @Value("${x-project.app.chat.vector-store.type:local}") String type,
                                  @Value("${spring.ai.dashscope.api-key}") String dashScopeApiKey
    ) {
        this.context = context;
        this.type = type;
        this.dashScopeApi = DashScopeApi.builder().apiKey(dashScopeApiKey).build();
    }

    /**
     * 根据索引名称获取对应的 VectorStore 实例
     */
    private VectorStore getVectorStore(String indexName) {
        if (StringUtils.isNotBlank(indexName) && "remote".equals(type)) {
            return new DashScopeCloudStore(dashScopeApi, new DashScopeStoreOptions(indexName));
        }
        // 默认使用LocalVectorStore，本地配置使用的是PgVectorStore，默认embedding使用自动注入的EmbeddingModel，本项目中的embedding模型为DashScopeEmbeddingModel
        return context.getBean(VectorStore.class);
    }

    @Override
    public boolean saveFileToVectorStore(MultipartFile file, String indexName) {
        var vectorStore = this.getVectorStore(indexName);

        if (StringUtils.isNotBlank(indexName) && "remote".equals(type)) {
            // 使用临时文件处理，通过 DashScopeCloudStore 保存文件到远程向量存储
            Path tempFile = null;
            try {
                // 将 MultipartFile 转换为临时文件 Path
                tempFile = Files.createTempFile("upload-", file.getOriginalFilename());
                // 注册一个钩子，以便在JVM退出时删除文件
                tempFile.toFile().deleteOnExit();

                Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

                // 使用临时文件 Path 创建 DashScopeDocumentCloudReader
                DocumentReader documentReader = new DashScopeDocumentCloudReader(
                        tempFile.toAbsolutePath().toString(), dashScopeApi, null);
                List<Document> documents = documentReader.get();

                // 获取 VectorStore 实例并添加文档
                vectorStore.add(documents);

                return true;
            } catch (IOException e) {
                // 在实际应用中，这里应该记录日志
                log.error("上传文件 {} 到云端失败:", file.getOriginalFilename(), e);
                return false;
            }
            // 由于 DashScopeDocumentCloudReader 不释放文件句柄，我们无法在 finally 中立即删除文件。
            // 通过 deleteOnExit()，我们将清理工作交给了JVM。
        }
        // 保存到本地向量存储
        try (InputStream inputStream = file.getInputStream()) {
            Tika tika = new Tika();
            var content = tika.parseToString(inputStream);
            // 进行文段切分，spring-ai只内置了TokenTextSplitter，仅能做到文段的简单分割
            List<Document> documents = new TokenTextSplitter().apply(List.of(new Document(content)));
            vectorStore.add(documents);
            log.info("文件 {} 已成功处理并添加到本地向量存储。", file.getOriginalFilename());
            return true;
        } catch (IOException | TikaException e) {
            log.error("上传文件 {} 到本地失败:", file.getOriginalFilename(), e);
            return false;
        }
    }

    @Override
    public DocumentRetriever createDocumentRetriever(String indexName) {
        if (StringUtils.isNotBlank(indexName) && "remote".equals(type)) {
            return new DashScopeDocumentRetriever(dashScopeApi,
                    DashScopeDocumentRetrieverOptions.builder().withIndexName(indexName).build());
        } else {
            return VectorStoreDocumentRetriever.builder()
                    .vectorStore(this.getVectorStore(indexName))
                    .similarityThreshold(0.4)
                    .topK(10)
                    .build();
        }
    }

}
