package com.antigravity.module.captcha;

import com.antigravity.common.Result;
import com.antigravity.module.captcha.dto.CaptchaResponse;
import com.antigravity.module.captcha.dto.CaptchaVerifyRequest;
import com.antigravity.module.captcha.dto.CaptchaVerifyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    @GetMapping("/generate")
    public Result<CaptchaResponse> generate() {
        return Result.success(captchaService.generateCaptcha());
    }

    @PostMapping("/verify")
    public Result<CaptchaVerifyResponse> verify(@RequestBody CaptchaVerifyRequest request) {
        CaptchaVerifyResponse response = captchaService.verifyCaptcha(request);
        if (response.getSuccess()) {
            return Result.success(response);
        } else {
            return Result.error(400, response.getMessage());
        }
    }
}
