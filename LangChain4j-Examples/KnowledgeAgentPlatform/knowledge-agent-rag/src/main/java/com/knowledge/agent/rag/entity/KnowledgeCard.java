package com.knowledge.agent.rag.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识记忆卡片表
 * 对应 MySQL 表: knowledge_card
 */
@Data
@Builder
@TableName(value = "knowledge_card", autoResultMap = true)
public class KnowledgeCard implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文档ID (关联 Milvus 中的向量记录)
     */
    @TableId(type = IdType.INPUT)
    private Long docId;

    // --- 艾宾浩斯算法参数 ---

    /**
     * 难度因子 (Easiness Factor)
     * 范围通常在 1.3 - 2.5 之间
     */
    private Double easinessFactor;

    /**
     * 复习间隔 (天)
     */
    private Integer intervalDays;

    /**
     * 重复次数 (成功复习的次数)
     */
    private Integer repetition;

    /**
     * 下次复习截止时间
     */
    private LocalDateTime nextReviewDate;

    // --- 元数据 ---

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 逻辑删除标识
     */
    private Integer deleted;
}
