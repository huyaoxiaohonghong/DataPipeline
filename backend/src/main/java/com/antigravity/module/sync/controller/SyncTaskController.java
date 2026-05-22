package com.antigravity.module.sync.controller;

import com.antigravity.common.PageResult;
import com.antigravity.common.Result;
import com.antigravity.module.sync.dto.SyncTaskLogVO;
import com.antigravity.module.sync.dto.SyncTaskVO;
import com.antigravity.module.sync.entity.SyncTask;
import com.antigravity.module.sync.service.SyncTaskService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 同步任务管理 Controller
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/sync/tasks")
@RequiredArgsConstructor
public class SyncTaskController {

    private final SyncTaskService syncTaskService;

    /**
     * 分页查询任务列表
     */
    @GetMapping
    public Result<PageResult<SyncTaskVO>> list(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码最小为 1") int pageNumber,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小最小为 1") int pageSize,
            @RequestParam(required = false) Long configId,
            @RequestParam(required = false) String status) {
        PageResult<SyncTask> page = syncTaskService.pageQuery(pageNumber, pageSize, configId, status);
        PageResult<SyncTaskVO> voPage = PageResult.of(
                page.getRecords().stream().map(SyncTaskVO::fromEntity).toList(),
                page.getPageNumber(),
                page.getPageSize(),
                page.getTotalRow());
        return Result.success(voPage);
    }

    /**
     * 查看任务详情
     */
    @GetMapping("/{id}")
    public Result<SyncTaskVO> getById(@PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id) {
        return syncTaskService.findById(id)
                .map(SyncTaskVO::fromEntity)
                .map(Result::success)
                .orElse(Result.notFound("任务不存在"));
    }

    /**
     * 取消任务
     */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id) {
        boolean success = syncTaskService.cancelTask(id);
        return success ? Result.success("任务已取消", null) : Result.error("取消任务失败");
    }

    /**
     * 刷新任务状态（从 SeaTunnel 同步最新状态）
     */
    @PostMapping("/{id}/refresh")
    public Result<SyncTaskVO> refresh(@PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id) {
        SyncTask task = syncTaskService.refreshTaskStatus(id);
        return Result.success(SyncTaskVO.fromEntity(task));
    }

    /**
     * 查看任务日志
     */
    @GetMapping("/{id}/logs")
    public Result<List<SyncTaskLogVO>> getLogs(@PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id) {
        List<SyncTaskLogVO> logs = syncTaskService.getTaskLogs(id);
        return Result.success(logs);
    }

}
