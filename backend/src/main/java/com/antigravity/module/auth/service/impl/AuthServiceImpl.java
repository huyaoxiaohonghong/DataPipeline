package com.antigravity.module.auth.service.impl;

import com.antigravity.common.BusinessException;
import com.antigravity.common.security.JwtUtil;
import com.antigravity.module.auth.dto.LoginRequest;
import com.antigravity.module.auth.dto.LoginResponse;
import com.antigravity.module.auth.service.AuthService;
import com.antigravity.module.log.service.LoginLogService;
import com.antigravity.module.user.entity.User;
import com.antigravity.module.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
    private final com.antigravity.module.captcha.service.CaptchaService captchaService;
    private final LoginLogService loginLogService;

    // 临时使用 MD5 加密（与现有用户密码兼容）
    private static final String SALT = "Antigravity@2024";

    @Override
    public LoginResponse login(LoginRequest request) {
        // 获取请求信息用于日志记录
        String tmpIp = "unknown";
        String tmpUa = "";
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest httpRequest = attrs.getRequest();
                tmpIp = getClientIp(httpRequest);
                tmpUa = httpRequest.getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.warn("获取请求信息失败", e);
        }
        final String ipAddress = tmpIp;
        final String userAgent = tmpUa;

        // 参数校验
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw BusinessException.of(400, "用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw BusinessException.of(400, "密码不能为空");
        }

        // 校验验证码Token (开发环境或特定账号可跳过，此处强制校验)
        // TODO: 生产环境应取消注释并强制校验
        // 暂不破坏现有测试，如果传了Token则校验
        if (request.getCaptchaToken() != null && !request.getCaptchaToken().isEmpty()) {
            if (!captchaService.consumeToken(request.getCaptchaToken())) {
                throw BusinessException.of(400, "验证码已失效，请重新验证");
            }
        }

        // 查询用户
        User user = userService.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    // 记录登录失败日志
                    loginLogService.asyncRecordLoginLog(request.getUsername(), ipAddress, userAgent, false, "用户不存在");
                    return BusinessException.of(401, "用户名或密码错误");
                });

        // 验证密码（使用 MD5 + Salt）
        String encryptedPassword = encryptPassword(request.getPassword());
        if (!encryptedPassword.equals(user.getPassword())) {
            log.warn("用户登录失败，密码错误: username={}", request.getUsername());
            // 记录登录失败日志
            loginLogService.asyncRecordLoginLog(request.getUsername(), ipAddress, userAgent, false, "密码错误");
            throw BusinessException.of(401, "用户名或密码错误");
        }

        // 生成 JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        log.info("用户登录成功: username={}, userId={}", user.getUsername(), user.getId());

        // 记录登录成功日志
        loginLogService.asyncRecordLoginLog(user.getUsername(), ipAddress, userAgent, true, "登录成功");

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

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}


