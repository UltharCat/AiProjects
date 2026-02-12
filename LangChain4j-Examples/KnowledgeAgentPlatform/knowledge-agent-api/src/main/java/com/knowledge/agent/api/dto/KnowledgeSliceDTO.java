package com.knowledge.agent.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class KnowledgeSliceDTO implements Serializable {
    /** 切片ID */
    private Long id;
    /** 父知识卡片ID */
    private Long knowledgeId;
    /** 所有者用户ID */
    private Long ownerId;
    /** 切片序列索引 */
    private Integer chunkIndex;
    /** 切片文本内容 */
    private String content;
    /** Token计数估计 */
    private Integer tokenCount;
    /** 向量存储ID（Milvus） */
    private String vectorId;
    /** 任意元数据映射 */
    private Map<String, Object> metadata;
    /** 行创建时间 */
    private LocalDateTime createdAt;
}
