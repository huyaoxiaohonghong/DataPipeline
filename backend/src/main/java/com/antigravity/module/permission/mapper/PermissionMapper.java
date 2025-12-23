package com.antigravity.module.permission.mapper;

import com.antigravity.module.permission.Permission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 权限 Mapper
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

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
