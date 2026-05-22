package com.antigravity.module.sync.mapper;

import com.antigravity.module.sync.entity.SyncTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 同步任务 Mapper
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Mapper
public interface SyncTaskMapper extends BaseMapper<SyncTask> {

}
