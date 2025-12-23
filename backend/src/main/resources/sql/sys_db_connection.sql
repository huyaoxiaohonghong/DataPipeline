-- 数据库连接管理表
-- Database Connection Management Table
-- 执行此脚本前请确保已连接到 data_pipeline 数据库

CREATE TABLE IF NOT EXISTS `sys_db_connection` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '连接名称',
    `db_type` VARCHAR(20) NOT NULL COMMENT '数据库类型：MYSQL, POSTGRESQL, ORACLE, SQLSERVER',
    `host` VARCHAR(255) NOT NULL COMMENT '主机地址',
    `port` INT NOT NULL COMMENT '端口',
    `database_name` VARCHAR(100) NOT NULL COMMENT '数据库名',
    `username` VARCHAR(100) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（建议加密存储）',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_db_type` (`db_type`),
    KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据库连接配置表';

-- 插入示例数据（可选）
-- INSERT INTO `sys_db_connection` (`name`, `db_type`, `host`, `port`, `database_name`, `username`, `password`, `description`)
-- VALUES ('本地MySQL', 'MYSQL', 'localhost', 3306, 'test_db', 'root', '123456', '本地测试数据库');
