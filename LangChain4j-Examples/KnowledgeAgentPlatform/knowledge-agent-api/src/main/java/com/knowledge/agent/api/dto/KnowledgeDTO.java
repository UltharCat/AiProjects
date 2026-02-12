package com.knowledge.agent.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class KnowledgeDTO implements Serializable {

    /**
     * 知识点ID：
     * - 新增时可为空 (由 User/Agent 服务生成)
     * - 更新时必填，对应 MySQL 中的主键和 Milvus 中的关联 Metadata
     */
    private Long id;

    /**
     * 所有者ID：用于多租户隔离，对应 RagDocumentDTO.userId
     */
    private Long ownerId;

    /**
     * 标题：作为图谱节点的唯一标识或主要属性
     */
    private String title;

    /**
     * 知识内容：完整的知识点详情，用于切片和 Embedding
     */
    private String content;

    /**
     * 摘要：用于 LLM 快速理解上下文，或者检索结果列表展示
     */
    private String summary;

    /**
     * 标签：列表形式，用于在 Neo4j 中建立 (:Knowledge)-[:HAS_TAG]->(:Tag) 关系
     */
    private List<String> tags;

    /**
     * 类别/领域：(如：JAVA_BACKEND, SYSTEM_DESIGN) 用于粗粒度过滤
     */
    private String category;

    /**
     * 来源描述：(如："会话总结", "上传文档", "手动录入")
     */
    private String sourceType;

    /**
     * 来源标识：(如：会话ID, 文件路径/URL)
     */
    private String sourceId;

    /**
     * 扩展属性：用于存储非结构化的额外信息 (如：重要性权重、艾宾浩斯初始强度等)
     */
    private Map<String, Object> extProperties;
}
