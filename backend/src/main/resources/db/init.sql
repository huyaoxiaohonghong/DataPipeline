-- =============================================================================
-- DataPipeline 数据库初始化脚本
-- =============================================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `data_pipeline` 
    DEFAULT CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE `data_pipeline`;

-- =============================================================================
-- 权限表
-- =============================================================================
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `code`        VARCHAR(50)   NOT NULL COMMENT '权限编码',
    `name`        VARCHAR(50)   NOT NULL COMMENT '权限名称',
    `type`        VARCHAR(20)   NOT NULL COMMENT '权限类型: MENU-菜单, BUTTON-按钮, API-接口',
    `parent_id`   BIGINT        NOT NULL DEFAULT 0 COMMENT '父级ID',
    `path`        VARCHAR(200)  DEFAULT NULL COMMENT '路径/URL',
    `icon`        VARCHAR(50)   DEFAULT NULL COMMENT '图标',
    `sort`        INT           NOT NULL DEFAULT 0 COMMENT '排序号',
    `enabled`     TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '是否启用: 0-否, 1-是',
    `is_deleted`  TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否, 1-是',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_type` (`type`),
    KEY `idx_enabled` (`enabled`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- =============================================================================
-- 角色表
-- =============================================================================
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `code`        VARCHAR(30)   NOT NULL COMMENT '角色编码',
    `name`        VARCHAR(50)   NOT NULL COMMENT '角色名称',
    `description` VARCHAR(200)  DEFAULT NULL COMMENT '角色描述',
    `sort`        INT           NOT NULL DEFAULT 0 COMMENT '排序号',
    `enabled`     TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '是否启用: 0-否, 1-是',
    `is_deleted`  TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否, 1-是',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_name` (`name`),
    KEY `idx_enabled` (`enabled`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- =============================================================================
-- 角色权限关联表
-- =============================================================================
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
    `role_id`       BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (`role_id`, `permission_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- =============================================================================
-- 用户表
-- =============================================================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`    VARCHAR(50)   NOT NULL COMMENT '用户名',
    `password`    VARCHAR(255)  NOT NULL COMMENT '密码（MD5加密）',
    `email`       VARCHAR(100)  DEFAULT NULL COMMENT '邮箱',
    `role`        VARCHAR(20)   NOT NULL DEFAULT 'USER' COMMENT '角色编码',
    `is_deleted`  TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否, 1-是',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_email` (`email`),
    KEY `idx_role` (`role`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================================================
-- 初始化权限数据
-- =============================================================================
INSERT INTO `sys_permission` (`code`, `name`, `type`, `parent_id`, `path`, `icon`, `sort`, `enabled`) VALUES
    -- 菜单权限
    ('MENU:DASHBOARD', '仪表盘', 'MENU', 0, '/dashboard', 'DashboardOutlined', 1, 1),
    ('MENU:USER', '用户管理', 'MENU', 0, '/users', 'UserOutlined', 2, 1),
    ('MENU:ROLE', '角色管理', 'MENU', 0, '/roles', 'TeamOutlined', 3, 1),
    ('MENU:PERMISSION', '权限管理', 'MENU', 0, '/permissions', 'SafetyOutlined', 4, 1),
    
    -- 用户管理按钮权限
    ('BUTTON:USER:CREATE', '创建用户', 'BUTTON', 2, NULL, NULL, 1, 1),
    ('BUTTON:USER:UPDATE', '编辑用户', 'BUTTON', 2, NULL, NULL, 2, 1),
    ('BUTTON:USER:DELETE', '删除用户', 'BUTTON', 2, NULL, NULL, 3, 1),
    
    -- 角色管理按钮权限
    ('BUTTON:ROLE:CREATE', '创建角色', 'BUTTON', 3, NULL, NULL, 1, 1),
    ('BUTTON:ROLE:UPDATE', '编辑角色', 'BUTTON', 3, NULL, NULL, 2, 1),
    ('BUTTON:ROLE:DELETE', '删除角色', 'BUTTON', 3, NULL, NULL, 3, 1),
    ('BUTTON:ROLE:ASSIGN', '分配权限', 'BUTTON', 3, NULL, NULL, 4, 1),
    
    -- 权限管理按钮权限
    ('BUTTON:PERMISSION:CREATE', '创建权限', 'BUTTON', 4, NULL, NULL, 1, 1),
    ('BUTTON:PERMISSION:UPDATE', '编辑权限', 'BUTTON', 4, NULL, NULL, 2, 1),
    ('BUTTON:PERMISSION:DELETE', '删除权限', 'BUTTON', 4, NULL, NULL, 3, 1),
    
    -- API 权限
    ('API:USER:*', '用户管理所有接口', 'API', 2, '/api/v1/users/**', NULL, 1, 1),
    ('API:ROLE:*', '角色管理所有接口', 'API', 3, '/api/v1/roles/**', NULL, 1, 1),
    ('API:PERMISSION:*', '权限管理所有接口', 'API', 4, '/api/v1/permissions/**', NULL, 1, 1);

-- =============================================================================
-- 初始化角色数据
-- =============================================================================
INSERT INTO `sys_role` (`code`, `name`, `description`, `sort`, `enabled`) VALUES
    ('ADMIN', '超级管理员', '系统超级管理员，拥有所有权限', 1, 1),
    ('USER', '普通用户', '普通用户，拥有基本权限', 2, 1),
    ('GUEST', '访客', '访客用户，只有查看权限', 3, 1);

-- =============================================================================
-- 初始化角色权限关联
-- =============================================================================
-- 超级管理员拥有所有权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, id FROM `sys_permission` WHERE is_deleted = 0;

-- 普通用户拥有仪表盘权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES (2, 1);

-- 访客拥有仪表盘权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES (3, 1);

-- =============================================================================
-- 初始化用户数据
-- 密码加密算法: MD5(password + 'Antigravity@2024')
-- =============================================================================

-- 超级管理员账号: admin / admin@123
INSERT INTO `sys_user` (`username`, `password`, `email`, `role`, `is_deleted`) VALUES
    ('admin', MD5(CONCAT('admin@123', 'Antigravity@2024')), 'admin@datapipeline.com', 'ADMIN', 0);

-- 测试用户账号: test / test123
INSERT INTO `sys_user` (`username`, `password`, `email`, `role`, `is_deleted`) VALUES
    ('test', MD5(CONCAT('test123', 'Antigravity@2024')), 'test@datapipeline.com', 'USER', 0);
