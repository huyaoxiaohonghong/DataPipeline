package com.antigravity.module.user.mapper;

import com.antigravity.module.user.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户 Mapper
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Mapper
public interface UserMapper {

        @Select("SELECT * FROM sys_user WHERE id = #{id} AND is_deleted = 0")
        User selectById(Long id);

        @Select("SELECT * FROM sys_user WHERE username = #{username} AND is_deleted = 0")
        User selectByUsername(String username);

        @Select("SELECT * FROM sys_user WHERE email = #{email} AND is_deleted = 0")
        User selectByEmail(String email);

        @Select("SELECT * FROM sys_user WHERE role = #{role} AND is_deleted = 0 ORDER BY create_time DESC")
        List<User> selectByRole(String role);

        @Select("SELECT COUNT(*) FROM sys_user WHERE username = #{username} AND is_deleted = 0")
        int countByUsername(String username);

        @Select("SELECT COUNT(*) FROM sys_user WHERE email = #{email} AND is_deleted = 0")
        int countByEmail(String email);

        @Insert("INSERT INTO sys_user (username, password, email, role, is_deleted, create_time, update_time) " +
                        "VALUES (#{username}, #{password}, #{email}, #{role}, #{isDeleted}, NOW(), NOW())")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(User user);

        @Update("UPDATE sys_user SET username = #{username}, email = #{email}, role = #{role}, update_time = NOW() " +
                        "WHERE id = #{id} AND is_deleted = 0")
        int update(User user);

        @Update("UPDATE sys_user SET password = #{password}, update_time = NOW() WHERE id = #{id} AND is_deleted = 0")
        int updatePassword(@Param("id") Long id, @Param("password") String password);

        @Update("UPDATE sys_user SET role = #{role}, update_time = NOW() WHERE id = #{id} AND is_deleted = 0")
        int updateRole(@Param("id") Long id, @Param("role") String role);

        @Update("UPDATE sys_user SET is_deleted = 1, update_time = NOW() WHERE id = #{id}")
        int deleteById(Long id);

        @Update("<script>" +
                        "UPDATE sys_user SET is_deleted = 1, update_time = NOW() WHERE id IN " +
                        "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
                        "#{id}" +
                        "</foreach>" +
                        "</script>")
        int deleteByIds(@Param("ids") List<Long> ids);

        @Select("<script>" +
                        "SELECT * FROM sys_user WHERE is_deleted = 0 " +
                        "<if test='username != null and username != \"\"'>" +
                        "AND username LIKE CONCAT('%', #{username}, '%') " +
                        "</if>" +
                        "<if test='role != null and role != \"\"'>" +
                        "AND role = #{role} " +
                        "</if>" +
                        "ORDER BY create_time DESC " +
                        "LIMIT #{offset}, #{pageSize}" +
                        "</script>")
        List<User> selectPage(@Param("username") String username,
                        @Param("role") String role,
                        @Param("offset") int offset,
                        @Param("pageSize") int pageSize);

        @Select("<script>" +
                        "SELECT COUNT(*) FROM sys_user WHERE is_deleted = 0 " +
                        "<if test='username != null and username != \"\"'>" +
                        "AND username LIKE CONCAT('%', #{username}, '%') " +
                        "</if>" +
                        "<if test='role != null and role != \"\"'>" +
                        "AND role = #{role} " +
                        "</if>" +
                        "</script>")
        long selectCount(@Param("username") String username, @Param("role") String role);

}
