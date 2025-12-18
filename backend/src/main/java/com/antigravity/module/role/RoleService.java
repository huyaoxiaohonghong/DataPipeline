package com.antigravity.module.role;

import com.antigravity.common.PageResult;

import java.util.List;
import java.util.Optional;

/**
 * 角色 Service 接口
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
public interface RoleService {

    Optional<Role> findById(Long id);

    Optional<Role> findByCode(String code);

    List<Role> findAll();

    List<Role> findEnabled();

    PageResult<Role> pageQuery(int pageNumber, int pageSize, String name, String code, Boolean enabled);

    boolean existsByCode(String code);

    boolean existsByName(String name);

    Role createRole(Role role);

    boolean updateRole(Role role);

    boolean updateEnabled(Long id, Boolean enabled);

    boolean deleteById(Long id);

    boolean deleteByIds(List<Long> ids);

}
