-- 学习记录 / 日志 (用于艾宾浩斯遗忘曲线管理)
CREATE TABLE IF NOT EXISTS `learning_records` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `concept_title` VARCHAR(255) NOT NULL COMMENT '知识点标题',
    `concept_summary` TEXT COMMENT '学习内容的总结',
    `vector_id` VARCHAR(64) DEFAULT NULL COMMENT '向量数据库中的ID',
    `graph_node_id` VARCHAR(64) DEFAULT NULL COMMENT '知识图谱中的节点ID',
    `mastery_level` INT DEFAULT 0 COMMENT '掌握程度 (0-5)',
    `next_review_time` DATETIME DEFAULT NULL COMMENT '基于艾宾浩斯的下一次复习时间',
    `last_review_time` DATETIME DEFAULT NULL COMMENT '上一次复习时间',
    `review_count` INT DEFAULT 0 COMMENT '复习次数',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_user_review` (`user_id`, `next_review_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户学习记录与艾宾浩斯计划表';

-- 事务日志 (用于 RocketMQ 幂等性)
CREATE TABLE IF NOT EXISTS `transaction_log` (
    `id` VARCHAR(64) NOT NULL PRIMARY KEY COMMENT '事务ID',
    `business_key` VARCHAR(64) NOT NULL COMMENT '业务键 (例如 record_id)',
    `status` VARCHAR(32) NOT NULL COMMENT '状态: PREPARED, COMMITTED, ROLLBACK',
    `payload` TEXT COMMENT '事务上下文载荷',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='可靠消息的本地事务日志';

-- 聊天会话元数据 (如果不仅仅存储在 Redis 中，则用于持久化)
CREATE TABLE IF NOT EXISTS `chat_sessions` (
    `session_id` VARCHAR(64) NOT NULL PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(255) DEFAULT NULL,
    `current_state` VARCHAR(32) DEFAULT 'IDLE' COMMENT '状态机状态: IDLE, LEARNING, SUMMARIZING...',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话持久化元数据';
