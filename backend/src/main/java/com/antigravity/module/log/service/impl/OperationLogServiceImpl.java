package com.antigravity.module.log.service.impl;

import com.antigravity.module.log.entity.OperationLog;
import com.antigravity.module.log.mapper.OperationLogMapper;
import com.antigravity.module.log.service.OperationLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog>
        implements OperationLogService {

    @Override
    public Page<OperationLog> getOperationLogPage(int pageNum, int pageSize, String username, String module,
            Integer status, String startTime, String endTime) {
        Page<OperationLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(username), OperationLog::getUsername, username)
                .like(StringUtils.hasText(module), OperationLog::getModule, module)
                .eq(status != null, OperationLog::getStatus, status);

        if (StringUtils.hasText(startTime) && StringUtils.hasText(endTime)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            wrapper.between(OperationLog::getCreateTime, LocalDateTime.parse(startTime, formatter),
                    LocalDateTime.parse(endTime, formatter));
        }

        wrapper.orderByDesc(OperationLog::getCreateTime);
        return this.page(page, wrapper);
    }

    @Async
    @Override
    public void asyncRecordOperationLog(OperationLog log) {
        log.setCreateTime(LocalDateTime.now());
        this.save(log);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        this.removeByIds(ids);
    }

    @Override
    public void cleanAll() {
        this.remove(new LambdaQueryWrapper<>());
    }
}
