package com.antigravity.module.captcha.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 验证码校验请求
 */
@Data
public class CaptchaVerifyRequest {

    /**
     * 验证码唯一标识
     */
    @NotBlank(message = "验证码标识不能为空")
    private String captchaId;

    /**
     * 滑块X轴位置（像素）
     */
    @NotNull(message = "滑动距离不能为空")
    private Integer sliderX;

}
