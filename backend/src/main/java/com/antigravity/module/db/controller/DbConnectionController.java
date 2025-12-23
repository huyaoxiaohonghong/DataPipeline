package com.antigravity.module.db.controller;

import com.antigravity.common.PageResult;
import com.antigravity.common.Result;
import com.antigravity.module.db.dto.*;
import com.antigravity.module.db.entity.DbConnection;
import com.antigravity.module.db.service.DbConnectionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据库连接管理控制器
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/db-connections")
@RequiredArgsConstructor
public class DbConnectionController {

    private final DbConnectionService dbConnectionService;

    /**
     * 分页查询数据库连接列表
     */
    @GetMapping
    public Result<PageResult<DbConnectionVO>> list(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码最小为1") int pageNumber,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小最小为1") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String dbType) {
        PageResult<DbConnection> page = dbConnectionService.pageQuery(pageNumber, pageSize, name, dbType);
        PageResult<DbConnectionVO> voPage = PageResult.of(
                page.getRecords().stream().map(DbConnectionVO::fromEntity).toList(),
                page.getPageNumber(),
                page.getPageSize(),
                page.getTotalRow());
        return Result.success(voPage);
    }

    /**
     * 根据ID获取数据库连接详情
     */
    @GetMapping("/{id}")
    public Result<DbConnectionVO> getById(
            @PathVariable @Min(value = 1, message = "ID必须大于0") Long id) {
        return dbConnectionService.findById(id)
                .map(DbConnectionVO::fromEntity)
                .map(Result::success)
                .orElse(Result.notFound("数据库连接不存在"));
    }

    /**
     * 创建数据库连接
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<DbConnectionVO> create(@RequestBody @Valid DbConnectionCreateRequest request) {
        // 检查名称是否存在
        if (dbConnectionService.existsByName(request.getName())) {
            return Result.badRequest("连接名称已存在");
        }

        DbConnection connection = new DbConnection();
        connection.setName(request.getName());
        connection.setDbType(request.getDbType());
        connection.setHost(request.getHost());
        connection.setPort(request.getPort());
        connection.setDatabaseName(request.getDatabaseName());
        connection.setUsername(request.getUsername());
        connection.setPassword(request.getPassword());
        connection.setDescription(request.getDescription());

        DbConnection created = dbConnectionService.createConnection(connection);
        return Result.success("数据库连接创建成功", DbConnectionVO.fromEntity(created));
    }

    /**
     * 更新数据库连接
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<DbConnectionVO> update(
            @PathVariable @Min(value = 1, message = "ID必须大于0") Long id,
            @RequestBody @Valid DbConnectionUpdateRequest request) {
        DbConnection existing = dbConnectionService.findById(id).orElse(null);
        if (existing == null) {
            return Result.notFound("数据库连接不存在");
        }

        // 检查名称是否冲突
        if (request.getName() != null && !request.getName().equals(existing.getName())
                && dbConnectionService.existsByName(request.getName())) {
            return Result.badRequest("连接名称已存在");
        }

        // 更新字段
        if (request.getName() != null)
            existing.setName(request.getName());
        if (request.getDbType() != null)
            existing.setDbType(request.getDbType());
        if (request.getHost() != null)
            existing.setHost(request.getHost());
        if (request.getPort() != null)
            existing.setPort(request.getPort());
        if (request.getDatabaseName() != null)
            existing.setDatabaseName(request.getDatabaseName());
        if (request.getUsername() != null)
            existing.setUsername(request.getUsername());
        if (request.getPassword() != null)
            existing.setPassword(request.getPassword());
        if (request.getDescription() != null)
            existing.setDescription(request.getDescription());
        if (request.getEnabled() != null)
            existing.setEnabled(request.getEnabled());

        boolean success = dbConnectionService.updateConnection(existing);
        if (success) {
            DbConnection updated = dbConnectionService.findById(id).orElse(null);
            return Result.success("数据库连接更新成功", DbConnectionVO.fromEntity(updated));
        }
        return Result.error("数据库连接更新失败");
    }

    /**
     * 删除数据库连接
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(
            @PathVariable @Min(value = 1, message = "ID必须大于0") Long id) {
        boolean success = dbConnectionService.deleteConnection(id);
        return success ? Result.success("数据库连接删除成功", null) : Result.error("数据库连接删除失败");
    }

    /**
     * 批量删除数据库连接
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> batchDelete(
            @RequestBody @NotEmpty(message = "请选择要删除的连接") List<Long> ids) {
        boolean success = dbConnectionService.deleteConnections(ids);
        return success ? Result.success("批量删除成功", null) : Result.error("批量删除失败");
    }

    /**
     * 测试数据库连接（使用已保存的配置）
     */
    @PostMapping("/{id}/test")
    public Result<DbMonitorInfo> testConnection(
            @PathVariable @Min(value = 1, message = "ID必须大于0") Long id) {
        DbMonitorInfo info = dbConnectionService.testConnection(id);
        if ("CONNECTED".equals(info.getStatus())) {
            return Result.success("连接成功", info);
        }
        return Result.error(400, info.getErrorMessage());
    }

    /**
     * 测试数据库连接（使用临时参数）
     */
    @PostMapping("/test")
    public Result<DbMonitorInfo> testConnection(@RequestBody @Valid DbConnectionTestRequest request) {
        DbMonitorInfo info = dbConnectionService.testConnection(request);
        if ("CONNECTED".equals(info.getStatus())) {
            return Result.success("连接成功", info);
        }
        return Result.error(400, info.getErrorMessage());
    }

    /**
     * 获取数据库监控信息
     */
    @GetMapping("/{id}/monitor")
    public Result<DbMonitorInfo> getMonitorInfo(
            @PathVariable @Min(value = 1, message = "ID必须大于0") Long id) {
        DbMonitorInfo info = dbConnectionService.getMonitorInfo(id);
        if ("CONNECTED".equals(info.getStatus())) {
            return Result.success(info);
        }
        return Result.error(400, info.getErrorMessage());
    }

    /**
     * 获取数据库表列表
     */
    @GetMapping("/{id}/tables")
    public Result<List<String>> listTables(
            @PathVariable @Min(value = 1, message = "ID必须大于0") Long id) {
        List<String> tables = dbConnectionService.listTables(id);
        return Result.success(tables);
    }

    /**
     * 更新连接状态
     */
    @PatchMapping("/{id}/enabled")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateEnabled(
            @PathVariable @Min(value = 1, message = "ID必须大于0") Long id,
            @RequestParam boolean enabled) {
        DbConnection connection = dbConnectionService.findById(id).orElse(null);
        if (connection == null) {
            return Result.notFound("数据库连接不存在");
        }
        connection.setEnabled(enabled);
        boolean success = dbConnectionService.updateConnection(connection);
        return success ? Result.success() : Result.error("更新状态失败");
    }

}
