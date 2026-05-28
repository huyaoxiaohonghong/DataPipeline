package com.antigravity.module.sync.engine.dialect;

/**
 * SQL Server 数据库方言
 * <p>
 * SQL Server 特性：
 * <ul>
 *   <li>JDBC URL 使用 ;databaseName= 参数格式</li>
 *   <li>默认 Schema 为 dbo</li>
 *   <li>表名需要 schema.table 格式（如 dbo.student）</li>
 *   <li>Sink 端需要设置 database 字段</li>
 * </ul>
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
public class SqlServerDialect implements DatabaseDialect {

    private static final String DEFAULT_SCHEMA = "dbo";

    @Override
    public String getDbType() {
        return "SQLSERVER";
    }

    @Override
    public String getDriverClassName() {
        return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    }

    @Override
    public String buildJdbcUrl(String host, int port, String databaseName) {
        return String.format(
                "jdbc:sqlserver://%s:%d;databaseName=%s;trustServerCertificate=true",
                host, port, databaseName);
    }

    @Override
    public String formatSinkTable(String tableName) {
        // SQL Server 需要 schema.table 格式，默认 schema 为 dbo
        if (!tableName.contains(".")) {
            return DEFAULT_SCHEMA + "." + tableName;
        }
        return tableName;
    }

    @Override
    public boolean includeDatabaseInSink() {
        return true;
    }

    @Override
    public String getDefaultSchema(String databaseName) {
        return DEFAULT_SCHEMA;
    }
}
