package com.antigravity.module.permission;

import com.antigravity.module.permission.mapper.PermissionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    @Override
    public Optional<Permission> findById(Long id) {
        return Optional.ofNullable(this.getById(id));
    }

    @Override
    public Optional<Permission> findByCode(String code) {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getCode, code);
        return Optional.ofNullable(this.getOne(wrapper));
    }

    @Override
    public List<Permission> findAll() {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Permission::getSort)
                .orderByDesc(Permission::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public List<Permission> findByParentId(Long parentId) {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getParentId, parentId)
                .orderByAsc(Permission::getSort);
        return this.list(wrapper);
    }

    @Override
    public List<Permission> findByType(String type) {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getType, type)
                .orderByAsc(Permission::getSort);
        return this.list(wrapper);
    }

    @Override
    public List<Permission> findEnabled() {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getEnabled, true)
                .orderByAsc(Permission::getSort);
        return this.list(wrapper);
    }

    @Override
    public List<Permission> getPermissionTree() {
        List<Permission> allPermissions = this.findAll();
        return buildTree(allPermissions, 0L);
    }

    @Override
    public boolean existsByCode(String code) {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getCode, code);
        return this.count(wrapper) > 0;
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
        this.save(permission);
        log.info("创建权限成功: code={}, name={}", permission.getCode(), permission.getName());
        return permission;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePermission(Permission permission) {
        boolean success = this.updateById(permission);
        if (success) {
            log.info("更新权限成功: id={}", permission.getId());
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateEnabled(Long id, Boolean enabled) {
        LambdaUpdateWrapper<Permission> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Permission::getId, id)
                .set(Permission::getEnabled, enabled);
        boolean success = this.update(wrapper);
        if (success) {
            log.info("更新权限状态: id={}, enabled={}", id, enabled);
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePermission(Long id) {
        boolean success = this.removeById(id);
        if (success) {
            log.info("删除权限成功: id={}", id);
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePermissions(List<Long> ids) {
        boolean success = this.removeByIds(ids);
        if (success) {
            log.info("批量删除权限成功: ids={}", ids);
        }
        return success;
    }

    @Override
    public List<Permission> findByRoleId(Long roleId) {
        return this.baseMapper.selectByRoleId(roleId);
    }

    @Override
    public List<Permission> findByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        return this.baseMapper.selectByRoleIds(roleIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        // 先删除旧的关联
        this.baseMapper.deleteRolePermissions(roleId);

        // 插入新的关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                this.baseMapper.insertRolePermission(roleId, permissionId);
            }
        }

        log.info("为角色分配权限成功: roleId={}, permissionIds={}", roleId, permissionIds);
        return true;
    }

    @Override
    public List<Long> getPermissionIdsByRoleId(Long roleId) {
        return this.baseMapper.selectPermissionIdsByRoleId(roleId);
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
