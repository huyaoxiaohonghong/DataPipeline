package com.antigravity.module.db.entity;

import com.antigravity.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 数据库连接实体
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_db_connection")
public class DbConnection extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 连接名称
     */
    private String name;

    /**
     * 数据库类型：MYSQL, POSTGRESQL, ORACLE, SQLSERVER
     */
    private String dbType;

    /**
     * 主机地址
     */
    private String host;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 数据库名
     */
    private String databaseName;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（加密存储）
     */
    private String password;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 是否删除
     */
    @TableLogic
    private Boolean isDeleted;

}
