package com.knowledge.agent.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class KnowledgeCardDTO implements Serializable {
    /** 知识卡片ID */
    private Long id;
    /** 所有者用户ID */
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
    /** 标签列表 */
    private List<String> tags;
    /** 领域/分类标签 */
    private String category;
    /** 来源类型（会话/文件/手动） */
    private String sourceType;
    /** 来源标识（会话ID/路径/URL） */
    private String sourceId;
    /** 艾宾浩斯易度系数 */
    private Double easinessFactor;
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
}
