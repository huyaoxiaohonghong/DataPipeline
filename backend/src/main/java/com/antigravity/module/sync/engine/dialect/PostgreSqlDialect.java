package com.antigravity.module.sync.engine.dialect;

/**
 * PostgreSQL 数据库方言
 * <p>
 * PostgreSQL 特性：
 * <ul>
 *   <li>Sink 端必须设置 database 字段（generate_sink_sql=true 时 SeaTunnel 强制要求）</li>
 *   <li>表名<b>不能</b>添加 schema 前缀 — 如果同时设置 database=postgres 和
 *       table=public.student，SeaTunnel 的 PostgresCatalog 会将三者组合为
 *       "postgres"."public"."student" 三段式标识，PostgreSQL 不支持跨数据库引用，
 *       会抛出 "relation does not exist" 错误</li>
 *   <li>直接使用表名（如 student），由 PostgreSQL 的 search_path 解析到
 *       默认的 public schema</li>
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
        // PostgreSQL 需要 schema.table 格式，默认 schema 为 public
        if (!tableName.contains(".")) {
            return DEFAULT_SCHEMA + "." + tableName;
        }
        return tableName;
    }

    @Override
    public boolean includeDatabaseInSink() {
        // 必须设置 database 字段，否则 generate_sink_sql=true 时 SeaTunnel 会报
        // OptionValidationException: options('database') are required
        return true;
    }

    @Override
    public String getDefaultSchema(String databaseName) {
        return DEFAULT_SCHEMA;
    }
}
