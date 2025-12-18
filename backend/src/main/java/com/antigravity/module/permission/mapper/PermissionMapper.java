package com.antigravity.module.permission.mapper;

import com.antigravity.module.permission.Permission;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 权限 Mapper
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Mapper
public interface PermissionMapper {

    @Select("SELECT * FROM sys_permission WHERE id = #{id} AND is_deleted = 0")
    Permission selectById(Long id);

    @Select("SELECT * FROM sys_permission WHERE code = #{code} AND is_deleted = 0")
    Permission selectByCode(String code);

    @Select("SELECT * FROM sys_permission WHERE is_deleted = 0 ORDER BY sort ASC, create_time DESC")
    List<Permission> selectAll();

    @Select("SELECT * FROM sys_permission WHERE parent_id = #{parentId} AND is_deleted = 0 ORDER BY sort ASC")
    List<Permission> selectByParentId(Long parentId);

    @Select("SELECT * FROM sys_permission WHERE type = #{type} AND is_deleted = 0 ORDER BY sort ASC")
    List<Permission> selectByType(String type);

    @Select("SELECT * FROM sys_permission WHERE enabled = 1 AND is_deleted = 0 ORDER BY sort ASC")
    List<Permission> selectEnabled();

    @Select("SELECT COUNT(*) FROM sys_permission WHERE code = #{code} AND is_deleted = 0")
    int countByCode(String code);

    @Insert("INSERT INTO sys_permission (code, name, type, parent_id, path, icon, sort, enabled, is_deleted, create_time, update_time) "
            +
            "VALUES (#{code}, #{name}, #{type}, #{parentId}, #{path}, #{icon}, #{sort}, #{enabled}, #{isDeleted}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Permission permission);

    @Update("UPDATE sys_permission SET code = #{code}, name = #{name}, type = #{type}, parent_id = #{parentId}, " +
            "path = #{path}, icon = #{icon}, sort = #{sort}, enabled = #{enabled}, update_time = NOW() " +
            "WHERE id = #{id} AND is_deleted = 0")
    int update(Permission permission);

    @Update("UPDATE sys_permission SET enabled = #{enabled}, update_time = NOW() WHERE id = #{id} AND is_deleted = 0")
    int updateEnabled(@Param("id") Long id, @Param("enabled") Boolean enabled);

    @Update("UPDATE sys_permission SET is_deleted = 1, update_time = NOW() WHERE id = #{id}")
    int deleteById(Long id);

    @Update("<script>" +
            "UPDATE sys_permission SET is_deleted = 1, update_time = NOW() WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int deleteByIds(@Param("ids") List<Long> ids);

    // ==================== 角色权限关联 ====================

    @Select("SELECT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.is_deleted = 0 " +
            "ORDER BY p.sort ASC")
    List<Permission> selectByRoleId(Long roleId);

    @Select("<script>" +
            "SELECT DISTINCT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id IN " +
            "<foreach collection='roleIds' item='roleId' open='(' separator=',' close=')'>" +
            "#{roleId}" +
            "</foreach>" +
            " AND p.is_deleted = 0 ORDER BY p.sort ASC" +
            "</script>")
    List<Permission> selectByRoleIds(@Param("roleIds") List<Long> roleIds);

    @Insert("INSERT INTO sys_role_permission (role_id, permission_id) VALUES (#{roleId}, #{permissionId})")
    int insertRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    int deleteRolePermissions(Long roleId);

    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId} AND permission_id = #{permissionId}")
    int deleteRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    @Select("SELECT permission_id FROM sys_role_permission WHERE role_id = #{roleId}")
    List<Long> selectPermissionIdsByRoleId(Long roleId);

}
