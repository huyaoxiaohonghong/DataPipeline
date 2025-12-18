package com.antigravity.module.user;

import com.antigravity.common.PageResult;
import com.antigravity.common.Result;
import com.antigravity.module.user.dto.ChangePasswordRequest;
import com.antigravity.module.user.dto.UserCreateRequest;
import com.antigravity.module.user.dto.UserUpdateRequest;
import com.antigravity.module.user.dto.UserVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理 Controller
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public Result<PageResult<UserVO>> list(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码最小为 1") int pageNumber,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页大小最小为 1") int pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role) {
        PageResult<User> userPage = userService.pageQuery(pageNumber, pageSize, username, role);
        PageResult<UserVO> voPage = PageResult.of(
                userPage.getRecords().stream().map(UserVO::fromEntity).toList(),
                userPage.getPageNumber(),
                userPage.getPageSize(),
                userPage.getTotalRow());
        return Result.success(voPage);
    }

    @GetMapping("/{id}")
    public Result<UserVO> getById(@PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id) {
        return userService.findById(id)
                .map(UserVO::fromEntity)
                .map(Result::success)
                .orElse(Result.notFound("用户不存在"));
    }

    @GetMapping("/username/{username}")
    public Result<UserVO> getByUsername(@PathVariable String username) {
        return userService.findByUsername(username)
                .map(UserVO::fromEntity)
                .map(Result::success)
                .orElse(Result.notFound("用户不存在"));
    }

    @GetMapping("/role/{role}")
    public Result<List<UserVO>> listByRole(@PathVariable String role) {
        List<UserVO> users = userService.findByRole(role)
                .stream()
                .map(UserVO::fromEntity)
                .toList();
        return Result.success(users);
    }

    @PostMapping
    public Result<UserVO> create(@RequestBody @Valid UserCreateRequest request) {
        // 检查用户名是否存在
        if (userService.existsByUsername(request.getUsername())) {
            return Result.badRequest("用户名已存在");
        }
        // 检查邮箱是否存在
        if (request.getEmail() != null && !request.getEmail().isEmpty()
                && userService.existsByEmail(request.getEmail())) {
            return Result.badRequest("邮箱已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());

        User createdUser = userService.createUser(user);
        return Result.success("用户创建成功", UserVO.fromEntity(createdUser));
    }

    @PutMapping("/{id}")
    public Result<UserVO> update(
            @PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id,
            @RequestBody @Valid UserUpdateRequest request) {
        User existingUser = userService.findById(id).orElse(null);
        if (existingUser == null) {
            return Result.notFound("用户不存在");
        }

        // 检查用户名是否存在
        if (request.getUsername() != null && !request.getUsername().isEmpty()
                && !request.getUsername().equals(existingUser.getUsername())
                && userService.existsByUsername(request.getUsername())) {
            return Result.badRequest("用户名已存在");
        }

        // 检查邮箱是否存在
        if (request.getEmail() != null && !request.getEmail().isEmpty()
                && !request.getEmail().equals(existingUser.getEmail())
                && userService.existsByEmail(request.getEmail())) {
            return Result.badRequest("邮箱已存在");
        }

        User user = new User();
        user.setId(id);
        user.setUsername(request.getUsername() != null ? request.getUsername() : existingUser.getUsername());
        user.setEmail(request.getEmail() != null ? request.getEmail() : existingUser.getEmail());
        user.setRole(request.getRole() != null ? request.getRole() : existingUser.getRole());

        boolean success = userService.updateUser(user);
        if (success) {
            User updatedUser = userService.findById(id).orElse(null);
            return Result.success("用户更新成功", UserVO.fromEntity(updatedUser));
        }
        return Result.error("用户更新失败");
    }

    @PatchMapping("/{id}/role")
    public Result<Void> updateRole(
            @PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id,
            @RequestParam String role) {
        if (role == null || role.isBlank()) {
            return Result.badRequest("角色不能为空");
        }
        if (!role.matches("^(ADMIN|USER|GUEST)$")) {
            return Result.badRequest("角色必须是 ADMIN、USER 或 GUEST");
        }
        boolean success = userService.updateRole(id, role);
        return success ? Result.success() : Result.error("更新角色失败");
    }

    @PatchMapping("/{id}/password")
    public Result<Void> changePassword(
            @PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id,
            @RequestBody @Valid ChangePasswordRequest request) {
        boolean success = userService.changePassword(id, request.getOldPassword(), request.getNewPassword());
        return success ? Result.success("密码修改成功", null) : Result.error("密码修改失败，请检查旧密码是否正确");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @Min(value = 1, message = "ID 必须大于 0") Long id) {
        boolean success = userService.deleteById(id);
        return success ? Result.success("用户删除成功", null) : Result.error("用户删除失败");
    }

    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody @NotEmpty(message = "请选择要删除的用户") List<Long> ids) {
        boolean success = userService.deleteByIds(ids);
        return success ? Result.success("批量删除成功", null) : Result.error("批量删除失败");
    }

    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        return Result.success(!exists);
    }

    @GetMapping("/check-email")
    public Result<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return Result.success(!exists);
    }

}
