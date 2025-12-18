package com.antigravity.module.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 角色创建请求 DTO
 */
@Data
public class RoleCreateRequest {

    @NotBlank(message = "角色编码不能为空")
    @Size(min = 2, max = 30, message = "角色编码长度必须在 2-30 个字符之间")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "角色编码必须以大写字母开头，只能包含大写字母、数字和下划线")
    private String code;

    @NotBlank(message = "角色名称不能为空")
    @Size(min = 2, max = 50, message = "角色名称长度必须在 2-50 个字符之间")
    private String name;

    @Size(max = 200, message = "描述长度不能超过 200 个字符")
    private String description;

    private Integer sort;

    private Boolean enabled;

}
