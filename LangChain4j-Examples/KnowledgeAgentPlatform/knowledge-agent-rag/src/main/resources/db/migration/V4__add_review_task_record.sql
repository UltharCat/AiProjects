CREATE TABLE IF NOT EXISTS `review_task_record` (
    `task_id` VARCHAR(64) NOT NULL COMMENT 'Review task id',
    `batch_id` VARCHAR(64) NOT NULL COMMENT 'Review batch id',
    `user_id` BIGINT NOT NULL COMMENT 'Owner user id',
    `knowledge_id` BIGINT NOT NULL COMMENT 'Knowledge card id',
    `summary` TEXT NULL COMMENT 'Knowledge summary snapshot',
    `due_at` DATETIME NULL COMMENT 'Due time snapshot',
    `trigger_source` VARCHAR(32) NOT NULL COMMENT 'LOGIN MANUAL SCHEDULED',
    `status` VARCHAR(32) NOT NULL COMMENT 'PENDING DISPATCHED COMPLETED SKIPPED',
    `dedup_key` VARCHAR(128) NULL COMMENT 'Dedup cache key snapshot',
    `requested_limit` INT NULL COMMENT 'Requested batch size',
    `batch_created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Batch creation time',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Row creation time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Row update time',
    PRIMARY KEY (`task_id`),
    KEY `idx_review_batch_lookup` (`user_id`, `trigger_source`, `batch_created_at`),
    KEY `idx_review_batch_id` (`batch_id`),
    KEY `idx_review_knowledge_status` (`user_id`, `knowledge_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Durable review task batch records';
