package com.knowledge.agent.rag.config;

import io.milvus.common.clientenum.FunctionType;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Configuration
public class MilvusConfig {

    @Value("${milvus.cloud.url}")
    private String URI;

    @Value("${milvus.cloud.collection-name}")
    private String COLLECTION_NAME;

    @Value("${milvus.cloud.token}")
    private String TOKEN;

    @Value("${milvus.cloud.dimension:1024}")
    private Integer DIMENSION;

    @Bean
    public MilvusClientV2 milvusClientV2() {
        // 1. 创建连接
        MilvusClientV2 milvusClient = new MilvusClientV2(
                ConnectConfig.builder()
                        .uri(URI)
                        .token(TOKEN)
                        .secure(false)
                        .connectTimeoutMs(5000L)
                        .build()
        );
        // 2. 检查并创建 Schema (为了支持未来的优化和高性能过滤，我们手动定义 Schema)
        initCollection(milvusClient);

        return milvusClient;
    }

    private void initCollection(MilvusClientV2 client) {
        // 检验是否已初始化milvus库
        if (client.hasCollection(HasCollectionReq.builder().collectionName(COLLECTION_NAME).build())) {
            return;
        }
        // --- 设计 Milvus 数据库结构 ---
        CreateCollectionReq.CollectionSchema schema = MilvusClientV2.CreateSchema();

        // 1. ID 字段 (Primary Key)
        schema.addField(AddFieldReq.builder()
                .autoID(true)
                .dataType(DataType.Int64)
                .isPrimaryKey(true)
                .fieldName("id")
                .description("主键ID，切片id")
                .build());
        // 2. text 字段
        schema.addField(AddFieldReq.builder()
                .dataType(DataType.VarChar)
                .fieldName("text")
                .description("知识文本内容")
                .enableAnalyzer(true)
                .build());
        // 3. Metadata Field (JSON)
        // 存储其他动态属性：title, source, tags, create_time 等
        // Milvus 2.4+ 支持 JSON 类型，LangChain4j 会自动将未匹配到独立列的 metadata 存入此 JSON
        schema.addField(AddFieldReq.builder()
                .dataType(DataType.JSON)
                .fieldName("metadata")
                .description("扩展信息")
                .build());
        // 4. text_dense Field
        schema.addField(AddFieldReq.builder()
                .dataType(DataType.FloatVector)
                .dimension(DIMENSION)
                .fieldName("text_dense")
                .description("文本稠密向量")
                .build());
        // 5.text_sparse Field 稀疏向量
        schema.addField(AddFieldReq.builder()
                .dataType(DataType.SparseFloatVector)
                .dimension(DIMENSION)
                .fieldName("text_sparse")
                .description("文本稀疏向量")
                .build());
        // 6.为text_sparse添加BM25函数，支持稀疏向量搜索
        schema.addFunction(CreateCollectionReq.Function.builder()
                .functionType(FunctionType.BM25).name("fun_bm25_emb")
                .inputFieldNames(Collections.singletonList("text"))
                .outputFieldNames(Collections.singletonList("text_sparse"))
                .build());

        // --- 创建索引 ---
        // 向量索引
        IndexParam idxForTextDense = IndexParam.builder()
                .fieldName("text_dense")
                .indexName("idx_text_dense")
                .indexType(IndexParam.IndexType.HNSW)
                .metricType(IndexParam.MetricType.IP)
                .build();
        IndexParam idxForTextSparse = IndexParam.builder()
                .fieldName("text_sparse")
                .indexName("idx_text_sparse")
                .indexType(IndexParam.IndexType.SPARSE_INVERTED_INDEX)
                .metricType(IndexParam.MetricType.BM25)
                .extraParams(Map.of("inverted_index_algo", "DAAT_MAXSCORE")) // 设置倒排检索算法
                .build();

        CreateCollectionReq createCollectionReq = CreateCollectionReq.builder()
                .collectionSchema(schema)
                .collectionName(COLLECTION_NAME)
                .description("Knowledge Agent RAG Collection")
                .consistencyLevel(ConsistencyLevel.SESSION)
                .indexParams(List.of(idxForTextDense, idxForTextSparse))
                .numShards(1)
                .build();

        // 创建 Collection
        client.createCollection(createCollectionReq);
        System.out.println("Collection " + COLLECTION_NAME + " created successfully!");
    }
}

