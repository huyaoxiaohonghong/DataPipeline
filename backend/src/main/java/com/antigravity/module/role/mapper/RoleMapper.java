package com.antigravity.module.role.mapper;

import com.antigravity.module.role.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色 Mapper
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

        // BaseMapper provides: selectById, selectOne, selectList, insert, updateById,
        // deleteById, selectPage, etc.
        // Custom complex queries can be added here if needed.

}
