package com.antigravity.module.sync.engine.dialect;

/**
 * MySQL 数据库方言
 * <p>
 * MySQL 特性：
 * <ul>
 *   <li>表名不需要 schema 前缀，database 已在 URL 中指定</li>
 *   <li>Sink 端需要设置 database 字段，SeaTunnel 用于自动建表等操作</li>
 *   <li>MetaData 查询中 schema 为 null（MySQL 的 schema 等同于 database）</li>
 * </ul>
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
public class MySqlDialect implements DatabaseDialect {

    @Override
    public String getDbType() {
        return "MYSQL";
    }

    @Override
    public String getDriverClassName() {
        return "com.mysql.cj.jdbc.Driver";
    }

    @Override
    public String buildJdbcUrl(String host, int port, String databaseName) {
        return String.format(
                "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true",
                host, port, databaseName);
    }

    @Override
    public String formatSinkTable(String tableName) {
        // MySQL 直接使用表名，database 已通过 URL 或 database 字段指定
        return tableName;
    }

    @Override
    public boolean includeDatabaseInSink() {
        return true;
    }

    @Override
    public String getDefaultSchema(String databaseName) {
        // MySQL 中 schema == database，JDBC MetaData 查询无需指定 schema
        return null;
    }
}
