package com.antigravity.module.sync.engine.dialect;

/**
 * Oracle 数据库方言
 * <p>
 * Oracle 特性：
 * <ul>
 *   <li>使用 SID 模式的 JDBC URL（如 jdbc:oracle:thin:@host:port:SID）</li>
 *   <li>表名需要 schema.table 格式，schema 通常为大写用户名</li>
 *   <li>Sink 端需要设置 database 字段（SeaTunnel 用于 Oracle Catalog 操作）</li>
 *   <li>标识符默认大写</li>
 * </ul>
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
public class OracleDialect implements DatabaseDialect {

    @Override
    public String getDbType() {
        return "ORACLE";
    }

    @Override
    public String getDriverClassName() {
        return "oracle.jdbc.OracleDriver";
    }

    @Override
    public String buildJdbcUrl(String host, int port, String databaseName) {
        // 使用 SID 模式；如果需要 Service Name 模式，可改为 /@host:port/serviceName
        return String.format("jdbc:oracle:thin:@%s:%d:%s", host, port, databaseName);
    }

    @Override
    public String formatSinkTable(String tableName) {
        // Oracle 的 schema 通常通过 JDBC 连接的用户名确定
        // 如果用户指定了 schema.table 格式，保持原样
        // 否则直接使用表名（Oracle 将使用连接用户的默认 schema）
        return tableName.toUpperCase();
    }

    @Override
    public String formatSourceTable(String tableName) {
        return tableName.toUpperCase();
    }

    @Override
    public boolean includeDatabaseInSink() {
        return true;
    }

    @Override
    public String getDefaultSchema(String databaseName) {
        // Oracle 中 schema 通常等于用户名（大写）
        return databaseName != null ? databaseName.toUpperCase() : null;
    }
}
