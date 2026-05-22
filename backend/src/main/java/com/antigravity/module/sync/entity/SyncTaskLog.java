package com.antigravity.module.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 同步任务日志实体
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Data
@TableName("sys_sync_task_log")
public class SyncTaskLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联任务ID */
    private Long taskId;

    /** 日志级别：INFO, WARN, ERROR */
    private String logLevel;

    /** 日志内容 */
    private String message;

    private LocalDateTime createTime;

}
