package com.antigravity.module.auth;

import com.antigravity.module.auth.dto.LoginRequest;
import com.antigravity.module.auth.dto.LoginResponse;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户登出
     *
     * @param token 访问令牌
     */
    void logout(String token);

    /**
     * 获取当前用户信息
     *
     * @param token 访问令牌
     * @return 用户信息
     */
    LoginResponse getCurrentUser(String token);

}
