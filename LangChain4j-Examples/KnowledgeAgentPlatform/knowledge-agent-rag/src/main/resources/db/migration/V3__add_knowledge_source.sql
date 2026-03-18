ALTER TABLE `knowledge_card`
    ADD COLUMN `source` VARCHAR(255) NULL COMMENT 'Knowledge source descriptor' AFTER `summary`;
