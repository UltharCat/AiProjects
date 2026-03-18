ALTER TABLE `sys_user`
    ADD COLUMN `preferences_json` TEXT NULL COMMENT 'Structured user preferences JSON' AFTER `learning_style`;
