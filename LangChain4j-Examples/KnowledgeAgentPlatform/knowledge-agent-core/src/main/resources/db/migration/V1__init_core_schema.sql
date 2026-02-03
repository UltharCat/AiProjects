-- Learning Records / Journal (For Ebbinghaus Manager)
CREATE TABLE IF NOT EXISTS `learning_records` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT 'User ID',
    `concept_title` VARCHAR(255) NOT NULL COMMENT 'Knowledge Concept Title',
    `concept_summary` TEXT COMMENT 'Summary of the learned concept',
    `vector_id` VARCHAR(64) DEFAULT NULL COMMENT 'ID in Vector Database',
    `graph_node_id` VARCHAR(64) DEFAULT NULL COMMENT 'ID in Knowledge Graph',
    `mastery_level` INT DEFAULT 0 COMMENT 'Mastery Level (0-5)',
    `next_review_time` DATETIME DEFAULT NULL COMMENT 'Next review time based on Ebbinghaus',
    `last_review_time` DATETIME DEFAULT NULL COMMENT 'Last review time',
    `review_count` INT DEFAULT 0 COMMENT 'Number of reviews',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_user_review` (`user_id`, `next_review_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User Learning Records & Ebbinghaus Schedule';

-- Transaction Log (For RocketMQ idempotency)
CREATE TABLE IF NOT EXISTS `transaction_log` (
    `id` VARCHAR(64) NOT NULL PRIMARY KEY COMMENT 'Transaction ID',
    `business_key` VARCHAR(64) NOT NULL COMMENT 'Business Key (e.g. record_id)',
    `status` VARCHAR(32) NOT NULL COMMENT 'Status: PREPARED, COMMITTED, ROLLBACK',
    `payload` TEXT COMMENT 'Transaction Context Payload',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Local Transaction Log for Reliable Messaging';

-- Chat Session Metadata (If not fully in Redis, for persistence)
CREATE TABLE IF NOT EXISTS `chat_sessions` (
    `session_id` VARCHAR(64) NOT NULL PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(255) DEFAULT NULL,
    `current_state` VARCHAR(32) DEFAULT 'IDLE' COMMENT 'FSM State: IDLE, LEARNING, SUMMARIZING...',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Chat Session Persistent Metadata';
