package com.antigravity.module.sync.engine.dialect;

/**
 * PostgreSQL 数据库方言
 * <p>
 * PostgreSQL 特性：
 * <ul>
 *   <li>使用 schema.table 两段式表名（如 public.student）</li>
 *   <li>Sink 端 <b>不能</b> 设置 database 字段 — SeaTunnel 的 PostgresCatalog
 *       会从 JDBC URL 解析 database name 并与 config 中的 database 字段合并，
 *       导致生成三段式标识（如 "postgres"."public"."student"），PostgreSQL
 *       不支持跨数据库引用，会抛出 "relation does not exist" 错误</li>
 *   <li>默认 Schema 为 public</li>
 * </ul>
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
public class PostgreSqlDialect implements DatabaseDialect {

    private static final String DEFAULT_SCHEMA = "public";

    @Override
    public String getDbType() {
        return "POSTGRESQL";
    }

    @Override
    public String getDriverClassName() {
        return "org.postgresql.Driver";
    }

    @Override
    public String buildJdbcUrl(String host, int port, String databaseName) {
        return String.format("jdbc:postgresql://%s:%d/%s", host, port, databaseName);
    }

    @Override
    public String formatSinkTable(String tableName) {
        // PostgreSQL 需要 schema.table 格式，SeaTunnel 解析后生成正确的两段式 SQL
        if (!tableName.contains(".")) {
            return DEFAULT_SCHEMA + "." + tableName;
        }
        return tableName;
    }

    @Override
    public boolean includeDatabaseInSink() {
        // 关键修复：PostgreSQL 不能在 Sink 中设置 database 字段
        // 否则 SeaTunnel 会生成三段式表名导致错误
        return false;
    }

    @Override
    public String getDefaultSchema(String databaseName) {
        return DEFAULT_SCHEMA;
    }
}
