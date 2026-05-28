package com.antigravity.module.sync.engine.dialect;

/**
 * 数据库方言接口
 * <p>
 * 封装不同数据库（MySQL、PostgreSQL、Oracle、SQL Server）在 SeaTunnel
 * 同步配置构建过程中的差异化行为，包括：
 * <ul>
 *   <li>JDBC 驱动类名</li>
 *   <li>JDBC URL 构建规则</li>
 *   <li>Source 查询中表名的引用格式</li>
 *   <li>Sink 端表名的格式化规则</li>
 *   <li>Sink 端是否需要 database 字段</li>
 *   <li>默认 Schema 名称</li>
 * </ul>
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
public interface DatabaseDialect {

    /**
     * 获取数据库类型标识（大写，如 MYSQL、POSTGRESQL）
     */
    String getDbType();

    /**
     * 获取 JDBC 驱动类全限定名
     */
    String getDriverClassName();

    /**
     * 构建 JDBC URL
     *
     * @param host         主机地址
     * @param port         端口
     * @param databaseName 数据库名
     * @return 完整的 JDBC URL
     */
    String buildJdbcUrl(String host, int port, String databaseName);

    /**
     * 格式化 Sink 端表名
     * <p>
     * 不同数据库对表名有不同的要求：
     * <ul>
     *   <li>MySQL: 直接使用表名（如 student）</li>
     *   <li>PostgreSQL: 需要 schema.table 格式（如 public.student）</li>
     *   <li>Oracle: 需要 schema.table 格式（如 SCHEMA.STUDENT）</li>
     *   <li>SQL Server: 需要 schema.table 格式（如 dbo.student）</li>
     * </ul>
     *
     * @param tableName 用户配置的表名
     * @return 格式化后的表名
     */
    String formatSinkTable(String tableName);

    /**
     * 格式化 Source 端查询中的表名
     * <p>
     * 用于构建 SELECT ... FROM {table} 中的表名部分。
     *
     * @param tableName 用户配置的表名
     * @return 格式化后的表名
     */
    default String formatSourceTable(String tableName) {
        return tableName;
    }

    /**
     * 是否在 Sink 配置中包含 database 字段
     * <p>
     * PostgreSQL 的 JDBC URL 已包含数据库名，SeaTunnel 的 PostgresCatalog
     * 会从 URL 中解析并构建 TablePath，再额外设置 database 字段会导致
     * 生成三段式表名（如 "postgres"."public"."student"），引发找不到表的错误。
     *
     * @return true 表示需要设置 database 字段；false 表示不设置
     */
    boolean includeDatabaseInSink();

    /**
     * 获取默认 Schema 名称（用于 JDBC MetaData 查询）
     *
     * @param databaseName 数据库名（Oracle 中 Schema 通常与用户名相同）
     * @return 默认 Schema 名，无则返回 null
     */
    String getDefaultSchema(String databaseName);
}
