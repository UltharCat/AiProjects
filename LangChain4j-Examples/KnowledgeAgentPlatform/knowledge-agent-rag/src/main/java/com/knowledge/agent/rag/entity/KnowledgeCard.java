package com.knowledge.agent.rag.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("knowledge_card")
public class KnowledgeCard {
    @TableId(type = IdType.AUTO)
    /** 主键 */
    private Long id;
    /** 知识所有者（租户用户） */
    private Long ownerId;
    /** 显示标题 */
    private String title;
    /** 问题/提示 */
    private String question;
    /** 答案/解决方案 */
    private String answer;
    /** 合并的完整内容 */
    private String content;
    /** 简短摘要，用于预览 */
    private String summary;
    /** 标签，作为JSON数组字符串 */
    private String tags; // JSON
    /** 领域/分类标签 */
    private String category;
    /** 来源类型（会话/文件/手动） */
    private String sourceType;
    /** 来源标识（会话ID/路径/URL） */
    private String sourceId;
    /** 状态标志（1 激活，0 归档） */
    private Integer status;
    /** 艾宾浩斯易度系数 */
    private BigDecimal easinessFactor;
    /** 复习间隔天数 */
    private Integer intervalDays;
    /** SM-2重复计数 */
    private Integer repetition;
    /** 下次复习日期时间 */
    private LocalDateTime nextReviewTime;
    /** 总复习计数 */
    private Integer reviewCount;
    /** 上次复习日期时间 */
    private LocalDateTime lastReviewTime;
    /** 行创建时间 */
    private LocalDateTime createdAt;
    /** 行更新时间 */
    private LocalDateTime updatedAt;
}
