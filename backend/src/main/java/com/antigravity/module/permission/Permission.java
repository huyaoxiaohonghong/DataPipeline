package com.antigravity.module.permission;

import com.antigravity.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 权限实体
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Permission extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 权限编码（唯一）
     */
    private String code;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限类型：MENU-菜单, BUTTON-按钮, API-接口
     */
    private String type;

    /**
     * 父级ID
     */
    private Long parentId;

    /**
     * 路径/URL
     */
    private String path;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 是否删除
     */
    private Boolean isDeleted;

}
