package com.antigravity.module.log.service;

import com.antigravity.module.log.entity.LoginLog;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface LoginLogService extends IService<LoginLog> {
    Page<LoginLog> getLoginLogPage(int page, int size, String username, Integer status, String startTime,
            String endTime);

    void asyncRecordLoginLog(String username, String ip, String userAgent, boolean success, String message);

    /**
     * 批量删除登录日志
     */
    void deleteByIds(List<Long> ids);

    /**
     * 清空所有登录日志
     */
    void cleanAll();
}
