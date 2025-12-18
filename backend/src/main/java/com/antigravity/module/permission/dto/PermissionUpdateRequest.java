package com.antigravity.module.permission.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 权限更新请求 DTO
 */
@Data
public class PermissionUpdateRequest {

    @Size(min = 2, max = 50, message = "权限名称长度必须在 2-50 个字符之间")
    private String name;

    private Long parentId;

    @Size(max = 200, message = "路径长度不能超过 200 个字符")
    private String path;

    @Size(max = 50, message = "图标长度不能超过 50 个字符")
    private String icon;

    private Integer sort;

    private Boolean enabled;

}
