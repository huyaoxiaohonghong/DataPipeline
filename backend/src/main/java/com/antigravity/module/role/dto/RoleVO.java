package com.antigravity.module.role.dto;

import com.antigravity.module.role.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 角色视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleVO {

    private Long id;

    private String code;

    private String name;

    private String description;

    private Integer sort;

    private Boolean enabled;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public static RoleVO fromEntity(Role role) {
        if (role == null)
            return null;
        return RoleVO.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .sort(role.getSort())
                .enabled(role.getEnabled())
                .createTime(role.getCreateTime())
                .updateTime(role.getUpdateTime())
                .build();
    }

}
