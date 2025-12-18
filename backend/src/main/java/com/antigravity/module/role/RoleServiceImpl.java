package com.antigravity.module.role;

import com.antigravity.common.PageResult;
import com.antigravity.module.role.mapper.RoleMapper;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;

    @Override
    public Optional<Role> findById(Long id) {
        return Optional.ofNullable(roleMapper.selectById(id));
    }

    @Override
    public Optional<Role> findByCode(String code) {
        return Optional.ofNullable(roleMapper.selectByCode(code));
    }

    @Override
    public List<Role> findAll() {
        return roleMapper.selectAll();
    }

    @Override
    public List<Role> findEnabled() {
        return roleMapper.selectEnabled();
    }

    @Override
    public PageResult<Role> pageQuery(int pageNumber, int pageSize, String name, String code, Boolean enabled) {
        int offset = (pageNumber - 1) * pageSize;
        List<Role> records = roleMapper.selectPage(name, code, enabled, offset, pageSize);
        long total = roleMapper.selectCount(name, code, enabled);
        return PageResult.of(records, pageNumber, pageSize, total);
    }

    @Override
    public boolean existsByCode(String code) {
        return roleMapper.countByCode(code) > 0;
    }

    @Override
    public boolean existsByName(String name) {
        return roleMapper.countByName(name) > 0;
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
        roleMapper.insert(role);
        log.info("创建角色成功: code={}, name={}", role.getCode(), role.getName());
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(Role role) {
        int rows = roleMapper.update(role);
        if (rows > 0) {
            log.info("更新角色成功: id={}", role.getId());
        }
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateEnabled(Long id, Boolean enabled) {
        int rows = roleMapper.updateEnabled(id, enabled);
        if (rows > 0) {
            log.info("更新角色状态: id={}, enabled={}", id, enabled);
        }
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        int rows = roleMapper.deleteById(id);
        if (rows > 0) {
            log.info("删除角色成功: id={}", id);
        }
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByIds(List<Long> ids) {
        int rows = roleMapper.deleteByIds(ids);
        if (rows > 0) {
            log.info("批量删除角色成功: ids={}", ids);
        }
        return rows > 0;
    }

}
