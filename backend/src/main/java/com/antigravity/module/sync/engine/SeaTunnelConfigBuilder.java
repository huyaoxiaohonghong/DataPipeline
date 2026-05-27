package com.antigravity.module.sync.engine;

import com.antigravity.common.BusinessException;
import com.antigravity.module.datasource.entity.DbConnection;
import com.antigravity.module.datasource.service.impl.DbConnectionServiceImpl;
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
 * 构建 SeaTunnel 2.3.3 所需的 Job JSON 配置。
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
            ArrayNode sourceArray = objectMapper.createArrayNode();
            sourceArray.add(buildSource(config, sourceDb, mappings));
            root.set("source", sourceArray);

            // transform（字段映射转换）
            if (mappings != null && !mappings.isEmpty() && hasFieldRename(mappings)) {
                ArrayNode transformArray = objectMapper.createArrayNode();
                transformArray.add(buildTransform(mappings));
                root.set("transform", transformArray);
            }

            // sink 配置
            ArrayNode sinkArray = objectMapper.createArrayNode();
            sinkArray.add(buildSink(config, targetDb, mappings));
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
    private ObjectNode buildSource(SyncConfig config, DbConnection sourceDb, List<FieldMapping> mappings) {
        ObjectNode source = objectMapper.createObjectNode();
        String pluginName = getJdbcPluginName(sourceDb.getDbType());
        source.put("plugin_name", pluginName);

        String jdbcUrl = DbConnectionServiceImpl.buildJdbcUrl(
                sourceDb.getDbType(), sourceDb.getHost(), sourceDb.getPort(), sourceDb.getDatabaseName());
        source.put("url", jdbcUrl);
        source.put("driver", getJdbcDriver(sourceDb.getDbType()));
        source.put("user", sourceDb.getUsername());
        source.put("password", sourceDb.getPassword());

        // 构建查询 SQL
        String query = buildSourceQuery(config, sourceDb, mappings);
        source.put("query", query);

        return source;
    }

    /**
     * 构建 source 查询 SQL
     */
    private String buildSourceQuery(SyncConfig config, DbConnection sourceDb, List<FieldMapping> mappings) {
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

        sql.append(" FROM ").append(config.getSourceTable());

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
     */
    private ObjectNode buildSink(SyncConfig config, DbConnection targetDb, List<FieldMapping> mappings) {
        ObjectNode sink = objectMapper.createObjectNode();
        String pluginName = getJdbcPluginName(targetDb.getDbType());
        sink.put("plugin_name", pluginName);

        String jdbcUrl = DbConnectionServiceImpl.buildJdbcUrl(
                targetDb.getDbType(), targetDb.getHost(), targetDb.getPort(), targetDb.getDatabaseName());
        sink.put("url", jdbcUrl);
        sink.put("driver", getJdbcDriver(targetDb.getDbType()));
        sink.put("user", targetDb.getUsername());
        sink.put("password", targetDb.getPassword());
        sink.put("database", targetDb.getDatabaseName());
        sink.put("table", config.getTargetTable());

        // 自动建表
        sink.put("generate_sink_sql", true);

        // Save Mode 配置
        // 注意: SeaTunnel 2.3.x PostgresCatalog 的自动建表存在 bug，
        // CREATE_SCHEMA_WHEN_NOT_EXIST 和 RECREATE_SCHEMA 均会触发
        // "relation already exists" 错误（dropTable 未生效即执行 CREATE TABLE），
        // 因此统一使用 IGNORE 跳过 Schema 处理，目标表需预先存在。
        sink.put("schema_save_mode", "IGNORE");

        // 全量同步清空目标表后写入，增量同步追加数据
        if ("FULL".equalsIgnoreCase(config.getSyncMode())) {
            sink.put("data_save_mode", "DROP_DATA");
        } else {
            sink.put("data_save_mode", "APPEND_DATA");
        }

        return sink;
    }

    /**
     * 获取 JDBC 连接器插件名
     */
    private String getJdbcPluginName(String dbType) {
        return switch (dbType.toUpperCase()) {
            case "MYSQL" -> "Jdbc";
            case "POSTGRESQL" -> "Jdbc";
            case "ORACLE" -> "Jdbc";
            case "SQLSERVER" -> "Jdbc";
            default -> throw BusinessException.of("不支持的数据库类型: " + dbType);
        };
    }

    /**
     * 获取 JDBC 驱动类名
     */
    private String getJdbcDriver(String dbType) {
        return switch (dbType.toUpperCase()) {
            case "MYSQL" -> "com.mysql.cj.jdbc.Driver";
            case "POSTGRESQL" -> "org.postgresql.Driver";
            case "ORACLE" -> "oracle.jdbc.OracleDriver";
            case "SQLSERVER" -> "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            default -> throw BusinessException.of("不支持的数据库类型: " + dbType);
        };
    }

    /**
     * 判断字段映射中是否存在重命名
     */
    private boolean hasFieldRename(List<FieldMapping> mappings) {
        return mappings.stream()
                .anyMatch(m -> !m.getSourceField().equals(m.getTargetField()));
    }

}
