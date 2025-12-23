package com.antigravity.module.db.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 测试数据库连接请求
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Data
public class DbConnectionTestRequest {

    @NotBlank(message = "数据库类型不能为空")
    @Pattern(regexp = "^(MYSQL|POSTGRESQL|ORACLE|SQLSERVER)$", message = "数据库类型必须是 MYSQL、POSTGRESQL、ORACLE 或 SQLSERVER")
    private String dbType;

    @NotBlank(message = "主机地址不能为空")
    private String host;

    @Min(value = 1, message = "端口号必须大于0")
    @Max(value = 65535, message = "端口号不能超过65535")
    private Integer port;

    @NotBlank(message = "数据库名不能为空")
    private String databaseName;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

}
