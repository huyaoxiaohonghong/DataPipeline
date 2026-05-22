package com.antigravity.module.sync.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 同步任务实体
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Data
@TableName("sys_sync_task")
public class SyncTask implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联同步配置ID */
    private Long configId;

    /** 配置名称（冗余） */
    private String configName;

    /** SeaTunnel任务ID */
    private String seatunnelJobId;

    /** 状态：PENDING, RUNNING, FINISHED, FAILED, CANCELED */
    private String status;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 总行数 */
    private Long totalRows;

    /** 已同步行数 */
    private Long syncRows;

    /** 错误信息 */
    private String errorMessage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
