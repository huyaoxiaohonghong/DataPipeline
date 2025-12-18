package com.antigravity.module.permission.dto;

import com.antigravity.module.permission.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionVO {

    private Long id;

    private String code;

    private String name;

    private String type;

    private Long parentId;

    private String path;

    private String icon;

    private Integer sort;

    private Boolean enabled;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 子权限列表（用于树形结构）
     */
    private List<PermissionVO> children;

    public static PermissionVO fromEntity(Permission permission) {
        if (permission == null)
            return null;
        return PermissionVO.builder()
                .id(permission.getId())
                .code(permission.getCode())
                .name(permission.getName())
                .type(permission.getType())
                .parentId(permission.getParentId())
                .path(permission.getPath())
                .icon(permission.getIcon())
                .sort(permission.getSort())
                .enabled(permission.getEnabled())
                .createTime(permission.getCreateTime())
                .updateTime(permission.getUpdateTime())
                .build();
    }

}
