package com.antigravity.module.sync.dto;

import com.antigravity.module.sync.entity.SyncTask;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 同步任务响应 VO
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncTaskVO {

    private Long id;
    private Long configId;
    private String configName;
    private String seatunnelJobId;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long totalRows;
    private Long syncRows;
    private String errorMessage;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 计算属性：耗时（秒） */
    private Long durationSeconds;

    public static SyncTaskVO fromEntity(SyncTask entity) {
        if (entity == null) return null;

        Long duration = null;
        if (entity.getStartTime() != null && entity.getEndTime() != null) {
            duration = java.time.Duration.between(entity.getStartTime(), entity.getEndTime()).getSeconds();
        }

        return SyncTaskVO.builder()
                .id(entity.getId())
                .configId(entity.getConfigId())
                .configName(entity.getConfigName())
                .seatunnelJobId(entity.getSeatunnelJobId())
                .status(entity.getStatus())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .totalRows(entity.getTotalRows())
                .syncRows(entity.getSyncRows())
                .errorMessage(entity.getErrorMessage())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .durationSeconds(duration)
                .build();
    }

}
