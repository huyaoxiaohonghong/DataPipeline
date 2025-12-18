package com.antigravity.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token 工具类
 * <p>
 * 简单的 Token 生成和验证（生产环境建议使用 JWT）
 * </p>
 */
public class TokenUtil {

    /**
     * Token 有效期（秒）：7天
     */
    public static final long TOKEN_EXPIRE_SECONDS = 7 * 24 * 60 * 60;

    /**
     * Token 存储（生产环境应使用 Redis）
     */
    private static final ConcurrentHashMap<String, TokenInfo> TOKEN_STORE = new ConcurrentHashMap<>();

    /**
     * 用户ID -> Token 映射（用于单点登录）
     */
    private static final ConcurrentHashMap<Long, String> USER_TOKEN_MAP = new ConcurrentHashMap<>();

    /**
     * Token 信息
     */
    public record TokenInfo(Long userId, String username, String role, long expireTime) {
        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    /**
     * 生成 Token
     */
    public static String generateToken(Long userId, String username, String role) {
        // 移除旧 Token（实现单点登录）
        String oldToken = USER_TOKEN_MAP.get(userId);
        if (oldToken != null) {
            TOKEN_STORE.remove(oldToken);
        }

        // 生成新 Token
        String token = createTokenString(userId, username);
        long expireTime = System.currentTimeMillis() + TOKEN_EXPIRE_SECONDS * 1000;

        // 存储 Token
        TOKEN_STORE.put(token, new TokenInfo(userId, username, role, expireTime));
        USER_TOKEN_MAP.put(userId, token);

        return token;
    }

    /**
     * 验证 Token
     */
    public static TokenInfo validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        TokenInfo info = TOKEN_STORE.get(token);
        if (info == null) {
            return null;
        }

        // 检查是否过期
        if (info.isExpired()) {
            removeToken(token);
            return null;
        }

        return info;
    }

    /**
     * 移除 Token
     */
    public static void removeToken(String token) {
        TokenInfo info = TOKEN_STORE.remove(token);
        if (info != null) {
            USER_TOKEN_MAP.remove(info.userId());
        }
    }

    /**
     * 根据用户ID移除 Token
     */
    public static void removeTokenByUserId(Long userId) {
        String token = USER_TOKEN_MAP.remove(userId);
        if (token != null) {
            TOKEN_STORE.remove(token);
        }
    }

    /**
     * 创建 Token 字符串
     */
    private static String createTokenString(Long userId, String username) {
        String raw = userId + ":" + username + ":" + UUID.randomUUID() + ":" + System.currentTimeMillis();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            // Fallback to UUID
            return UUID.randomUUID().toString().replace("-", "");
        }
    }

}
