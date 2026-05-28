package com.antigravity.module.sync.engine;

import com.antigravity.common.BusinessException;
import com.antigravity.module.datasource.entity.DbConnection;
import com.antigravity.module.sync.engine.dialect.DatabaseDialect;
import com.antigravity.module.sync.engine.dialect.DatabaseDialectFactory;
import com.antigravity.module.sync.entity.FieldMapping;
import com.antigravity.module.sync.entity.SyncConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * SeaTunnel Job 配置构建器
 * <p>
 * 根据同步配置、数据库连接和字段映射信息，
 * 构建 SeaTunnel 2.3.8 所需的 Job JSON 配置。
 * <p>
 * 通过 {@link DatabaseDialect} 策略模式处理不同数据库之间的差异，
 * 包括 JDBC URL 构建、驱动类名、表名格式化、Sink database 字段等。
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeaTunnelConfigBuilder {

    private final ObjectMapper objectMapper;

    /**
     * 构建 SeaTunnel Job 配置 JSON
     *
     * @param config      同步配置
     * @param sourceDb    源数据库连接
     * @param targetDb    目标数据库连接
     * @param mappings    字段映射列表
     * @return Job 配置 JSON 字符串
     */
    public String buildJobConfig(SyncConfig config, DbConnection sourceDb, DbConnection targetDb, List<FieldMapping> mappings) {
        try {
            ObjectNode root = objectMapper.createObjectNode();

            // env 配置
            ObjectNode env = buildEnv(config);
            root.set("env", env);

            // source 配置
            DatabaseDialect sourceDialect = DatabaseDialectFactory.getDialect(sourceDb.getDbType());
            ArrayNode sourceArray = objectMapper.createArrayNode();
            sourceArray.add(buildSource(config, sourceDb, sourceDialect, mappings));
            root.set("source", sourceArray);

            // transform（字段重命名 或 实时计算 SQL 转换）
            boolean hasTransformSql = config.getTransformSql() != null && !config.getTransformSql().isBlank();
            boolean hasFieldRename = mappings != null && !mappings.isEmpty() && hasFieldRename(mappings);

            if (hasTransformSql || hasFieldRename) {
                ArrayNode transformArray = objectMapper.createArrayNode();
                if (hasTransformSql) {
                    transformArray.add(buildSqlTransform(config));
                } else {
                    transformArray.add(buildTransform(mappings));
                }
                root.set("transform", transformArray);
            }

            // sink 配置
            DatabaseDialect targetDialect = DatabaseDialectFactory.getDialect(targetDb.getDbType());
            ArrayNode sinkArray = objectMapper.createArrayNode();
            sinkArray.add(buildSink(config, targetDb, targetDialect, mappings));
            root.set("sink", sinkArray);

            String configJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
            log.debug("构建 SeaTunnel Job 配置: {}", configJson);
            return configJson;
        } catch (Exception e) {
            log.error("构建 SeaTunnel Job 配置失败: {}", e.getMessage());
            throw BusinessException.of("构建同步任务配置失败: " + e.getMessage());
        }
    }

    /**
     * 构建 env 配置
     */
    private ObjectNode buildEnv(SyncConfig config) {
        ObjectNode env = objectMapper.createObjectNode();
        env.put("job.name", "sync_" + config.getName() + "_" + System.currentTimeMillis());
        env.put("job.mode", "REALTIME".equalsIgnoreCase(config.getSyncMode()) ? "STREAM" : "BATCH");
        return env;
    }

    /**
     * 构建 source 配置
     */
    private ObjectNode buildSource(SyncConfig config, DbConnection sourceDb,
                                   DatabaseDialect dialect, List<FieldMapping> mappings) {
        if ("REALTIME".equalsIgnoreCase(config.getSyncMode())) {
            if (!"MYSQL".equalsIgnoreCase(sourceDb.getDbType())) {
                throw BusinessException.of("实时同步当前仅支持 MySQL 数据源（基于 MySQL-CDC 实时监听）");
            }
            return buildCdcSource(config, sourceDb);
        }

        ObjectNode source = objectMapper.createObjectNode();
        source.put("plugin_name", "Jdbc");

        String jdbcUrl = dialect.buildJdbcUrl(sourceDb.getHost(), sourceDb.getPort(), sourceDb.getDatabaseName());
        source.put("url", jdbcUrl);
        source.put("driver", dialect.getDriverClassName());
        source.put("user", sourceDb.getUsername());
        source.put("password", sourceDb.getPassword());

        // 构建查询 SQL
        String query = buildSourceQuery(config, dialect, mappings);
        source.put("query", query);

        if (config.getTransformSql() != null && !config.getTransformSql().isBlank()) {
            source.put("result_table_name", "source_data");
        }

        return source;
    }

    /**
     * 构建 MySQL-CDC 实时流数据源配置
     */
    private ObjectNode buildCdcSource(SyncConfig config, DbConnection sourceDb) {
        ObjectNode source = objectMapper.createObjectNode();
        source.put("plugin_name", "MySQL-CDC");
        source.put("parallelism", 1);
        source.put("server-id", 5400 + (int) (Math.random() * 100)); // 随机生成 server-id 范围，避免冲突

        // base-url 格式为 jdbc:mysql://host:port/databaseName
        String baseUrl = String.format("jdbc:mysql://%s:%d/%s", 
                sourceDb.getHost(), sourceDb.getPort(), sourceDb.getDatabaseName());
        source.put("base-url", baseUrl);
        source.put("username", sourceDb.getUsername());
        source.put("password", sourceDb.getPassword());

        ArrayNode dbNames = objectMapper.createArrayNode();
        dbNames.add(sourceDb.getDatabaseName());
        source.set("database-names", dbNames);

        ArrayNode tableNames = objectMapper.createArrayNode();
        tableNames.add(sourceDb.getDatabaseName() + "." + config.getSourceTable());
        source.set("table-names", tableNames);

        source.put("startup.mode", "initial"); // initial 模式会先读存量快照，再平滑读取 binlog 实时流
        
        if (config.getTransformSql() != null && !config.getTransformSql().isBlank()) {
            source.put("result_table_name", "source_data");
        }
        
        return source;
    }

    /**
     * 构建 source 查询 SQL
     */
    private String buildSourceQuery(SyncConfig config, DatabaseDialect dialect, List<FieldMapping> mappings) {
        StringBuilder sql = new StringBuilder("SELECT ");

        if (mappings != null && !mappings.isEmpty()) {
            // 只查询映射中定义的源字段
            List<String> fields = mappings.stream()
                    .map(FieldMapping::getSourceField)
                    .toList();
            sql.append(String.join(", ", fields));
        } else {
            sql.append("*");
        }

        // 使用方言格式化源表名
        String tableName = dialect.formatSourceTable(config.getSourceTable());
        sql.append(" FROM ").append(tableName);

        // 增量模式添加 WHERE 条件
        if ("INCREMENTAL".equalsIgnoreCase(config.getSyncMode()) && config.getIncrementalField() != null) {
            if (config.getLastSyncValue() != null && !config.getLastSyncValue().isEmpty()) {
                sql.append(" WHERE ").append(config.getIncrementalField())
                        .append(" > '").append(config.getLastSyncValue()).append("'");
            }
        }

        return sql.toString();
    }

    /**
     * 构建 transform 配置（自定义 SQL 转换）
     */
    private ObjectNode buildSqlTransform(SyncConfig config) {
        ObjectNode transform = objectMapper.createObjectNode();
        transform.put("plugin_name", "Sql");
        transform.put("source_table_name", "source_data");
        transform.put("query", config.getTransformSql().trim());
        transform.put("result_table_name", "transformed_data");
        return transform;
    }

    /**
     * 构建 transform 配置（字段重命名）
     */
    private ObjectNode buildTransform(List<FieldMapping> mappings) {
        ObjectNode transform = objectMapper.createObjectNode();
        transform.put("plugin_name", "FieldMapper");

        ObjectNode fieldMapper = objectMapper.createObjectNode();
        for (FieldMapping mapping : mappings) {
            if (!mapping.getSourceField().equals(mapping.getTargetField())) {
                fieldMapper.put(mapping.getSourceField(), mapping.getTargetField());
            }
        }
        transform.set("field_mapper", fieldMapper);

        return transform;
    }

    /**
     * 构建 sink 配置
     * <p>
     * 通过 {@link DatabaseDialect} 处理不同数据库的差异：
     * <ul>
     *   <li>表名格式化：PostgreSQL 需要 schema.table，MySQL 直接用表名</li>
     *   <li>database 字段：PostgreSQL 不设置（避免三段式表名），MySQL/Oracle 需要设置</li>
     * </ul>
     */
    private ObjectNode buildSink(SyncConfig config, DbConnection targetDb,
                                 DatabaseDialect dialect, List<FieldMapping> mappings) {
        ObjectNode sink = objectMapper.createObjectNode();
        sink.put("plugin_name", "Jdbc");

        String jdbcUrl = dialect.buildJdbcUrl(targetDb.getHost(), targetDb.getPort(), targetDb.getDatabaseName());
        sink.put("url", jdbcUrl);
        sink.put("driver", dialect.getDriverClassName());
        sink.put("user", targetDb.getUsername());
        sink.put("password", targetDb.getPassword());

        // 根据方言决定是否设置 database 字段，并在需要时通过方言获取其适当值（如 PostgreSQL 返回 Schema 名）
        if (dialect.includeDatabaseInSink()) {
            sink.put("database", dialect.getSinkDatabaseName(targetDb.getDatabaseName(), config.getTargetTable()));
        }

        // 使用方言格式化 Sink 端表名
        String sinkTable = dialect.formatSinkTable(config.getTargetTable());
        sink.put("table", sinkTable);

        // 如果配置了实时计算 SQL，则从 transformed_data 数据流写入
        if (config.getTransformSql() != null && !config.getTransformSql().isBlank()) {
            sink.put("source_table_name", "transformed_data");
        }

        // 自动建表
        sink.put("generate_sink_sql", true);

        boolean isRealtime = "REALTIME".equalsIgnoreCase(config.getSyncMode());
        boolean isFullSync = "FULL".equalsIgnoreCase(config.getSyncMode());
        boolean hasFieldMappings = mappings != null && !mappings.isEmpty();

        // Save Mode 配置
        // 自动建表策略：
        //   - 实时流同步：不删表且追加数据（保留结构并持续应用增量变动）
        //   - 全量同步且无字段映射：使用 RECREATE_SCHEMA 强制重建目标表（先删后建，保证结构完全一致）
        //   - 其他情况：使用 CREATE_SCHEMA_WHEN_NOT_EXIST（不存在则自动建表，存在则保留结构）
        if (isRealtime) {
            sink.put("schema_save_mode", "CREATE_SCHEMA_WHEN_NOT_EXIST");
            sink.put("data_save_mode", "APPEND_DATA");
        } else if (isFullSync && !hasFieldMappings) {
            sink.put("schema_save_mode", "RECREATE_SCHEMA");
            sink.put("data_save_mode", "DROP_DATA");
        } else if (isFullSync) {
            sink.put("schema_save_mode", "CREATE_SCHEMA_WHEN_NOT_EXIST");
            sink.put("data_save_mode", "DROP_DATA");
        } else {
            sink.put("schema_save_mode", "CREATE_SCHEMA_WHEN_NOT_EXIST");
            sink.put("data_save_mode", "APPEND_DATA");
        }

        return sink;
    }

    /**
     * 判断字段映射中是否存在重命名
     */
    private boolean hasFieldRename(List<FieldMapping> mappings) {
        return mappings.stream()
                .anyMatch(m -> !m.getSourceField().equals(m.getTargetField()));
    }

}
