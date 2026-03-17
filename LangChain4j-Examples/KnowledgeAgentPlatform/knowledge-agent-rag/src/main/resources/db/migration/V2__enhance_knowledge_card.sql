ALTER TABLE `knowledge_card`
    ADD COLUMN `user_id` BIGINT NULL COMMENT 'Owner user id' AFTER `doc_id`,
    ADD COLUMN `summary` TEXT NULL COMMENT 'Knowledge summary' AFTER `user_id`,
    ADD COLUMN `tags_json` TEXT NULL COMMENT 'Tags JSON payload' AFTER `summary`;

CREATE INDEX `idx_review_user_date` ON `knowledge_card` (`user_id`, `next_review_date`, `deleted`);
