package com.antigravity.module.auth;

import com.antigravity.common.BusinessException;
import com.antigravity.common.security.JwtUtil;
import com.antigravity.module.auth.dto.LoginRequest;
import com.antigravity.module.auth.dto.LoginResponse;
import com.antigravity.module.user.User;
import com.antigravity.module.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    // 临时使用 MD5 加密（与现有用户密码兼容）
    private static final String SALT = "Antigravity@2024";

    @Override
    public LoginResponse login(LoginRequest request) {
        // 参数校验
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw BusinessException.of(400, "用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw BusinessException.of(400, "密码不能为空");
        }

        // 查询用户
        User user = userService.findByUsername(request.getUsername())
                .orElseThrow(() -> BusinessException.of(401, "用户名或密码错误"));

        // 验证密码（使用 MD5 + Salt）
        String encryptedPassword = encryptPassword(request.getPassword());
        if (!encryptedPassword.equals(user.getPassword())) {
            log.warn("用户登录失败，密码错误: username={}", request.getUsername());
            throw BusinessException.of(401, "用户名或密码错误");
        }

        // 生成 JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        log.info("用户登录成功: username={}, userId={}", user.getUsername(), user.getId());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpiration())
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    @Override
    public void logout(String token) {
        // JWT 是无状态的，客户端删除 Token 即可
        // 如果需要服务端失效，可以将 Token 加入黑名单（存入 Redis）
        log.info("用户登出");
    }

    @Override
    public LoginResponse getCurrentUser(String token) {
        if (token == null || token.isEmpty()) {
            throw BusinessException.of(401, "未登录");
        }

        // 移除 Bearer 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (!jwtUtil.validateToken(token)) {
            throw BusinessException.of(401, "登录已过期，请重新登录");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpiration())
                .userId(userId)
                .username(username)
                .role(role)
                .build();
    }

    /**
     * 加密密码（MD5 + Salt）
     */
    private String encryptPassword(String password) {
        String saltedPassword = password + SALT;
        return DigestUtils.md5DigestAsHex(saltedPassword.getBytes(StandardCharsets.UTF_8));
    }

}
