package com.antigravity.module.db.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 更新数据库连接请求
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Data
public class DbConnectionUpdateRequest {

    private String name;

    @Pattern(regexp = "^(MYSQL|POSTGRESQL|ORACLE|SQLSERVER)$", message = "数据库类型必须是 MYSQL、POSTGRESQL、ORACLE 或 SQLSERVER")
    private String dbType;

    private String host;

    @Min(value = 1, message = "端口号必须大于0")
    @Max(value = 65535, message = "端口号不能超过65535")
    private Integer port;

    private String databaseName;

    private String username;

    private String password;

    private String description;

    private Boolean enabled;

}
