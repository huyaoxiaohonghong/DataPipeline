package com.antigravity.module.datasource.service.impl;

import com.antigravity.common.BusinessException;
import com.antigravity.module.sync.engine.dialect.DatabaseDialect;
import com.antigravity.module.sync.engine.dialect.DatabaseDialectFactory;
import com.antigravity.common.PageResult;
import com.antigravity.module.datasource.entity.DbConnection;
import com.antigravity.module.datasource.mapper.DbConnectionMapper;
import com.antigravity.module.datasource.service.DbConnectionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.*;

/**
 * 数据库连接 Service 实现
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DbConnectionServiceImpl extends ServiceImpl<DbConnectionMapper, DbConnection> implements DbConnectionService {

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
    public PageResult<DbConnection> pageQuery(int pageNumber, int pageSize, String name, String dbType) {
        Page<DbConnection> page = new Page<>(pageNumber, pageSize);
        LambdaQueryWrapper<DbConnection> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(name), DbConnection::getName, name)
                .eq(StringUtils.isNotBlank(dbType), DbConnection::getDbType, dbType)
                .orderByDesc(DbConnection::getCreateTime);

        Page<DbConnection> resultPage = this.page(page, wrapper);
        return PageResult.of(resultPage.getRecords(), pageNumber, pageSize, resultPage.getTotal());
    }

    @Override
    public boolean existsByName(String name) {
        LambdaQueryWrapper<DbConnection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DbConnection::getName, name);
        return this.count(wrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DbConnection createConnection(DbConnection connection) {
        if (connection.getEnabled() == null) {
            connection.setEnabled(true);
        }
        connection.setIsDeleted(false);
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
    public boolean testConnection(String dbType, String host, int port, String databaseName, String username, String password) {
        String jdbcUrl = buildJdbcUrl(dbType, host, port, databaseName);
        log.info("测试数据库连接: type={}, url={}", dbType, jdbcUrl);

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            return conn.isValid(5);
        } catch (SQLException e) {
            log.warn("数据库连接测试失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> getTables(Long connectionId) {
        DbConnection conn = this.getById(connectionId);
        if (conn == null) {
            throw BusinessException.of("数据库连接不存在");
        }

        String jdbcUrl = buildJdbcUrl(conn.getDbType(), conn.getHost(), conn.getPort(), conn.getDatabaseName());
        List<String> tables = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(jdbcUrl, conn.getUsername(), conn.getPassword())) {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            String schema = getSchema(conn.getDbType(), conn.getDatabaseName());

            try (ResultSet rs = metaData.getTables(catalog, schema, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }
        } catch (SQLException e) {
            log.error("获取表列表失败: {}", e.getMessage());
            throw BusinessException.of("获取表列表失败: " + e.getMessage());
        }

        return tables;
    }

    @Override
    public List<Map<String, String>> getTableColumns(Long connectionId, String tableName) {
        DbConnection conn = this.getById(connectionId);
        if (conn == null) {
            throw BusinessException.of("数据库连接不存在");
        }

        String jdbcUrl = buildJdbcUrl(conn.getDbType(), conn.getHost(), conn.getPort(), conn.getDatabaseName());
        List<Map<String, String>> columns = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(jdbcUrl, conn.getUsername(), conn.getPassword())) {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            String schema = getSchema(conn.getDbType(), conn.getDatabaseName());

            try (ResultSet rs = metaData.getColumns(catalog, schema, tableName, "%")) {
                while (rs.next()) {
                    Map<String, String> col = new LinkedHashMap<>();
                    String columnName = rs.getString("COLUMN_NAME");
                    String dataType = rs.getString("TYPE_NAME");
                    String comment = rs.getString("REMARKS");
                    
                    col.put("name", columnName);
                    col.put("columnName", columnName);
                    col.put("type", dataType);
                    col.put("dataType", dataType);
                    col.put("size", rs.getString("COLUMN_SIZE"));
                    col.put("nullable", rs.getString("IS_NULLABLE"));
                    col.put("comment", comment);
                    col.put("columnComment", comment);
                    
                    columns.add(col);
                }
            }
        } catch (SQLException e) {
            log.error("获取列信息失败: {}", e.getMessage());
            throw BusinessException.of("获取列信息失败: " + e.getMessage());
        }

        return columns;
    }

    @Override
    public List<String> getSupportedDbTypes() {
        return DatabaseDialectFactory.getSupportedTypes();
    }

    // ==================== 私有方法 ====================

    /**
     * 根据数据库类型构建 JDBC URL
     * <p>
     * 委托给 {@link DatabaseDialect} 方言实现，消除重复的 switch 逻辑。
     */
    public static String buildJdbcUrl(String dbType, String host, int port, String databaseName) {
        DatabaseDialect dialect = DatabaseDialectFactory.getDialect(dbType);
        return dialect.buildJdbcUrl(host, port, databaseName);
    }

    /**
     * 获取 Schema（不同数据库的 schema 取法不同）
     * <p>
     * 委托给 {@link DatabaseDialect} 方言实现。
     */
    private String getSchema(String dbType, String databaseName) {
        DatabaseDialect dialect = DatabaseDialectFactory.getDialect(dbType);
        return dialect.getDefaultSchema(databaseName);
    }

}
