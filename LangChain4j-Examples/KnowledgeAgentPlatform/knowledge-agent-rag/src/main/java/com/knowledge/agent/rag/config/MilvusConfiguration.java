package com.knowledge.agent.rag.config;

import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import io.milvus.client.MilvusServiceClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.grpc.DataType;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.index.CreateIndexParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class MilvusConfiguration {

    @Value("${milvus.cloud.url}")
    private String URL;

    @Value("${milvus.cloud.collection-name}")
    private String COLLECTION_NAME;

    @Value("${milvus.cloud.token}")
    private String TOKEN;

    @Value("${milvus.cloud.dimension:1024}")
    private Integer DIMENSION;

    @Bean
    public MilvusServiceClient milvusServiceClient() {
        return new MilvusServiceClient(
                ConnectParam.newBuilder()
                        .withUri(URL)
                        .withToken(TOKEN)
                        .build()
        );
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(MilvusServiceClient milvusClient) {
        // 1. 检查并创建 Schema (为了支持未来的优化和高性能过滤，我们手动定义 Schema)
        initCollection(milvusClient);

        // 2. 返回 LangChain4j 的 Store 实现
        return MilvusEmbeddingStore.builder()
                .uri(URL)
                .token(TOKEN)
                .collectionName(COLLECTION_NAME)
                .dimension(DIMENSION)
                .indexType(IndexType.HNSW)
                .metricType(MetricType.COSINE)
                // 能够自动映射 TextSegment 的 metadata key 到 Milvus 的 field
                // 我们在 Schema 中定义了 json 类型的 metadata 字段，以及 user_id, knowledge_id 标量
                .retrieveEmbeddingsOnSearch(true)
                .build();
    }

    private void initCollection(MilvusServiceClient client) {
        boolean hasCollection = client.hasCollection(
                HasCollectionParam.newBuilder().withCollectionName(COLLECTION_NAME).build()
        ).getData();

        if (hasCollection) {
            return;
        }

        // --- 设计 Milvus 数据库结构 ---

        // 1. ID 字段 (Primary Key)
        FieldType idField = FieldType.newBuilder()
                .withName("id")
                .withDataType(DataType.Int64)
                .withPrimaryKey(true)
                .withAutoID(false) // 建议手动生成ID (如Snowflake)，方便与 ES/MySQL/Neo4j 统一关联
                .withDescription("Global Unique ID (Snowflake)")
                .build();

        // 2. User ID (Partition Key / Filtering Field)
        // 这是一个高频过滤字段，作为独立标量列存储，性能优于 JSON 包含
        FieldType userIdField = FieldType.newBuilder()
                .withName("user_id") // LangChain4j metadata key "user_id" 将映射到此字段
                .withDataType(DataType.Int64)
                .withDescription("Owner User ID")
                .build();

        // 3. Knowledge ID (Linking Field)
        // 关联到 MySQL 或 Neo4j 的知识 ID
        FieldType knowledgeIdField = FieldType.newBuilder()
                .withName("knowledge_id")
                .withDataType(DataType.Int64)
                .withDescription("Original Knowledge ID")
                .build();

        // 4. Vector Field
        FieldType vectorField = FieldType.newBuilder()
                .withName("vector")
                .withDataType(DataType.FloatVector)
                .withDimension(DIMENSION)
                .withDescription("Embedding Vector")
                .build();

        // 5. Content Field (VarChar)
        // 存储切片文本，直接检索出结果，避免回查 ES 或 MySQL (当前阶段优化)
        FieldType textField = FieldType.newBuilder()
                .withName("text")
                .withDataType(DataType.VarChar)
                .withMaxLength(60000) // 根据实际需要调整
                .withDescription("Segment Content")
                .build();

        // 6. Metadata Field (JSON)
        // 存储其他动态属性：title, source, tags, create_time 等
        // Milvus 2.4+ 支持 JSON 类型，LangChain4j 会自动将未匹配到独立列的 metadata 存入此 JSON
        FieldType metadataField = FieldType.newBuilder()
                .withName("metadata")
                .withDataType(DataType.JSON)
                .withDescription("Dynamic Metadata (title, source, etc.)")
                .build();

        CreateCollectionParam createParam = CreateCollectionParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withDescription("Knowledge Agent RAG Collection")
                .withConsistencyLevel(ConsistencyLevelEnum.STRONG)
                .addFieldType(idField)
                .addFieldType(userIdField)
                .addFieldType(knowledgeIdField)
                .addFieldType(textField)
                .addFieldType(metadataField)
                .addFieldType(vectorField)
                // .withEnableDynamicField(true) // 可选：启用动态字段
                .build();

        client.createCollection(createParam);

        // --- 创建索引 ---

        // 向量索引
        client.createIndex(CreateIndexParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withFieldName("vector")
                .withIndexType(IndexType.IVF_FLAT) // 生产环境推荐 HNSW
                .withMetricType(MetricType.COSINE)
                .withExtraParam("{\"nlist\":1024}")
                .build());

        // 标量索引 (加速 user_id 过滤)
        client.createIndex(CreateIndexParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withFieldName("user_id") // Scalar Index
                .withIndexType(IndexType.STL_SORT) // Milvus auto chooses suitable index for scalar
                .build());

        // 标量索引 (加速 knowledge_id 过滤)
        client.createIndex(CreateIndexParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withFieldName("knowledge_id")
                .build());
    }
}

