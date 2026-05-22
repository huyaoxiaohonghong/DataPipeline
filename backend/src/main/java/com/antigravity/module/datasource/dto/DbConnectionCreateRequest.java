package com.antigravity.module.datasource.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 数据库连接创建请求 DTO
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Data
public class DbConnectionCreateRequest {

    @NotBlank(message = "连接名称不能为空")
    @Size(max = 100, message = "连接名称长度不能超过 100 个字符")
    private String name;

    @NotBlank(message = "数据库类型不能为空")
    @Pattern(regexp = "^(MYSQL|POSTGRESQL|ORACLE|SQLSERVER)$", message = "数据库类型必须是 MYSQL、POSTGRESQL、ORACLE 或 SQLSERVER")
    private String dbType;

    @NotBlank(message = "主机地址不能为空")
    @Size(max = 255, message = "主机地址长度不能超过 255 个字符")
    private String host;

    @NotNull(message = "端口不能为空")
    @Min(value = 1, message = "端口号最小为 1")
    @Max(value = 65535, message = "端口号最大为 65535")
    private Integer port;

    @NotBlank(message = "数据库名不能为空")
    @Size(max = 100, message = "数据库名长度不能超过 100 个字符")
    private String databaseName;

    @NotBlank(message = "用户名不能为空")
    @Size(max = 100, message = "用户名长度不能超过 100 个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(max = 255, message = "密码长度不能超过 255 个字符")
    private String password;

    @Size(max = 500, message = "描述长度不能超过 500 个字符")
    private String description;

}
