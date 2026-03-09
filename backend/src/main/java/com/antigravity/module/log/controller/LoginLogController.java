package com.antigravity.module.log.controller;

import com.antigravity.common.Result;
import com.antigravity.module.log.entity.LoginLog;
import com.antigravity.module.log.service.LoginLogService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/log/login")
@RequiredArgsConstructor
public class LoginLogController {

    private final LoginLogService loginLogService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('log:login:list')")
    public Result<Page<LoginLog>> getLoginLogPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return Result.success(loginLogService.getLoginLogPage(page, size, username, status, startTime, endTime));
    }

    /**
     * 批量删除登录日志
     */
    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('log:login:remove')")
    public Result<Void> deleteByIds(@RequestBody List<Long> ids) {
        loginLogService.deleteByIds(ids);
        return Result.success("删除成功", null);
    }

    /**
     * 清空登录日志
     */
    @DeleteMapping("/clean")
    @PreAuthorize("hasAuthority('log:login:clean')")
    public Result<Void> cleanAll() {
        loginLogService.cleanAll();
        return Result.success("清空成功", null);
    }
}
