package com.antigravity.common;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 数据库自愈迁移组件
 * <p>
 * 在系统启动时自动检查并升级表结构，确保平滑落地。
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseMigration {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrate() {
        try {
            // 检查 sys_sync_config 表中是否存在 transform_sql 字段
            String checkSql = "SELECT COUNT(*) FROM information_schema.columns " +
                    "WHERE table_schema = DATABASE() " +
                    "AND table_name = 'sys_sync_config' " +
                    "AND column_name = 'transform_sql'";
            
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            if (count != null && count == 0) {
                log.info("开始执行数据库迁移：为 sys_sync_config 表添加 transform_sql 字段...");
                String alterSql = "ALTER TABLE sys_sync_config ADD COLUMN transform_sql TEXT DEFAULT NULL COMMENT '实时计算SQL表达式' AFTER incremental_field";
                jdbcTemplate.execute(alterSql);
                log.info("数据库迁移成功：已成功添加 transform_sql 字段！");
            } else {
                log.debug("数据库检查：sys_sync_config.transform_sql 字段已存在，跳过迁移。");
            }
        } catch (Exception e) {
            log.error("数据库迁移异常，请手动执行升级语句 (ALTER TABLE sys_sync_config ADD COLUMN transform_sql TEXT DEFAULT NULL): {}", e.getMessage());
        }
    }
}
