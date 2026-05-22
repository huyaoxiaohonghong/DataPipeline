package com.antigravity.module.sync.mapper;

import com.antigravity.module.sync.entity.SyncTaskLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 同步任务日志 Mapper
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Mapper
public interface SyncTaskLogMapper extends BaseMapper<SyncTaskLog> {

}
