package com.antigravity.module.captcha.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码校验响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaVerifyResponse {

    /**
     * 是否验证成功
     */
    private Boolean success;

    /**
     * 验证Token（验证成功后用于登录）
     */
    private String token;

    /**
     * 提示消息
     */
    private String message;

}
