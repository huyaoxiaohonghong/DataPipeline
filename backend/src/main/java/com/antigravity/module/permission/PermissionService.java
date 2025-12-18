package com.antigravity.module.permission;

import java.util.List;
import java.util.Optional;

/**
 * 权限 Service 接口
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
public interface PermissionService {

    /**
     * 根据ID查询权限
     */
    Optional<Permission> findById(Long id);

    /**
     * 根据编码查询权限
     */
    Optional<Permission> findByCode(String code);

    /**
     * 查询所有权限
     */
    List<Permission> findAll();

    /**
     * 根据父级ID查询权限
     */
    List<Permission> findByParentId(Long parentId);

    /**
     * 根据类型查询权限
     */
    List<Permission> findByType(String type);

    /**
     * 查询所有启用的权限
     */
    List<Permission> findEnabled();

    /**
     * 查询权限树
     */
    List<Permission> getPermissionTree();

    /**
     * 检查权限编码是否存在
     */
    boolean existsByCode(String code);

    /**
     * 创建权限
     */
    Permission createPermission(Permission permission);

    /**
     * 更新权限
     */
    boolean updatePermission(Permission permission);

    /**
     * 更新权限状态
     */
    boolean updateEnabled(Long id, Boolean enabled);

    /**
     * 删除权限
     */
    boolean deleteById(Long id);

    /**
     * 批量删除权限
     */
    boolean deleteByIds(List<Long> ids);

    /**
     * 根据角色ID查询权限
     */
    List<Permission> findByRoleId(Long roleId);

    /**
     * 根据角色ID列表查询权限
     */
    List<Permission> findByRoleIds(List<Long> roleIds);

    /**
     * 为角色分配权限
     */
    boolean assignPermissionsToRole(Long roleId, List<Long> permissionIds);

    /**
     * 获取角色的权限ID列表
     */
    List<Long> getPermissionIdsByRoleId(Long roleId);

}
