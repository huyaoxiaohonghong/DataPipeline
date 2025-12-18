package com.antigravity.module.role.mapper;

import com.antigravity.module.role.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 角色 Mapper
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Mapper
public interface RoleMapper {

        @Select("SELECT * FROM sys_role WHERE id = #{id} AND is_deleted = 0")
        Role selectById(Long id);

        @Select("SELECT * FROM sys_role WHERE code = #{code} AND is_deleted = 0")
        Role selectByCode(String code);

        @Select("SELECT * FROM sys_role WHERE is_deleted = 0 ORDER BY sort ASC, create_time DESC")
        List<Role> selectAll();

        @Select("SELECT * FROM sys_role WHERE enabled = 1 AND is_deleted = 0 ORDER BY sort ASC")
        List<Role> selectEnabled();

        @Select("SELECT COUNT(*) FROM sys_role WHERE code = #{code} AND is_deleted = 0")
        int countByCode(String code);

        @Select("SELECT COUNT(*) FROM sys_role WHERE name = #{name} AND is_deleted = 0")
        int countByName(String name);

        @Insert("INSERT INTO sys_role (code, name, description, sort, enabled, is_deleted, create_time, update_time) " +
                        "VALUES (#{code}, #{name}, #{description}, #{sort}, #{enabled}, #{isDeleted}, NOW(), NOW())")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(Role role);

        @Update("UPDATE sys_role SET code = #{code}, name = #{name}, description = #{description}, " +
                        "sort = #{sort}, enabled = #{enabled}, update_time = NOW() " +
                        "WHERE id = #{id} AND is_deleted = 0")
        int update(Role role);

        @Update("UPDATE sys_role SET enabled = #{enabled}, update_time = NOW() WHERE id = #{id} AND is_deleted = 0")
        int updateEnabled(@Param("id") Long id, @Param("enabled") Boolean enabled);

        @Update("UPDATE sys_role SET is_deleted = 1, update_time = NOW() WHERE id = #{id}")
        int deleteById(Long id);

        @Update("<script>" +
                        "UPDATE sys_role SET is_deleted = 1, update_time = NOW() WHERE id IN " +
                        "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
                        "#{id}" +
                        "</foreach>" +
                        "</script>")
        int deleteByIds(@Param("ids") List<Long> ids);

        @Select("<script>" +
                        "SELECT * FROM sys_role WHERE is_deleted = 0 " +
                        "<if test='name != null and name != \"\"'>" +
                        "AND name LIKE CONCAT('%', #{name}, '%') " +
                        "</if>" +
                        "<if test='code != null and code != \"\"'>" +
                        "AND code LIKE CONCAT('%', #{code}, '%') " +
                        "</if>" +
                        "<if test='enabled != null'>" +
                        "AND enabled = #{enabled} " +
                        "</if>" +
                        "ORDER BY sort ASC, create_time DESC " +
                        "LIMIT #{offset}, #{pageSize}" +
                        "</script>")
        List<Role> selectPage(@Param("name") String name,
                        @Param("code") String code,
                        @Param("enabled") Boolean enabled,
                        @Param("offset") int offset,
                        @Param("pageSize") int pageSize);

        @Select("<script>" +
                        "SELECT COUNT(*) FROM sys_role WHERE is_deleted = 0 " +
                        "<if test='name != null and name != \"\"'>" +
                        "AND name LIKE CONCAT('%', #{name}, '%') " +
                        "</if>" +
                        "<if test='code != null and code != \"\"'>" +
                        "AND code LIKE CONCAT('%', #{code}, '%') " +
                        "</if>" +
                        "<if test='enabled != null'>" +
                        "AND enabled = #{enabled} " +
                        "</if>" +
                        "</script>")
        long selectCount(@Param("name") String name,
                        @Param("code") String code,
                        @Param("enabled") Boolean enabled);

}
