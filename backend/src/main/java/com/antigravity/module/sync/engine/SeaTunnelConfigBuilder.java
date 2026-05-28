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

            // transform（字段映射转换）
            if (mappings != null && !mappings.isEmpty() && hasFieldRename(mappings)) {
                ArrayNode transformArray = objectMapper.createArrayNode();
                transformArray.add(buildTransform(mappings));
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
        env.put("job.mode", "BATCH");
        return env;
    }

    /**
     * 构建 source 配置
     */
    private ObjectNode buildSource(SyncConfig config, DbConnection sourceDb,
                                   DatabaseDialect dialect, List<FieldMapping> mappings) {
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

        // 根据方言决定是否设置 database 字段
        // PostgreSQL 不能设置此字段，否则 SeaTunnel 会生成三段式表名导致错误
        if (dialect.includeDatabaseInSink()) {
            sink.put("database", targetDb.getDatabaseName());
        }

        // 使用方言格式化 Sink 端表名
        String sinkTable = dialect.formatSinkTable(config.getTargetTable());
        sink.put("table", sinkTable);

        // 自动建表
        sink.put("generate_sink_sql", true);

        boolean isFullSync = "FULL".equalsIgnoreCase(config.getSyncMode());
        boolean hasFieldMappings = mappings != null && !mappings.isEmpty();

        // Save Mode 配置
        // 全量同步 + 无字段映射 = 自动建表模式：
        //   使用 RECREATE_SCHEMA 让 SeaTunnel 自动根据源表结构在目标库创建表
        //   （先删后建，确保目标表结构与源表一致）
        // 其他情况：使用 IGNORE 跳过 Schema 处理（目标表需预先存在）
        if (isFullSync && !hasFieldMappings) {
            sink.put("schema_save_mode", "RECREATE_SCHEMA");
            sink.put("data_save_mode", "DROP_DATA");
        } else if (isFullSync) {
            sink.put("schema_save_mode", "IGNORE");
            sink.put("data_save_mode", "DROP_DATA");
        } else {
            sink.put("schema_save_mode", "IGNORE");
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
