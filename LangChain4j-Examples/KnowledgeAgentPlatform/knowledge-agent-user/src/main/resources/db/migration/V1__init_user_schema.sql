-- V1__init_user_schema.sql
-- Flyway Migration Script for Knowledge Agent User
-- Matches com.knowledge.agent.user.entity.SysUser

CREATE TABLE IF NOT EXISTS `sys_user` (
   `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID (Auto Increment)',
   `username` VARCHAR(50) NOT NULL COMMENT '用户名',
   `password` VARCHAR(100) DEFAULT NULL COMMENT '加密密码',
   `learning_style` VARCHAR(20) DEFAULT 'SOCRATIC' COMMENT '学习风格(SOCRATIC, DIRECT, ELABORATE)',

   -- Metadata
   `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
   `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0:否, 1:是)',

   PRIMARY KEY (`id`),
   UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户基础信息表';

