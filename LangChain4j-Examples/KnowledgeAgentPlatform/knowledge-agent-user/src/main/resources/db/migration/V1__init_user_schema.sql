-- Users Table
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'User ID',
    `username` VARCHAR(64) NOT NULL UNIQUE COMMENT 'Username',
    `password` VARCHAR(255) NOT NULL COMMENT 'Encrypted Password',
    `email` VARCHAR(128) DEFAULT NULL COMMENT 'Email',
    `nickname` VARCHAR(64) DEFAULT NULL COMMENT 'Nickname',
    `avatar_url` VARCHAR(512) DEFAULT NULL COMMENT 'Avatar Application',
    `status` TINYINT DEFAULT 1 COMMENT 'Status: 1-Active, 0-Disabled',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation Time',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User Info';

-- Roles Table
CREATE TABLE IF NOT EXISTS `roles` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Role ID',
    `code` VARCHAR(32) NOT NULL UNIQUE COMMENT 'Role Code (e.g. USER, ADMIN)',
    `name` VARCHAR(64) NOT NULL COMMENT 'Role Name',
    `description` VARCHAR(255) DEFAULT NULL COMMENT 'Description',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Roles';

-- User Roles Relation
CREATE TABLE IF NOT EXISTS `user_roles` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT 'User ID',
    `role_id` BIGINT NOT NULL COMMENT 'Role ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User Role Mapping';

-- User Agent Settings (For dynamic role binding)
CREATE TABLE IF NOT EXISTS `user_agent_configs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT 'User ID',
    `agent_name` VARCHAR(64) DEFAULT 'Default' COMMENT 'Bound Agent Name/Role',
    `personality_tags` JSON DEFAULT NULL COMMENT 'Learned personality tags (JSON)',
    `interaction_style` VARCHAR(255) DEFAULT NULL COMMENT 'Preferred interaction style',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_agent` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User Specific Agent Configuration';

-- Initial Data
INSERT INTO `roles` (`code`, `name`, `description`) VALUES ('USER', 'Ordinary User', 'Standard user with learning capabilities');
INSERT INTO `roles` (`code`, `name`, `description`) VALUES ('ADMIN', 'Administrator', 'System administrator');
