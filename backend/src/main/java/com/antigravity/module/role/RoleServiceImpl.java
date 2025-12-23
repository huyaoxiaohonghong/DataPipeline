package com.antigravity.module.role;

import com.antigravity.common.PageResult;
import com.antigravity.module.role.mapper.RoleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 角色 Service 实现
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Override
    public Optional<Role> findById(Long id) {
        return Optional.ofNullable(this.getById(id));
    }

    @Override
    public Optional<Role> findByCode(String code) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getCode, code);
        return Optional.ofNullable(this.getOne(wrapper));
    }

    @Override
    public List<Role> findAll() {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Role::getSort)
                .orderByDesc(Role::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public List<Role> findEnabled() {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getEnabled, true)
                .orderByAsc(Role::getSort);
        return this.list(wrapper);
    }

    @Override
    public PageResult<Role> pageQuery(int pageNumber, int pageSize, String name, String code, Boolean enabled) {
        Page<Role> page = new Page<>(pageNumber, pageSize);
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.isNotBlank(name), Role::getName, name)
                .like(StringUtils.isNotBlank(code), Role::getCode, code)
                .eq(enabled != null, Role::getEnabled, enabled)
                .orderByAsc(Role::getSort)
                .orderByDesc(Role::getCreateTime);

        Page<Role> resultPage = this.page(page, wrapper);
        return PageResult.of(resultPage.getRecords(), pageNumber, pageSize, resultPage.getTotal());
    }

    @Override
    public boolean existsByCode(String code) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getCode, code);
        return this.count(wrapper) > 0;
    }

    @Override
    public boolean existsByName(String name) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getName, name);
        return this.count(wrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Role createRole(Role role) {
        if (role.getSort() == null) {
            role.setSort(0);
        }
        if (role.getEnabled() == null) {
            role.setEnabled(true);
        }
        role.setIsDeleted(false);
        this.save(role);
        log.info("创建角色成功: code={}, name={}", role.getCode(), role.getName());
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(Role role) {
        boolean success = this.updateById(role);
        if (success) {
            log.info("更新角色成功: id={}", role.getId());
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateEnabled(Long id, Boolean enabled) {
        LambdaUpdateWrapper<Role> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Role::getId, id)
                .set(Role::getEnabled, enabled);
        boolean success = this.update(wrapper);
        if (success) {
            log.info("更新角色状态: id={}, enabled={}", id, enabled);
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long id) {
        boolean success = this.removeById(id);
        if (success) {
            log.info("删除角色成功: id={}", id);
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRoles(List<Long> ids) {
        boolean success = this.removeByIds(ids);
        if (success) {
            log.info("批量删除角色成功: ids={}", ids);
        }
        return success;
    }

}
