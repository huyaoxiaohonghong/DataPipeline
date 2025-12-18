package com.antigravity.module.user.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 用户更新请求 DTO
 */
@Data
public class UserUpdateRequest {

    @Size(min = 3, max = 20, message = "用户名长度必须在 3-20 个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过 100 个字符")
    private String email;

    @Pattern(regexp = "^(ADMIN|USER|GUEST)?$", message = "角色必须是 ADMIN、USER 或 GUEST")
    private String role;

}
