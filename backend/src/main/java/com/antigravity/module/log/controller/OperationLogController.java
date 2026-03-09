package com.antigravity.module.log.controller;

import com.antigravity.common.Result;
import com.antigravity.module.log.entity.OperationLog;
import com.antigravity.module.log.service.OperationLogService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/log/operation")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('log:operation:list')")
    public Result<Page<OperationLog>> getOperationLogPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return Result.success(
                operationLogService.getOperationLogPage(page, size, username, module, status, startTime, endTime));
    }

    /**
     * 批量删除操作日志
     */
    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('log:operation:remove')")
    public Result<Void> deleteByIds(@RequestBody List<Long> ids) {
        operationLogService.deleteByIds(ids);
        return Result.success("删除成功", null);
    }

    /**
     * 清空操作日志
     */
    @DeleteMapping("/clean")
    @PreAuthorize("hasAuthority('log:operation:clean')")
    public Result<Void> cleanAll() {
        operationLogService.cleanAll();
        return Result.success("清空成功", null);
    }
}
