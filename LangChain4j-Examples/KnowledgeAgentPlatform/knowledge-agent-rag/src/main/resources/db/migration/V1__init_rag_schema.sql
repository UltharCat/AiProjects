-- 核心知识卡片表
CREATE TABLE IF NOT EXISTS `knowledge_card` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '知识卡片ID',
    `owner_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `title` VARCHAR(255) DEFAULT NULL COMMENT '标题',
    `question` TEXT DEFAULT NULL COMMENT '问题',
    `answer` TEXT DEFAULT NULL COMMENT '答案',
    `content` MEDIUMTEXT DEFAULT NULL COMMENT '合并后的正文内容',
    `summary` TEXT DEFAULT NULL COMMENT '摘要',
    `tags` JSON DEFAULT NULL COMMENT '标签(JSON数组)',
    `category` VARCHAR(64) DEFAULT NULL COMMENT '类别/领域',
    `source_type` VARCHAR(64) DEFAULT NULL COMMENT '来源类型',
    `source_id` VARCHAR(128) DEFAULT NULL COMMENT '来源标识',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 1-启用, 0-归档',
    `easiness_factor` DECIMAL(4,2) DEFAULT 2.50 COMMENT '艾宾浩斯易度系数',
    `interval_days` INT DEFAULT 0 COMMENT '复习间隔(天)',
    `repetition` INT DEFAULT 0 COMMENT '重复次数',
    `next_review_time` DATETIME DEFAULT NULL COMMENT '下次复习时间',
    `review_count` INT DEFAULT 0 COMMENT '复习次数',
    `last_review_time` DATETIME DEFAULT NULL COMMENT '上次复习时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_owner_review` (`owner_id`, `next_review_time`),
    FULLTEXT KEY `ft_core_text` (`question`, `answer`, `content`, `summary`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识卡片核心表';

-- 知识切片表 (用于向量索引/混合检索)
CREATE TABLE IF NOT EXISTS `knowledge_slice` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '切片ID',
    `knowledge_id` BIGINT NOT NULL COMMENT '关联知识卡片ID',
    `owner_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `chunk_index` INT NOT NULL COMMENT '切片序号',
    `content` TEXT NOT NULL COMMENT '切片文本内容',
    `token_count` INT DEFAULT NULL COMMENT '切片token数量',
    `vector_id` VARCHAR(64) DEFAULT NULL COMMENT '向量库中的ID',
    `metadata` JSON DEFAULT NULL COMMENT '切片元数据(JSON)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_knowledge_chunk` (`knowledge_id`, `chunk_index`),
    FULLTEXT KEY `ft_slice_text` (`content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识切片表';

