-- V1__init_rag_schema.sql
-- Flyway Migration Script
-- Matches com.knowledge.agent.rag.entity.KnowledgeCard

CREATE TABLE IF NOT EXISTS `knowledge_card` (
    `doc_id` BIGINT NOT NULL COMMENT '文档ID',

    -- Ebbinghaus Memory Algorithm Parameters
    `easiness_factor` DOUBLE NOT NULL DEFAULT 2.5 COMMENT '难度因子(EF)',
    `interval_days` INT NOT NULL DEFAULT 1 COMMENT '复习间隔(天)',
    `repetition` INT NOT NULL DEFAULT 0 COMMENT '重复次数(连胜次数)',
    `next_review_date` DATETIME DEFAULT NULL COMMENT '下次复习时间',

    -- Metadata (Snake case mapping to Java camelCase)
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0:否, 1:是)',

    PRIMARY KEY (`doc_id`),
    KEY `idx_next_review` (`next_review_date`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识记忆卡片表';
