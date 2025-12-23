package com.antigravity.module.db.service;

import com.antigravity.common.PageResult;
import com.antigravity.module.db.dto.DbConnectionTestRequest;
import com.antigravity.module.db.dto.DbMonitorInfo;
import com.antigravity.module.db.entity.DbConnection;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Optional;

/**
 * 数据库连接服务接口
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
public interface DbConnectionService extends IService<DbConnection> {

    /**
     * 根据ID查询
     */
    Optional<DbConnection> findById(Long id);

    /**
     * 根据名称查询
     */
    Optional<DbConnection> findByName(String name);

    /**
     * 检查名称是否存在
     */
    boolean existsByName(String name);

    /**
     * 分页查询
     */
    PageResult<DbConnection> pageQuery(int pageNumber, int pageSize, String name, String dbType);

    /**
     * 创建连接
     */
    DbConnection createConnection(DbConnection connection);

    /**
     * 更新连接
     */
    boolean updateConnection(DbConnection connection);

    /**
     * 删除连接
     */
    boolean deleteConnection(Long id);

    /**
     * 批量删除
     */
    boolean deleteConnections(List<Long> ids);

    /**
     * 测试连接（使用已保存的连接）
     */
    DbMonitorInfo testConnection(Long id);

    /**
     * 测试连接（使用临时参数）
     */
    DbMonitorInfo testConnection(DbConnectionTestRequest request);

    /**
     * 获取监控信息
     */
    DbMonitorInfo getMonitorInfo(Long id);

    /**
     * 获取表列表
     */
    List<String> listTables(Long id);

}
