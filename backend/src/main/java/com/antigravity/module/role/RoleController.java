package com.antigravity.module.role;

import com.antigravity.common.PageResult;
import com.antigravity.common.Result;
import com.antigravity.module.role.dto.RoleCreateRequest;
import com.antigravity.module.role.dto.RoleUpdateRequest;
import com.antigravity.module.role.dto.RoleVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理 Controller
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * 分页查询角色列表
     */
    @GetMapping
    public Result<PageResult<RoleVO>> list(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码最小为 1") int pageNumber,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小最小为 1") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Boolean enabled) {
        PageResult<Role> rolePage = roleService.pageQuery(pageNumber, pageSize, name, code, enabled);
        PageResult<RoleVO> voPage = PageResult.of(
                rolePage.getRecords().stream().map(RoleVO::fromEntity).toList(),
                rolePage.getPageNumber(),
                rolePage.getPageSize(),
                rolePage.getTotalRow());
        return Result.success(voPage);
    }

    /**
     * 获取所有角色（用于下拉选择）
     */
    @GetMapping("/all")
    public Result<List<RoleVO>> listAll() {
        List<RoleVO> roles = roleService.findAll()
                .stream()
                .map(RoleVO::fromEntity)
                .toList();
        return Result.success(roles);
    }

    /**
     * 获取所有启用的角色
     */
    @GetMapping("/enabled")
    public Result<List<RoleVO>> listEnabled() {
        List<RoleVO> roles = roleService.findEnabled()
                .stream()
                .map(RoleVO::fromEntity)
                .toList();
        return Result.success(roles);
    }

    /**
     * 根据ID查询角色
     */
    @GetMapping("/{id}")
    public Result<RoleVO> getById(@PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id) {
        return roleService.findById(id)
                .map(RoleVO::fromEntity)
                .map(Result::success)
                .orElse(Result.notFound("角色不存在"));
    }

    /**
     * 根据编码查询角色
     */
    @GetMapping("/code/{code}")
    public Result<RoleVO> getByCode(@PathVariable String code) {
        return roleService.findByCode(code)
                .map(RoleVO::fromEntity)
                .map(Result::success)
                .orElse(Result.notFound("角色不存在"));
    }

    /**
     * 创建角色
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<RoleVO> create(@RequestBody @Valid RoleCreateRequest request) {
        // 检查编码是否存在
        if (roleService.existsByCode(request.getCode())) {
            return Result.badRequest("角色编码已存在");
        }

        Role role = new Role();
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setSort(request.getSort());
        role.setEnabled(request.getEnabled());

        Role createdRole = roleService.createRole(role);
        return Result.success("角色创建成功", RoleVO.fromEntity(createdRole));
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<RoleVO> update(
            @PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id,
            @RequestBody @Valid RoleUpdateRequest request) {
        Role existingRole = roleService.findById(id).orElse(null);
        if (existingRole == null) {
            return Result.notFound("角色不存在");
        }

        // 系统内置角色不允许修改
        if (isBuiltInRole(existingRole.getCode())) {
            return Result.badRequest("系统内置角色不允许修改");
        }

        Role role = new Role();
        role.setId(id);
        role.setCode(existingRole.getCode()); // 编码不允许修改
        role.setName(request.getName() != null ? request.getName() : existingRole.getName());
        role.setDescription(
                request.getDescription() != null ? request.getDescription() : existingRole.getDescription());
        role.setSort(request.getSort() != null ? request.getSort() : existingRole.getSort());
        role.setEnabled(request.getEnabled() != null ? request.getEnabled() : existingRole.getEnabled());

        boolean success = roleService.updateRole(role);
        if (success) {
            Role updatedRole = roleService.findById(id).orElse(null);
            return Result.success("角色更新成功", RoleVO.fromEntity(updatedRole));
        }
        return Result.error("角色更新失败");
    }

    /**
     * 更新角色状态
     */
    @PatchMapping("/{id}/enabled")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateEnabled(
            @PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id,
            @RequestParam Boolean enabled) {
        Role existingRole = roleService.findById(id).orElse(null);
        if (existingRole == null) {
            return Result.notFound("角色不存在");
        }

        // 系统内置角色不允许禁用
        if (isBuiltInRole(existingRole.getCode()) && !enabled) {
            return Result.badRequest("系统内置角色不允许禁用");
        }

        boolean success = roleService.updateEnabled(id, enabled);
        return success ? Result.success() : Result.error("更新状态失败");
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id) {
        Role existingRole = roleService.findById(id).orElse(null);
        if (existingRole == null) {
            return Result.notFound("角色不存在");
        }

        // 系统内置角色不允许删除
        if (isBuiltInRole(existingRole.getCode())) {
            return Result.badRequest("系统内置角色不允许删除");
        }

        boolean success = roleService.deleteById(id);
        return success ? Result.success("角色删除成功", null) : Result.error("角色删除失败");
    }

    /**
     * 批量删除角色
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> batchDelete(@RequestBody @NotEmpty(message = "请选择要删除的角色") List<Long> ids) {
        boolean success = roleService.deleteByIds(ids);
        return success ? Result.success("批量删除成功", null) : Result.error("批量删除失败");
    }

    /**
     * 检查角色编码是否可用
     */
    @GetMapping("/check-code")
    public Result<Boolean> checkCode(@RequestParam String code) {
        boolean exists = roleService.existsByCode(code);
        return Result.success(!exists);
    }

    /**
     * 判断是否为内置角色
     */
    private boolean isBuiltInRole(String code) {
        return "ADMIN".equals(code) || "USER".equals(code) || "GUEST".equals(code);
    }

}
