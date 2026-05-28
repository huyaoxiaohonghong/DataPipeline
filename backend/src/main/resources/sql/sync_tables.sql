-- =====================================================
-- SeaTunnel 数据同步模块表结构
-- =====================================================

-- 1. 同步配置表
CREATE TABLE IF NOT EXISTS `sys_sync_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '配置名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `source_db_id` BIGINT NOT NULL COMMENT '源数据库连接ID',
    `source_table` VARCHAR(200) NOT NULL COMMENT '源表名',
    `target_db_id` BIGINT NOT NULL COMMENT '目标数据库连接ID',
    `target_table` VARCHAR(200) NOT NULL COMMENT '目标表名',
    `sync_mode` VARCHAR(20) NOT NULL DEFAULT 'FULL' COMMENT '同步模式：FULL-全量, INCREMENTAL-增量',
    `incremental_field` VARCHAR(100) DEFAULT NULL COMMENT '增量字段（增量模式使用）',
    `transform_sql` TEXT DEFAULT NULL COMMENT '实时计算SQL表达式',
    `last_sync_value` VARCHAR(100) DEFAULT NULL COMMENT '上次增量同步的值',
    `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_source_db` (`source_db_id`),
    KEY `idx_target_db` (`target_db_id`),
    KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='同步配置表';

-- 2. 字段映射表
CREATE TABLE IF NOT EXISTS `sys_field_mapping` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `config_id` BIGINT NOT NULL COMMENT '关联同步配置ID',
    `source_field` VARCHAR(100) NOT NULL COMMENT '源字段名',
    `source_type` VARCHAR(50) DEFAULT NULL COMMENT '源字段类型',
    `target_field` VARCHAR(100) NOT NULL COMMENT '目标字段名',
    `target_type` VARCHAR(50) DEFAULT NULL COMMENT '目标字段类型',
    `transform_expr` VARCHAR(500) DEFAULT NULL COMMENT '转换表达式（可选）',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_config_id` (`config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字段映射表';

-- 3. 同步任务表
CREATE TABLE IF NOT EXISTS `sys_sync_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `config_id` BIGINT NOT NULL COMMENT '关联同步配置ID',
    `config_name` VARCHAR(100) DEFAULT NULL COMMENT '配置名称（冗余）',
    `seatunnel_job_id` VARCHAR(50) DEFAULT NULL COMMENT 'SeaTunnel任务ID',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING, RUNNING, FINISHED, FAILED, CANCELED',
    `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
    `total_rows` BIGINT DEFAULT 0 COMMENT '总行数',
    `sync_rows` BIGINT DEFAULT 0 COMMENT '已同步行数',
    `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_config_id` (`config_id`),
    KEY `idx_status` (`status`),
    KEY `idx_seatunnel_job_id` (`seatunnel_job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='同步任务表';

-- 4. 任务日志表
CREATE TABLE IF NOT EXISTS `sys_sync_task_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_id` BIGINT NOT NULL COMMENT '关联任务ID',
    `log_level` VARCHAR(10) NOT NULL DEFAULT 'INFO' COMMENT '日志级别：INFO, WARN, ERROR',
    `message` TEXT NOT NULL COMMENT '日志内容',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务日志表';
