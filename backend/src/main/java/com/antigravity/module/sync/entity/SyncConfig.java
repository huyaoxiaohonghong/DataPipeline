package com.antigravity.module.sync.entity;

import com.antigravity.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 同步配置实体
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_sync_config")
public class SyncConfig extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 配置名称 */
    private String name;

    /** 描述 */
    private String description;

    /** 源数据库连接ID */
    private Long sourceDbId;

    /** 源表名 */
    private String sourceTable;

    /** 目标数据库连接ID */
    private Long targetDbId;

    /** 目标表名 */
    private String targetTable;

    /** 同步模式：FULL-全量, INCREMENTAL-增量 */
    private String syncMode;

    /** 增量字段（增量模式使用） */
    private String incrementalField;

    /** 上次增量同步的值 */
    private String lastSyncValue;

    /** 是否启用 */
    private Boolean enabled;

    /** 逻辑删除 */
    @TableLogic
    private Boolean isDeleted;

}
