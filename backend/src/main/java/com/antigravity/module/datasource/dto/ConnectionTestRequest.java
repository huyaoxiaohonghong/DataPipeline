package com.antigravity.module.datasource.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 连接测试请求 DTO
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Data
public class ConnectionTestRequest {

    @NotBlank(message = "数据库类型不能为空")
    @Pattern(regexp = "^(MYSQL|POSTGRESQL|ORACLE|SQLSERVER)$", message = "数据库类型必须是 MYSQL、POSTGRESQL、ORACLE 或 SQLSERVER")
    private String dbType;

    @NotBlank(message = "主机地址不能为空")
    private String host;

    @NotNull(message = "端口不能为空")
    @Min(value = 1, message = "端口号最小为 1")
    @Max(value = 65535, message = "端口号最大为 65535")
    private Integer port;

    @NotBlank(message = "数据库名不能为空")
    private String databaseName;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

}
