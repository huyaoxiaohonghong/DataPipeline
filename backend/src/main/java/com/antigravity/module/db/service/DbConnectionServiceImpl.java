package com.antigravity.module.db.service;

import com.antigravity.common.PageResult;
import com.antigravity.module.db.dto.DbConnectionTestRequest;
import com.antigravity.module.db.dto.DbMonitorInfo;
import com.antigravity.module.db.entity.DbConnection;
import com.antigravity.module.db.mapper.DbConnectionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 数据库连接服务实现
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class DbConnectionServiceImpl extends ServiceImpl<DbConnectionMapper, DbConnection>
        implements DbConnectionService {

    @Override
    public Optional<DbConnection> findById(Long id) {
        return Optional.ofNullable(this.getById(id));
    }

    @Override
    public Optional<DbConnection> findByName(String name) {
        LambdaQueryWrapper<DbConnection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DbConnection::getName, name);
        return Optional.ofNullable(this.getOne(wrapper));
    }

    @Override
    public boolean existsByName(String name) {
        LambdaQueryWrapper<DbConnection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DbConnection::getName, name);
        return this.count(wrapper) > 0;
    }

    @Override
    public PageResult<DbConnection> pageQuery(int pageNumber, int pageSize, String name, String dbType) {
        LambdaQueryWrapper<DbConnection> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isBlank()) {
            wrapper.like(DbConnection::getName, name);
        }
        if (dbType != null && !dbType.isBlank()) {
            wrapper.eq(DbConnection::getDbType, dbType);
        }
        wrapper.orderByDesc(DbConnection::getCreateTime);

        Page<DbConnection> page = this.page(new Page<>(pageNumber, pageSize), wrapper);
        return PageResult.of(page.getRecords(), pageNumber, pageSize, page.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DbConnection createConnection(DbConnection connection) {
        connection.setEnabled(true);
        this.save(connection);
        return connection;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateConnection(DbConnection connection) {
        return this.updateById(connection);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteConnection(Long id) {
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteConnections(List<Long> ids) {
        return this.removeByIds(ids);
    }

    @Override
    public DbMonitorInfo testConnection(Long id) {
        DbConnection connection = this.getById(id);
        if (connection == null) {
            return DbMonitorInfo.builder()
                    .status("ERROR")
                    .errorMessage("连接配置不存在")
                    .build();
        }
        return doTestConnection(
                connection.getDbType(),
                connection.getHost(),
                connection.getPort(),
                connection.getDatabaseName(),
                connection.getUsername(),
                connection.getPassword());
    }

    @Override
    public DbMonitorInfo testConnection(DbConnectionTestRequest request) {
        return doTestConnection(
                request.getDbType(),
                request.getHost(),
                request.getPort(),
                request.getDatabaseName(),
                request.getUsername(),
                request.getPassword());
    }

    @Override
    public DbMonitorInfo getMonitorInfo(Long id) {
        DbConnection connection = this.getById(id);
        if (connection == null) {
            return DbMonitorInfo.builder()
                    .status("ERROR")
                    .errorMessage("连接配置不存在")
                    .build();
        }

        long startTime = System.currentTimeMillis();
        String jdbcUrl = buildJdbcUrl(connection.getDbType(), connection.getHost(),
                connection.getPort(), connection.getDatabaseName());

        try (Connection conn = DriverManager.getConnection(jdbcUrl, connection.getUsername(),
                connection.getPassword())) {
            long responseTime = System.currentTimeMillis() - startTime;

            DbMonitorInfo.DbMonitorInfoBuilder builder = DbMonitorInfo.builder()
                    .status("CONNECTED")
                    .responseTimeMs(responseTime);

            // 获取数据库版本
            DatabaseMetaData metaData = conn.getMetaData();
            builder.version(metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion());

            // 获取表列表
            List<String> tables = getTableList(conn, connection.getDatabaseName());
            builder.tables(tables);
            builder.tableCount(tables.size());

            // 获取数据库特定信息
            addDbSpecificInfo(conn, connection.getDbType(), connection.getDatabaseName(), builder);

            return builder.build();
        } catch (SQLException e) {
            log.error("获取数据库监控信息失败: {}", e.getMessage());
            return DbMonitorInfo.builder()
                    .status("ERROR")
                    .errorMessage(e.getMessage())
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    @Override
    public List<String> listTables(Long id) {
        DbConnection connection = this.getById(id);
        if (connection == null) {
            return List.of();
        }

        String jdbcUrl = buildJdbcUrl(connection.getDbType(), connection.getHost(),
                connection.getPort(), connection.getDatabaseName());

        try (Connection conn = DriverManager.getConnection(jdbcUrl, connection.getUsername(),
                connection.getPassword())) {
            return getTableList(conn, connection.getDatabaseName());
        } catch (SQLException e) {
            log.error("获取表列表失败: {}", e.getMessage());
            return List.of();
        }
    }

    private DbMonitorInfo doTestConnection(String dbType, String host, Integer port,
            String databaseName, String username, String password) {
        long startTime = System.currentTimeMillis();
        String jdbcUrl = buildJdbcUrl(dbType, host, port, databaseName);

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            long responseTime = System.currentTimeMillis() - startTime;
            DatabaseMetaData metaData = conn.getMetaData();

            return DbMonitorInfo.builder()
                    .status("CONNECTED")
                    .version(metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion())
                    .responseTimeMs(responseTime)
                    .build();
        } catch (SQLException e) {
            log.error("测试连接失败: {}", e.getMessage());
            return DbMonitorInfo.builder()
                    .status("ERROR")
                    .errorMessage(e.getMessage())
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    private String buildJdbcUrl(String dbType, String host, Integer port, String databaseName) {
        return switch (dbType.toUpperCase()) {
            case "MYSQL" -> String.format(
                    "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true",
                    host, port, databaseName);
            case "POSTGRESQL" -> String.format(
                    "jdbc:postgresql://%s:%d/%s",
                    host, port, databaseName);
            case "ORACLE" -> String.format(
                    "jdbc:oracle:thin:@%s:%d:%s",
                    host, port, databaseName);
            case "SQLSERVER" -> String.format(
                    "jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=false",
                    host, port, databaseName);
            default -> throw new IllegalArgumentException("不支持的数据库类型: " + dbType);
        };
    }

    private List<String> getTableList(Connection conn, String databaseName) throws SQLException {
        List<String> tables = new ArrayList<>();
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet rs = metaData.getTables(databaseName, null, "%", new String[] { "TABLE" })) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        }
        return tables;
    }

    private void addDbSpecificInfo(Connection conn, String dbType, String databaseName,
            DbMonitorInfo.DbMonitorInfoBuilder builder) {
        try {
            if ("MYSQL".equalsIgnoreCase(dbType)) {
                // 获取MySQL连接数
                try (Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery("SHOW STATUS LIKE 'Threads_connected'")) {
                    if (rs.next()) {
                        builder.activeConnections(rs.getInt("Value"));
                    }
                }
                try (Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery("SHOW VARIABLES LIKE 'max_connections'")) {
                    if (rs.next()) {
                        builder.maxConnections(rs.getInt("Value"));
                    }
                }
                // 获取数据库大小
                try (Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery(
                                "SELECT SUM(data_length + index_length) / 1024 / 1024 AS size_mb " +
                                        "FROM information_schema.tables WHERE table_schema = '" + databaseName + "'")) {
                    if (rs.next()) {
                        builder.databaseSizeMB(rs.getDouble("size_mb"));
                    }
                }
            }
        } catch (SQLException e) {
            log.warn("获取数据库特定信息失败: {}", e.getMessage());
        }
    }

}
