package com.knowledge.agent.api.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * RAG 文档切片传输对象
 * 用于在 RAG 服务内部流转，或者存入向量数据库的数据结构
 */
@Data
@Builder
public class RagDocumentDTO implements Serializable {

    /**
     * 全局唯一ID (建议使用 Snowflake 算法生成 Long 类型)
     * 作为 Milvus 的 Primary Key，以及未来 ES/Neo4j 的关联键
     */
    private Long id;

    /**
     * 用户ID / 所有者ID (用于权限隔离过滤)
     */
    private Long userId;

    /**
     * 原始知识点ID (关联回 MySQL 中的主知识记录)
     */
    private Long knowledgeId;

    /**
     * 文本内容 (切片后的文本)
     */
    private String content;

    /**
     * 向量数据 (由 Embedding 模型生成)
     */
    private List<Float> vector;

    /**
     * 动态元数据 (存储 title, source, tags, summary 等)
     * 使用 Map 结构，映射到 Milvus 的 JSON 字段
     */
    private Map<String, Object> metadata;
}

