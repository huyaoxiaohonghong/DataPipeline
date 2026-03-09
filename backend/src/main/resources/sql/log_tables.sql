-- =====================================================
-- 日志模块表结构
-- =====================================================

-- 1. 登录日志表
CREATE TABLE IF NOT EXISTS `sys_login_log` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`   VARCHAR(50)  DEFAULT NULL COMMENT '用户账号',
    `ip_address` VARCHAR(128) DEFAULT NULL COMMENT '登录IP地址',
    `browser`    VARCHAR(50)  DEFAULT NULL COMMENT '浏览器类型',
    `os`         VARCHAR(50)  DEFAULT NULL COMMENT '操作系统',
    `status`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '登录状态：1-成功，0-失败',
    `message`    VARCHAR(255) DEFAULT NULL COMMENT '提示消息',
    `login_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (`id`),
    KEY `idx_username` (`username`),
    KEY `idx_status` (`status`),
    KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- 2. 操作日志表
CREATE TABLE IF NOT EXISTS `sys_operation_log` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`    VARCHAR(50)   DEFAULT NULL COMMENT '操作人员',
    `module`      VARCHAR(50)   DEFAULT NULL COMMENT '系统模块',
    `action`      VARCHAR(50)   DEFAULT NULL COMMENT '操作类型',
    `method`      VARCHAR(200)  DEFAULT NULL COMMENT '请求方法',
    `params`      TEXT          DEFAULT NULL COMMENT '请求参数',
    `ip_address`  VARCHAR(128)  DEFAULT NULL COMMENT '主机IP',
    `duration`    BIGINT        DEFAULT 0 COMMENT '耗时(ms)',
    `status`      TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '操作状态：1-成功，0-异常',
    `error_msg`   TEXT          DEFAULT NULL COMMENT '错误信息',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_username` (`username`),
    KEY `idx_module` (`module`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';
