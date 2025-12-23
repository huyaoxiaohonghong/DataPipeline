package com.antigravity.module.user.mapper;

import com.antigravity.module.user.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

        // BaseMapper provides: selectById, selectOne, selectList, insert, updateById,
        // deleteById, selectPage, etc.
        // Custom complex queries can be added here if needed.

}
