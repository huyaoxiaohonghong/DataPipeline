package com.antigravity.module.sync.dto;

import com.antigravity.module.sync.entity.SyncConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 同步配置响应 VO
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncConfigVO {

    private Long id;
    private String name;
    private String description;
    private Long sourceDbId;
    private String sourceDbName;
    private String sourceTable;
    private Long targetDbId;
    private String targetDbName;
    private String targetTable;
    private String syncMode;
    private String incrementalField;
    private String lastSyncValue;
    private Boolean enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 字段映射列表 */
    private List<FieldMappingDTO> fieldMappings;

    /**
     * 从实体转换（不含字段映射和数据库连接名称）
     */
    public static SyncConfigVO fromEntity(SyncConfig entity) {
        if (entity == null) return null;
        return SyncConfigVO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .sourceDbId(entity.getSourceDbId())
                .sourceTable(entity.getSourceTable())
                .targetDbId(entity.getTargetDbId())
                .targetTable(entity.getTargetTable())
                .syncMode(entity.getSyncMode())
                .incrementalField(entity.getIncrementalField())
                .lastSyncValue(entity.getLastSyncValue())
                .enabled(entity.getEnabled())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

}
