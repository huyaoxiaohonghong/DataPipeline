package com.antigravity.module.role.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 角色更新请求 DTO
 */
@Data
public class RoleUpdateRequest {

    @Size(min = 2, max = 50, message = "角色名称长度必须在 2-50 个字符之间")
    private String name;

    @Size(max = 200, message = "描述长度不能超过 200 个字符")
    private String description;

    private Integer sort;

    private Boolean enabled;

}
