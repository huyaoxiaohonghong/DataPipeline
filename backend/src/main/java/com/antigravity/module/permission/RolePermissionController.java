package com.antigravity.module.permission;

import com.antigravity.common.Result;
import com.antigravity.module.permission.dto.PermissionVO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色权限关联 Controller
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/role-permissions")
@RequiredArgsConstructor
public class RolePermissionController {

    private final PermissionService permissionService;

    /**
     * 获取角色的权限列表
     */
    @GetMapping("/{roleId}")
    public Result<List<PermissionVO>> getPermissionsByRoleId(
            @PathVariable @Min(value = 1, message = "角色ID必须大于0") Long roleId) {
        List<PermissionVO> vos = permissionService.findByRoleId(roleId)
                .stream()
                .map(PermissionVO::fromEntity)
                .collect(Collectors.toList());
        return Result.success(vos);
    }

    /**
     * 获取角色的权限ID列表
     */
    @GetMapping("/{roleId}/ids")
    public Result<List<Long>> getPermissionIdsByRoleId(
            @PathVariable @Min(value = 1, message = "角色ID必须大于0") Long roleId) {
        List<Long> permissionIds = permissionService.getPermissionIdsByRoleId(roleId);
        return Result.success(permissionIds);
    }

    /**
     * 为角色分配权限
     */
    @PostMapping("/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> assignPermissions(
            @PathVariable @Min(value = 1, message = "角色ID必须大于0") Long roleId,
            @RequestBody @NotNull(message = "权限ID列表不能为null") List<Long> permissionIds) {
        boolean success = permissionService.assignPermissionsToRole(roleId, permissionIds);
        return success ? Result.success("权限分配成功", null) : Result.error("权限分配失败");
    }

}
