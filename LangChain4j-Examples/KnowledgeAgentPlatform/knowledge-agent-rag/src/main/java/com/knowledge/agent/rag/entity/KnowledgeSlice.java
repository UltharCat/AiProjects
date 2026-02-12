package com.knowledge.agent.rag.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge_slice")
public class KnowledgeSlice {
    @TableId(type = IdType.AUTO)
    /** 主键 */
    private Long id;
    /** 父知识卡片ID */
    private Long knowledgeId;
    /** 知识所有者（租户用户） */
    private Long ownerId;
    /** 切片序列索引 */
    private Integer chunkIndex;
    /** 切片文本内容 */
    private String content;
    /** Token计数估计 */
    private Integer tokenCount;
    /** 向量存储ID（Milvus） */
    private String vectorId;
    /** 切片的JSON元数据 */
    private String metadata; // JSON
    /** 行创建时间 */
    private LocalDateTime createdAt;
}
