package com.antigravity.module.user;

import com.antigravity.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 用户实体
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String username;

    private String password;

    private String email;

    private String role;

    @TableLogic
    private Boolean isDeleted;

}
