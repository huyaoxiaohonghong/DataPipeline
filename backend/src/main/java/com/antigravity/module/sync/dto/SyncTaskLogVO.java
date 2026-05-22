package com.antigravity.module.sync.dto;

import com.antigravity.module.sync.entity.SyncTaskLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 同步任务日志响应 VO
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncTaskLogVO {

    private Long id;
    private Long taskId;
    private String logLevel;
    private String message;
    private LocalDateTime createTime;

    public static SyncTaskLogVO fromEntity(SyncTaskLog entity) {
        if (entity == null) return null;
        return SyncTaskLogVO.builder()
                .id(entity.getId())
                .taskId(entity.getTaskId())
                .logLevel(entity.getLogLevel())
                .message(entity.getMessage())
                .createTime(entity.getCreateTime())
                .build();
    }

}
