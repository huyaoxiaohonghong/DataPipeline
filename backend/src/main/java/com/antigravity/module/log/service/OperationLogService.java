package com.antigravity.module.log.service;

import com.antigravity.module.log.entity.OperationLog;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface OperationLogService extends IService<OperationLog> {
    Page<OperationLog> getOperationLogPage(int page, int size, String username, String module, Integer status,
            String startTime, String endTime);

    void asyncRecordOperationLog(OperationLog log);

    /**
     * 批量删除操作日志
     */
    void deleteByIds(List<Long> ids);

    /**
     * 清空所有操作日志
     */
    void cleanAll();
}
