package com.antigravity.module.datasource.controller;

import com.antigravity.common.PageResult;
import com.antigravity.common.Result;
import com.antigravity.module.datasource.dto.ConnectionTestRequest;
import com.antigravity.module.datasource.dto.DbConnectionCreateRequest;
import com.antigravity.module.datasource.dto.DbConnectionUpdateRequest;
import com.antigravity.module.datasource.dto.DbConnectionVO;
import com.antigravity.module.datasource.entity.DbConnection;
import com.antigravity.module.datasource.service.DbConnectionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据库连接管理 Controller
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/datasources")
@RequiredArgsConstructor
public class DbConnectionController {

    private final DbConnectionService dbConnectionService;

    /**
     * 分页查询数据库连接
     */
    @GetMapping
    public Result<PageResult<DbConnectionVO>> list(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码最小为 1") int pageNumber,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小最小为 1") int pageSize,
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
     * 按 ID 查询数据库连接
     */
    @GetMapping("/{id}")
    public Result<DbConnectionVO> getById(@PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id) {
        return dbConnectionService.findById(id)
                .map(DbConnectionVO::fromEntity)
                .map(Result::success)
                .orElse(Result.notFound("数据库连接不存在"));
    }

    /**
     * 创建数据库连接
     */
    @PostMapping
    public Result<DbConnectionVO> create(@RequestBody @Valid DbConnectionCreateRequest request) {
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
        return Result.success("创建成功", DbConnectionVO.fromEntity(created));
    }

    /**
     * 更新数据库连接
     */
    @PutMapping("/{id}")
    public Result<DbConnectionVO> update(
            @PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id,
            @RequestBody @Valid DbConnectionUpdateRequest request) {
        DbConnection existing = dbConnectionService.findById(id).orElse(null);
        if (existing == null) {
            return Result.notFound("数据库连接不存在");
        }

        if (request.getName() != null && !request.getName().equals(existing.getName())
                && dbConnectionService.existsByName(request.getName())) {
            return Result.badRequest("连接名称已存在");
        }

        DbConnection connection = new DbConnection();
        connection.setId(id);
        connection.setName(request.getName() != null ? request.getName() : existing.getName());
        connection.setDbType(request.getDbType() != null ? request.getDbType() : existing.getDbType());
        connection.setHost(request.getHost() != null ? request.getHost() : existing.getHost());
        connection.setPort(request.getPort() != null ? request.getPort() : existing.getPort());
        connection.setDatabaseName(request.getDatabaseName() != null ? request.getDatabaseName() : existing.getDatabaseName());
        connection.setUsername(request.getUsername() != null ? request.getUsername() : existing.getUsername());
        connection.setPassword(request.getPassword() != null ? request.getPassword() : existing.getPassword());
        connection.setDescription(request.getDescription() != null ? request.getDescription() : existing.getDescription());
        connection.setEnabled(request.getEnabled() != null ? request.getEnabled() : existing.getEnabled());

        boolean success = dbConnectionService.updateConnection(connection);
        if (success) {
            DbConnection updated = dbConnectionService.findById(id).orElse(null);
            return Result.success("更新成功", DbConnectionVO.fromEntity(updated));
        }
        return Result.error("更新失败");
    }

    /**
     * 删除数据库连接
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id) {
        boolean success = dbConnectionService.deleteConnection(id);
        return success ? Result.success("删除成功", null) : Result.error("删除失败");
    }

    /**
     * 测试数据库连接
     */
    @PostMapping("/test")
    public Result<Boolean> testConnection(@RequestBody @Valid ConnectionTestRequest request) {
        boolean success = dbConnectionService.testConnection(
                request.getDbType(), request.getHost(), request.getPort(),
                request.getDatabaseName(), request.getUsername(), request.getPassword());
        return success ? Result.success("连接成功", true) : Result.error(400, "连接失败");
    }

    /**
     * 获取支持的数据库类型
     */
    @GetMapping("/types")
    public Result<List<String>> getSupportedTypes() {
        return Result.success(dbConnectionService.getSupportedDbTypes());
    }

    /**
     * 获取指定连接下的所有表名
     */
    @GetMapping("/{id}/tables")
    public Result<List<String>> getTables(@PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id) {
        List<String> tables = dbConnectionService.getTables(id);
        return Result.success(tables);
    }

    /**
     * 获取指定表的列信息
     */
    @GetMapping("/{id}/tables/{tableName}/columns")
    public Result<List<Map<String, String>>> getTableColumns(
            @PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id,
            @PathVariable String tableName) {
        List<Map<String, String>> columns = dbConnectionService.getTableColumns(id, tableName);
        return Result.success(columns);
    }

}
