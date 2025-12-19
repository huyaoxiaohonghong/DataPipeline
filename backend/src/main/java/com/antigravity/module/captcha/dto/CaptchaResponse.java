package com.antigravity.module.captcha.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaResponse {

    /**
     * 验证码唯一标识
     */
    private String captchaId;

    /**
     * 背景图片 Base64
     */
    private String backgroundImage;

    /**
     * 滑块图片 Base64
     */
    private String sliderImage;

    /**
     * 滑块Y轴位置（像素）
     */
    private Integer sliderY;

}
