package com.antigravity.module.sync.controller;

import com.antigravity.common.PageResult;
import com.antigravity.common.Result;
import com.antigravity.module.datasource.entity.DbConnection;
import com.antigravity.module.datasource.service.DbConnectionService;
import com.antigravity.module.sync.dto.FieldMappingDTO;
import com.antigravity.module.sync.dto.SyncConfigCreateRequest;
import com.antigravity.module.sync.dto.SyncConfigUpdateRequest;
import com.antigravity.module.sync.dto.SyncConfigVO;
import com.antigravity.module.sync.dto.SyncTaskVO;
import com.antigravity.module.sync.entity.SyncConfig;
import com.antigravity.module.sync.entity.SyncTask;
import com.antigravity.module.sync.service.SyncConfigService;
import com.antigravity.module.sync.service.SyncTaskService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 同步配置管理 Controller
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/sync/configs")
@RequiredArgsConstructor
public class SyncConfigController {

    private final SyncConfigService syncConfigService;
    private final SyncTaskService syncTaskService;
    private final DbConnectionService dbConnectionService;

    /**
     * 分页查询同步配置
     */
    @GetMapping
    public Result<PageResult<SyncConfigVO>> list(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码最小为 1") int pageNumber,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小最小为 1") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String syncMode) {
        PageResult<SyncConfig> page = syncConfigService.pageQuery(pageNumber, pageSize, name, syncMode);
        PageResult<SyncConfigVO> voPage = PageResult.of(
                page.getRecords().stream().map(this::toVO).toList(),
                page.getPageNumber(),
                page.getPageSize(),
                page.getTotalRow());
        return Result.success(voPage);
    }

    /**
     * 按 ID 查询同步配置（含字段映射）
     */
    @GetMapping("/{id}")
    public Result<SyncConfigVO> getById(@PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id) {
        return syncConfigService.findById(id)
                .map(config -> {
                    SyncConfigVO vo = toVO(config);
                    vo.setFieldMappings(syncConfigService.getFieldMappings(id));
                    return vo;
                })
                .map(Result::success)
                .orElse(Result.notFound("同步配置不存在"));
    }

    /**
     * 创建同步配置
     */
    @PostMapping
    public Result<SyncConfigVO> create(@RequestBody @Valid SyncConfigCreateRequest request) {
        if (syncConfigService.existsByName(request.getName())) {
            return Result.badRequest("配置名称已存在");
        }

        // 验证数据库连接是否存在
        if (dbConnectionService.findById(request.getSourceDbId()).isEmpty()) {
            return Result.badRequest("源数据库连接不存在");
        }
        if (dbConnectionService.findById(request.getTargetDbId()).isEmpty()) {
            return Result.badRequest("目标数据库连接不存在");
        }

        // 增量模式必须指定增量字段
        if ("INCREMENTAL".equals(request.getSyncMode())
                && (request.getIncrementalField() == null || request.getIncrementalField().isBlank())) {
            return Result.badRequest("增量模式必须指定增量字段");
        }

        SyncConfig config = new SyncConfig();
        config.setName(request.getName());
        config.setDescription(request.getDescription());
        config.setSourceDbId(request.getSourceDbId());
        config.setSourceTable(request.getSourceTable());
        config.setTargetDbId(request.getTargetDbId());
        config.setTargetTable(request.getTargetTable());
        config.setSyncMode(request.getSyncMode());
        config.setIncrementalField(request.getIncrementalField());

        SyncConfig created = syncConfigService.createConfig(config, request.getFieldMappings());

        SyncConfigVO vo = toVO(created);
        if (request.getFieldMappings() != null) {
            vo.setFieldMappings(syncConfigService.getFieldMappings(created.getId()));
        }
        return Result.success("创建成功", vo);
    }

    /**
     * 更新同步配置
     */
    @PutMapping("/{id}")
    public Result<SyncConfigVO> update(
            @PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id,
            @RequestBody @Valid SyncConfigUpdateRequest request) {
        SyncConfig existing = syncConfigService.findById(id).orElse(null);
        if (existing == null) {
            return Result.notFound("同步配置不存在");
        }

        if (request.getName() != null && !request.getName().equals(existing.getName())
                && syncConfigService.existsByName(request.getName())) {
            return Result.badRequest("配置名称已存在");
        }

        SyncConfig config = new SyncConfig();
        config.setId(id);
        config.setName(request.getName() != null ? request.getName() : existing.getName());
        config.setDescription(request.getDescription() != null ? request.getDescription() : existing.getDescription());
        config.setSourceDbId(request.getSourceDbId() != null ? request.getSourceDbId() : existing.getSourceDbId());
        config.setSourceTable(request.getSourceTable() != null ? request.getSourceTable() : existing.getSourceTable());
        config.setTargetDbId(request.getTargetDbId() != null ? request.getTargetDbId() : existing.getTargetDbId());
        config.setTargetTable(request.getTargetTable() != null ? request.getTargetTable() : existing.getTargetTable());
        config.setSyncMode(request.getSyncMode() != null ? request.getSyncMode() : existing.getSyncMode());
        config.setIncrementalField(request.getIncrementalField() != null ? request.getIncrementalField() : existing.getIncrementalField());
        config.setEnabled(request.getEnabled() != null ? request.getEnabled() : existing.getEnabled());

        boolean success = syncConfigService.updateConfig(config, request.getFieldMappings());
        if (success) {
            SyncConfigVO vo = toVO(syncConfigService.findById(id).orElse(null));
            vo.setFieldMappings(syncConfigService.getFieldMappings(id));
            return Result.success("更新成功", vo);
        }
        return Result.error("更新失败");
    }

    /**
     * 删除同步配置
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id) {
        boolean success = syncConfigService.deleteConfig(id);
        return success ? Result.success("删除成功", null) : Result.error("删除失败");
    }

    /**
     * 执行同步任务
     */
    @PostMapping("/{id}/execute")
    public Result<SyncTaskVO> execute(@PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id) {
        SyncTask task = syncTaskService.executeSync(id);
        return Result.success("任务已提交", SyncTaskVO.fromEntity(task));
    }

    // ==================== 私有方法 ====================

    /**
     * 将 SyncConfig 实体转换为 VO，并填充数据库连接名称
     */
    private SyncConfigVO toVO(SyncConfig config) {
        SyncConfigVO vo = SyncConfigVO.fromEntity(config);
        if (vo == null) return null;

        dbConnectionService.findById(config.getSourceDbId())
                .map(DbConnection::getName)
                .ifPresent(vo::setSourceDbName);
        dbConnectionService.findById(config.getTargetDbId())
                .map(DbConnection::getName)
                .ifPresent(vo::setTargetDbName);

        return vo;
    }

}
