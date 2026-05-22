package com.antigravity.module.datasource.service;

import com.antigravity.common.PageResult;
import com.antigravity.module.datasource.entity.DbConnection;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 数据库连接 Service 接口
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
public interface DbConnectionService extends IService<DbConnection> {

    Optional<DbConnection> findById(Long id);

    Optional<DbConnection> findByName(String name);

    PageResult<DbConnection> pageQuery(int pageNumber, int pageSize, String name, String dbType);

    boolean existsByName(String name);

    DbConnection createConnection(DbConnection connection);

    boolean updateConnection(DbConnection connection);

    boolean deleteConnection(Long id);

    /**
     * 测试数据库连接
     *
     * @param dbType       数据库类型
     * @param host         主机地址
     * @param port         端口
     * @param databaseName 数据库名
     * @param username     用户名
     * @param password     密码
     * @return 连接成功返回 true
     */
    boolean testConnection(String dbType, String host, int port, String databaseName, String username, String password);

    /**
     * 获取指定连接下的所有表名
     *
     * @param connectionId 连接ID
     * @return 表名列表
     */
    List<String> getTables(Long connectionId);

    /**
     * 获取指定表的列信息
     *
     * @param connectionId 连接ID
     * @param tableName    表名
     * @return 列信息列表（每列包含 name、type、comment 等）
     */
    List<Map<String, String>> getTableColumns(Long connectionId, String tableName);

    /**
     * 获取支持的数据库类型列表
     */
    List<String> getSupportedDbTypes();

}
