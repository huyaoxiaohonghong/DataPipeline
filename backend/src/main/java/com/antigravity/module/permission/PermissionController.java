package com.antigravity.module.permission;

import com.antigravity.common.Result;
import com.antigravity.module.permission.dto.PermissionCreateRequest;
import com.antigravity.module.permission.dto.PermissionUpdateRequest;
import com.antigravity.module.permission.dto.PermissionVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限管理 Controller
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 获取所有权限（列表）
     */
    @GetMapping
    public Result<List<PermissionVO>> list(@RequestParam(required = false) String type) {
        List<Permission> permissions;
        if (type != null && !type.isEmpty()) {
            permissions = permissionService.findByType(type);
        } else {
            permissions = permissionService.findAll();
        }
        List<PermissionVO> vos = permissions.stream()
                .map(PermissionVO::fromEntity)
                .collect(Collectors.toList());
        return Result.success(vos);
    }

    /**
     * 获取权限树
     */
    @GetMapping("/tree")
    public Result<List<PermissionVO>> tree() {
        List<Permission> allPermissions = permissionService.findAll();
        List<PermissionVO> tree = buildTree(allPermissions, 0L);
        return Result.success(tree);
    }

    /**
     * 获取所有启用的权限
     */
    @GetMapping("/enabled")
    public Result<List<PermissionVO>> listEnabled() {
        List<PermissionVO> vos = permissionService.findEnabled()
                .stream()
                .map(PermissionVO::fromEntity)
                .collect(Collectors.toList());
        return Result.success(vos);
    }

    /**
     * 根据ID查询权限
     */
    @GetMapping("/{id}")
    public Result<PermissionVO> getById(@PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id) {
        return permissionService.findById(id)
                .map(PermissionVO::fromEntity)
                .map(Result::success)
                .orElse(Result.notFound("权限不存在"));
    }

    /**
     * 根据编码查询权限
     */
    @GetMapping("/code/{code}")
    public Result<PermissionVO> getByCode(@PathVariable String code) {
        return permissionService.findByCode(code)
                .map(PermissionVO::fromEntity)
                .map(Result::success)
                .orElse(Result.notFound("权限不存在"));
    }

    /**
     * 创建权限
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PermissionVO> create(@RequestBody @Valid PermissionCreateRequest request) {
        // 检查编码是否存在
        if (permissionService.existsByCode(request.getCode())) {
            return Result.badRequest("权限编码已存在");
        }

        Permission permission = new Permission();
        permission.setCode(request.getCode());
        permission.setName(request.getName());
        permission.setType(request.getType());
        permission.setParentId(request.getParentId());
        permission.setPath(request.getPath());
        permission.setIcon(request.getIcon());
        permission.setSort(request.getSort());
        permission.setEnabled(request.getEnabled());

        Permission created = permissionService.createPermission(permission);
        return Result.success("权限创建成功", PermissionVO.fromEntity(created));
    }

    /**
     * 更新权限
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PermissionVO> update(
            @PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id,
            @RequestBody @Valid PermissionUpdateRequest request) {
        Permission existing = permissionService.findById(id).orElse(null);
        if (existing == null) {
            return Result.notFound("权限不存在");
        }

        Permission permission = new Permission();
        permission.setId(id);
        permission.setCode(existing.getCode()); // 编码不允许修改
        permission.setType(existing.getType()); // 类型不允许修改
        permission.setName(request.getName() != null ? request.getName() : existing.getName());
        permission.setParentId(request.getParentId() != null ? request.getParentId() : existing.getParentId());
        permission.setPath(request.getPath() != null ? request.getPath() : existing.getPath());
        permission.setIcon(request.getIcon() != null ? request.getIcon() : existing.getIcon());
        permission.setSort(request.getSort() != null ? request.getSort() : existing.getSort());
        permission.setEnabled(request.getEnabled() != null ? request.getEnabled() : existing.getEnabled());

        boolean success = permissionService.updatePermission(permission);
        if (success) {
            Permission updated = permissionService.findById(id).orElse(null);
            return Result.success("权限更新成功", PermissionVO.fromEntity(updated));
        }
        return Result.error("权限更新失败");
    }

    /**
     * 更新权限状态
     */
    @PatchMapping("/{id}/enabled")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateEnabled(
            @PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id,
            @RequestParam Boolean enabled) {
        boolean success = permissionService.updateEnabled(id, enabled);
        return success ? Result.success() : Result.error("更新状态失败");
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id) {
        boolean success = permissionService.deletePermission(id);
        return success ? Result.success("权限删除成功", null) : Result.error("权限删除失败");
    }

    /**
     * 批量删除权限
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> batchDelete(@RequestBody @NotEmpty(message = "请选择要删除的权限") List<Long> ids) {
        boolean success = permissionService.deletePermissions(ids);
        return success ? Result.success("批量删除成功", null) : Result.error("批量删除失败");
    }

    /**
     * 检查权限编码是否可用
     */
    @GetMapping("/check-code")
    public Result<Boolean> checkCode(@RequestParam String code) {
        boolean exists = permissionService.existsByCode(code);
        return Result.success(!exists);
    }

    /**
     * 构建权限树
     */
    private List<PermissionVO> buildTree(List<Permission> allPermissions, Long parentId) {
        Map<Long, List<Permission>> groupedByParent = allPermissions.stream()
                .collect(Collectors.groupingBy(p -> p.getParentId() != null ? p.getParentId() : 0L));

        return buildTreeRecursive(groupedByParent, parentId);
    }

    private List<PermissionVO> buildTreeRecursive(Map<Long, List<Permission>> groupedByParent, Long parentId) {
        List<Permission> children = groupedByParent.get(parentId);
        if (children == null || children.isEmpty()) {
            return new ArrayList<>();
        }

        return children.stream()
                .map(permission -> {
                    PermissionVO vo = PermissionVO.fromEntity(permission);
                    List<PermissionVO> childVos = buildTreeRecursive(groupedByParent, permission.getId());
                    if (!childVos.isEmpty()) {
                        vo.setChildren(childVos);
                    }
                    return vo;
                })
                .collect(Collectors.toList());
    }

}
