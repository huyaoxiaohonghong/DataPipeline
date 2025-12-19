package com.antigravity.module.captcha;

import com.antigravity.module.captcha.dto.CaptchaResponse;
import com.antigravity.module.captcha.dto.CaptchaVerifyRequest;
import com.antigravity.module.captcha.dto.CaptchaVerifyResponse;

/**
 * 验证码服务接口
 */
public interface CaptchaService {

    /**
     * 生成滑动验证码
     *
     * @return 验证码信息（包含背景图、滑块图、滑块Y位置）
     */
    CaptchaResponse generateCaptcha();

    /**
     * 校验滑动验证码
     *
     * @param request 校验请求
     * @return 校验结果
     */
    CaptchaVerifyResponse verifyCaptcha(CaptchaVerifyRequest request);

    /**
     * 验证Token是否有效
     *
     * @param token 验证token
     * @return 是否有效
     */
    boolean validateToken(String token);

    /**
     * 消费验证Token（一次性使用）
     *
     * @param token 验证token
     * @return 是否消费成功
     */
    boolean consumeToken(String token);

}
