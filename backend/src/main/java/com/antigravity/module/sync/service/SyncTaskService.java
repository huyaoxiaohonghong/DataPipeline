package com.antigravity.module.sync.service;

import com.antigravity.common.PageResult;
import com.antigravity.module.sync.dto.SyncTaskLogVO;
import com.antigravity.module.sync.entity.SyncTask;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Optional;

/**
 * 同步任务 Service 接口
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
public interface SyncTaskService extends IService<SyncTask> {

    /**
     * 按 ID 查找
     */
    Optional<SyncTask> findById(Long id);

    /**
     * 分页查询任务列表
     */
    PageResult<SyncTask> pageQuery(int pageNumber, int pageSize, Long configId, String status);

    /**
     * 执行同步任务
     *
     * @param configId 同步配置ID
     * @return 创建的任务
     */
    SyncTask executeSync(Long configId);

    /**
     * 取消任务
     *
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean cancelTask(Long taskId);

    /**
     * 从 SeaTunnel 刷新任务状态
     *
     * @param taskId 任务ID
     * @return 更新后的任务
     */
    SyncTask refreshTaskStatus(Long taskId);

    /**
     * 获取任务日志
     *
     * @param taskId 任务ID
     * @return 日志列表
     */
    List<SyncTaskLogVO> getTaskLogs(Long taskId);

}
