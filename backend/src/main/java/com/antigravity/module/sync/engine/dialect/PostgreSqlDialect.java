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
        // 关键：PostgreSQL 表名不能添加 schema 前缀！
        // 如果同时设置 database + schema.table，SeaTunnel 会生成三段式表名
        // (database.schema.table) 导致 PostgreSQL 报 "relation does not exist" 错误。
        // 直接使用裸表名，由 PostgreSQL 的 search_path 自动解析到 public schema。
        if (tableName.contains(".")) {
            // 用户已指定 schema.table 格式（如 public.student），提取裸表名
            return tableName.substring(tableName.lastIndexOf('.') + 1);
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
