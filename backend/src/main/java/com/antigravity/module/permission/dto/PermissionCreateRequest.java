package com.antigravity.module.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 权限创建请求 DTO
 */
@Data
public class PermissionCreateRequest {

    @NotBlank(message = "权限编码不能为空")
    @Size(min = 2, max = 50, message = "权限编码长度必须在 2-50 个字符之间")
    @Pattern(regexp = "^[A-Z][A-Z0-9_:]*$", message = "权限编码必须以大写字母开头，只能包含大写字母、数字、下划线和冒号")
    private String code;

    @NotBlank(message = "权限名称不能为空")
    @Size(min = 2, max = 50, message = "权限名称长度必须在 2-50 个字符之间")
    private String name;

    @NotBlank(message = "权限类型不能为空")
    @Pattern(regexp = "^(MENU|BUTTON|API)$", message = "权限类型必须是 MENU、BUTTON 或 API")
    private String type;

    private Long parentId;

    @Size(max = 200, message = "路径长度不能超过 200 个字符")
    private String path;

    @Size(max = 50, message = "图标长度不能超过 50 个字符")
    private String icon;

    private Integer sort;

    private Boolean enabled;

}
