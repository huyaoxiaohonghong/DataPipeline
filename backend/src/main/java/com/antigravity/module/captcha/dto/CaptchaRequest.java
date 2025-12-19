package com.antigravity.module.captcha.dto;

import lombok.Data;

/**
 * 验证码请求
 */
@Data
public class CaptchaRequest {

    /**
     * 验证类型：slide(滑动验证)
     */
    private String type = "slide";

}
