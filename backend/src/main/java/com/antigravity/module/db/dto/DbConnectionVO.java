package com.antigravity.module.db.dto;

import com.antigravity.module.db.entity.DbConnection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 数据库连接 VO
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DbConnectionVO {

    private Long id;
    private String name;
    private String dbType;
    private String host;
    private Integer port;
    private String databaseName;
    private String username;
    private String description;
    private Boolean enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static DbConnectionVO fromEntity(DbConnection entity) {
        if (entity == null)
            return null;
        return DbConnectionVO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .dbType(entity.getDbType())
                .host(entity.getHost())
                .port(entity.getPort())
                .databaseName(entity.getDatabaseName())
                .username(entity.getUsername())
                .description(entity.getDescription())
                .enabled(entity.getEnabled())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

}
