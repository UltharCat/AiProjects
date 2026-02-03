/*
 * V1.0.0__init_business_schema.sql
 * MySQL Business Data Schema
 * 包含：用户、日记、周报
 */

-- 1. 用户表
CREATE TABLE `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户主键ID',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `email` VARCHAR(128) COMMENT '邮箱',
    `avatar_url` VARCHAR(512) COMMENT '头像链接',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户基础信息表';

-- 2. 日记条目表 (核心业务表)
CREATE TABLE `diary_entries` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日记ID',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `entry_date` DATE NOT NULL COMMENT '日记日期',
    `content` TEXT NOT NULL COMMENT '日记原始内容',
    `summary` VARCHAR(1024) COMMENT '单篇日记自动摘要',
    `mood` VARCHAR(32) COMMENT '情感标签',
    `image_urls` JSON COMMENT '日记配图URL列表(JSON数组)',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING(待确认), COMMITTED(已提交), DELETED(已删除)',
    `transaction_id` VARCHAR(64) COMMENT '关联的RocketMQ事务ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_user_date` (`user_id`, `entry_date`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户日记表';

-- 3. 周报总结表
CREATE TABLE `weekly_summaries` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '周报ID',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `week_start_date` DATE NOT NULL COMMENT '周开始日期',
    `week_end_date` DATE NOT NULL COMMENT '周结束日期',
    `summary_content` TEXT NOT NULL COMMENT 'AI生成的周报文本',
    `generated_image_url` VARCHAR(512) COMMENT 'AI生成的周报配图',
    `user_feedback` TEXT COMMENT '用户修改意见',
    `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT(草稿), CONFIRMED(已确认)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_user_week` (`user_id`, `week_start_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='周报总结表';
