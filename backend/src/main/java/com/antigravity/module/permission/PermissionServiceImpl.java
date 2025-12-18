package com.antigravity.module.permission;

import com.antigravity.module.permission.mapper.PermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 权限 Service 实现
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionMapper permissionMapper;

    @Override
    public Optional<Permission> findById(Long id) {
        return Optional.ofNullable(permissionMapper.selectById(id));
    }

    @Override
    public Optional<Permission> findByCode(String code) {
        return Optional.ofNullable(permissionMapper.selectByCode(code));
    }

    @Override
    public List<Permission> findAll() {
        return permissionMapper.selectAll();
    }

    @Override
    public List<Permission> findByParentId(Long parentId) {
        return permissionMapper.selectByParentId(parentId);
    }

    @Override
    public List<Permission> findByType(String type) {
        return permissionMapper.selectByType(type);
    }

    @Override
    public List<Permission> findEnabled() {
        return permissionMapper.selectEnabled();
    }

    @Override
    public List<Permission> getPermissionTree() {
        List<Permission> allPermissions = permissionMapper.selectAll();
        return buildTree(allPermissions, 0L);
    }

    @Override
    public boolean existsByCode(String code) {
        return permissionMapper.countByCode(code) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Permission createPermission(Permission permission) {
        if (permission.getSort() == null) {
            permission.setSort(0);
        }
        if (permission.getEnabled() == null) {
            permission.setEnabled(true);
        }
        if (permission.getParentId() == null) {
            permission.setParentId(0L);
        }
        permission.setIsDeleted(false);
        permissionMapper.insert(permission);
        log.info("创建权限成功: code={}, name={}", permission.getCode(), permission.getName());
        return permission;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePermission(Permission permission) {
        int rows = permissionMapper.update(permission);
        if (rows > 0) {
            log.info("更新权限成功: id={}", permission.getId());
        }
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateEnabled(Long id, Boolean enabled) {
        int rows = permissionMapper.updateEnabled(id, enabled);
        if (rows > 0) {
            log.info("更新权限状态: id={}, enabled={}", id, enabled);
        }
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        int rows = permissionMapper.deleteById(id);
        if (rows > 0) {
            log.info("删除权限成功: id={}", id);
        }
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByIds(List<Long> ids) {
        int rows = permissionMapper.deleteByIds(ids);
        if (rows > 0) {
            log.info("批量删除权限成功: ids={}", ids);
        }
        return rows > 0;
    }

    @Override
    public List<Permission> findByRoleId(Long roleId) {
        return permissionMapper.selectByRoleId(roleId);
    }

    @Override
    public List<Permission> findByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        return permissionMapper.selectByRoleIds(roleIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        // 先删除旧的关联
        permissionMapper.deleteRolePermissions(roleId);

        // 插入新的关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                permissionMapper.insertRolePermission(roleId, permissionId);
            }
        }

        log.info("为角色分配权限成功: roleId={}, permissionIds={}", roleId, permissionIds);
        return true;
    }

    @Override
    public List<Long> getPermissionIdsByRoleId(Long roleId) {
        return permissionMapper.selectPermissionIdsByRoleId(roleId);
    }

    /**
     * 构建权限树
     */
    private List<Permission> buildTree(List<Permission> allPermissions, Long parentId) {
        return allPermissions.stream()
                .filter(p -> parentId.equals(p.getParentId()))
                .peek(p -> {
                    List<Permission> children = buildTree(allPermissions, p.getId());
                    // 这里可以添加 children 字段到 Permission 实体，或者使用 DTO
                })
                .collect(Collectors.toList());
    }

}
