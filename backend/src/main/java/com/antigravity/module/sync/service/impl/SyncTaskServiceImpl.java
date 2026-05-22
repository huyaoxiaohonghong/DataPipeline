package com.antigravity.module.sync.service.impl;

import com.antigravity.common.BusinessException;
import com.antigravity.common.PageResult;
import com.antigravity.module.datasource.entity.DbConnection;
import com.antigravity.module.datasource.service.DbConnectionService;
import com.antigravity.module.sync.dto.SyncTaskLogVO;
import com.antigravity.module.sync.engine.SeaTunnelClient;
import com.antigravity.module.sync.engine.SeaTunnelConfigBuilder;
import com.antigravity.module.sync.entity.FieldMapping;
import com.antigravity.module.sync.entity.SyncConfig;
import com.antigravity.module.sync.entity.SyncTask;
import com.antigravity.module.sync.entity.SyncTaskLog;
import com.antigravity.module.sync.mapper.FieldMappingMapper;
import com.antigravity.module.sync.mapper.SyncTaskLogMapper;
import com.antigravity.module.sync.mapper.SyncTaskMapper;
import com.antigravity.module.sync.service.SyncConfigService;
import com.antigravity.module.sync.service.SyncTaskService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 同步任务 Service 实现
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SyncTaskServiceImpl extends ServiceImpl<SyncTaskMapper, SyncTask> implements SyncTaskService {

    private final SyncConfigService syncConfigService;
    private final DbConnectionService dbConnectionService;
    private final FieldMappingMapper fieldMappingMapper;
    private final SyncTaskLogMapper syncTaskLogMapper;
    private final SeaTunnelClient seaTunnelClient;
    private final SeaTunnelConfigBuilder configBuilder;

    @Override
    public Optional<SyncTask> findById(Long id) {
        return Optional.ofNullable(this.getById(id));
    }

    @Override
    public PageResult<SyncTask> pageQuery(int pageNumber, int pageSize, Long configId, String status) {
        Page<SyncTask> page = new Page<>(pageNumber, pageSize);
        LambdaQueryWrapper<SyncTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(configId != null, SyncTask::getConfigId, configId)
                .eq(StringUtils.isNotBlank(status), SyncTask::getStatus, status)
                .orderByDesc(SyncTask::getCreateTime);

        Page<SyncTask> resultPage = this.page(page, wrapper);
        return PageResult.of(resultPage.getRecords(), pageNumber, pageSize, resultPage.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SyncTask executeSync(Long configId) {
        // 1. 查询同步配置
        SyncConfig config = syncConfigService.findById(configId)
                .orElseThrow(() -> BusinessException.of("同步配置不存在: " + configId));

        if (!Boolean.TRUE.equals(config.getEnabled())) {
            throw BusinessException.of("同步配置已禁用");
        }

        // 2. 查询数据库连接
        DbConnection sourceDb = dbConnectionService.findById(config.getSourceDbId())
                .orElseThrow(() -> BusinessException.of("源数据库连接不存在: " + config.getSourceDbId()));
        DbConnection targetDb = dbConnectionService.findById(config.getTargetDbId())
                .orElseThrow(() -> BusinessException.of("目标数据库连接不存在: " + config.getTargetDbId()));

        // 3. 查询字段映射
        LambdaQueryWrapper<FieldMapping> mappingWrapper = new LambdaQueryWrapper<>();
        mappingWrapper.eq(FieldMapping::getConfigId, configId)
                .orderByAsc(FieldMapping::getSortOrder);
        List<FieldMapping> mappings = fieldMappingMapper.selectList(mappingWrapper);

        // 4. 创建任务记录
        SyncTask task = new SyncTask();
        task.setConfigId(configId);
        task.setConfigName(config.getName());
        task.setStatus("PENDING");
        task.setTotalRows(0L);
        task.setSyncRows(0L);
        this.save(task);

        // 5. 记录日志
        saveTaskLog(task.getId(), "INFO", "任务创建成功，准备提交到 SeaTunnel");

        // 6. 异步提交任务
        submitToSeaTunnel(task.getId(), config, sourceDb, targetDb, mappings);

        return task;
    }

    /**
     * 异步提交任务到 SeaTunnel
     */
    @Async
    public void submitToSeaTunnel(Long taskId, SyncConfig config, DbConnection sourceDb,
                                   DbConnection targetDb, List<FieldMapping> mappings) {
        try {
            // 构建配置
            String jobConfig = configBuilder.buildJobConfig(config, sourceDb, targetDb, mappings);
            saveTaskLog(taskId, "INFO", "SeaTunnel Job 配置构建完成");

            // 更新状态为 RUNNING
            updateTaskStatus(taskId, "RUNNING", null);
            updateTaskStartTime(taskId);

            // 提交任务
            String jobId = seaTunnelClient.submitJob(jobConfig);

            // 更新 SeaTunnel 任务 ID
            LambdaUpdateWrapper<SyncTask> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SyncTask::getId, taskId)
                    .set(SyncTask::getSeatunnelJobId, jobId);
            this.update(updateWrapper);

            saveTaskLog(taskId, "INFO", "任务已提交到 SeaTunnel，jobId: " + jobId);
            log.info("同步任务已提交: taskId={}, seatunnelJobId={}", taskId, jobId);

        } catch (Exception e) {
            log.error("提交同步任务失败: taskId={}, error={}", taskId, e.getMessage());
            updateTaskStatus(taskId, "FAILED", e.getMessage());
            updateTaskEndTime(taskId);
            saveTaskLog(taskId, "ERROR", "任务提交失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelTask(Long taskId) {
        SyncTask task = this.getById(taskId);
        if (task == null) {
            throw BusinessException.of("任务不存在");
        }

        // 只有 PENDING 或 RUNNING 状态的任务可以取消
        if (!"PENDING".equals(task.getStatus()) && !"RUNNING".equals(task.getStatus())) {
            throw BusinessException.of("当前任务状态不允许取消: " + task.getStatus());
        }

        // 如果已提交到 SeaTunnel，尝试停止
        if (task.getSeatunnelJobId() != null && !task.getSeatunnelJobId().isEmpty()) {
            boolean stopped = seaTunnelClient.stopJob(task.getSeatunnelJobId());
            if (!stopped) {
                log.warn("SeaTunnel 任务停止失败，但仍将更新本地状态: jobId={}", task.getSeatunnelJobId());
            }
        }

        updateTaskStatus(taskId, "CANCELED", "用户手动取消");
        updateTaskEndTime(taskId);
        saveTaskLog(taskId, "WARN", "任务已被用户取消");

        log.info("取消同步任务: taskId={}", taskId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SyncTask refreshTaskStatus(Long taskId) {
        SyncTask task = this.getById(taskId);
        if (task == null) {
            throw BusinessException.of("任务不存在");
        }

        // 只刷新 RUNNING 状态的任务
        if (!"RUNNING".equals(task.getStatus())) {
            return task;
        }

        // 如果没有 SeaTunnel Job ID，无法刷新
        if (task.getSeatunnelJobId() == null || task.getSeatunnelJobId().isEmpty()) {
            return task;
        }

        // 从 SeaTunnel 查询状态
        Map<String, Object> jobInfo = seaTunnelClient.getJobInfo(task.getSeatunnelJobId());
        if (jobInfo.isEmpty()) {
            // 任务可能已完成（不在 running-jobs 中了）
            updateTaskStatus(taskId, "FINISHED", null);
            updateTaskEndTime(taskId);
            saveTaskLog(taskId, "INFO", "任务已完成（SeaTunnel 中已无运行记录）");
        } else {
            // 更新行数等信息
            Object jobStatus = jobInfo.get("jobStatus");
            if (jobStatus != null) {
                String statusStr = jobStatus.toString();
                if ("FINISHED".equalsIgnoreCase(statusStr)) {
                    updateTaskStatus(taskId, "FINISHED", null);
                    updateTaskEndTime(taskId);
                    saveTaskLog(taskId, "INFO", "任务已完成");
                } else if ("FAILED".equalsIgnoreCase(statusStr)) {
                    String errMsg = jobInfo.getOrDefault("errorMsg", "未知错误").toString();
                    updateTaskStatus(taskId, "FAILED", errMsg);
                    updateTaskEndTime(taskId);
                    saveTaskLog(taskId, "ERROR", "任务执行失败: " + errMsg);
                } else if ("CANCELED".equalsIgnoreCase(statusStr) || "CANCELLED".equalsIgnoreCase(statusStr)) {
                    updateTaskStatus(taskId, "CANCELED", null);
                    updateTaskEndTime(taskId);
                    saveTaskLog(taskId, "WARN", "任务已被 SeaTunnel 取消");
                }
            }

            // 尝试更新行数
            Object readRowCount = jobInfo.get("readRowCount");
            Object writeRowCount = jobInfo.get("writeRowCount");
            if (readRowCount != null || writeRowCount != null) {
                LambdaUpdateWrapper<SyncTask> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(SyncTask::getId, taskId);
                if (readRowCount != null) {
                    wrapper.set(SyncTask::getTotalRows, Long.parseLong(readRowCount.toString()));
                }
                if (writeRowCount != null) {
                    wrapper.set(SyncTask::getSyncRows, Long.parseLong(writeRowCount.toString()));
                }
                this.update(wrapper);
            }
        }

        return this.getById(taskId);
    }

    @Override
    public List<SyncTaskLogVO> getTaskLogs(Long taskId) {
        LambdaQueryWrapper<SyncTaskLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SyncTaskLog::getTaskId, taskId)
                .orderByAsc(SyncTaskLog::getCreateTime);
        List<SyncTaskLog> logs = syncTaskLogMapper.selectList(wrapper);
        if (logs == null || logs.isEmpty()) {
            return Collections.emptyList();
        }
        return logs.stream().map(SyncTaskLogVO::fromEntity).toList();
    }

    // ==================== 私有方法 ====================

    private void updateTaskStatus(Long taskId, String status, String errorMessage) {
        LambdaUpdateWrapper<SyncTask> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SyncTask::getId, taskId)
                .set(SyncTask::getStatus, status)
                .set(SyncTask::getErrorMessage, errorMessage);
        this.update(wrapper);
    }

    private void updateTaskStartTime(Long taskId) {
        LambdaUpdateWrapper<SyncTask> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SyncTask::getId, taskId)
                .set(SyncTask::getStartTime, LocalDateTime.now());
        this.update(wrapper);
    }

    private void updateTaskEndTime(Long taskId) {
        LambdaUpdateWrapper<SyncTask> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SyncTask::getId, taskId)
                .set(SyncTask::getEndTime, LocalDateTime.now());
        this.update(wrapper);
    }

    private void saveTaskLog(Long taskId, String level, String message) {
        SyncTaskLog taskLog = new SyncTaskLog();
        taskLog.setTaskId(taskId);
        taskLog.setLogLevel(level);
        taskLog.setMessage(message);
        taskLog.setCreateTime(LocalDateTime.now());
        syncTaskLogMapper.insert(taskLog);
    }

}
