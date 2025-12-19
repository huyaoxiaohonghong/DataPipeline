package com.antigravity.module.captcha;

import com.antigravity.common.BusinessException;
import com.antigravity.module.captcha.dto.CaptchaResponse;
import com.antigravity.module.captcha.dto.CaptchaVerifyRequest;
import com.antigravity.module.captcha.dto.CaptchaVerifyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private final StringRedisTemplate redisTemplate;

    private static final String CAPTCHA_KEY_PREFIX = "captcha:slide:";
    private static final String VERIFIED_TOKEN_PREFIX = "captcha:verified:";
    private static final int EXPIRE_SECONDS = 300; // 5分钟过期
    private static final int TOLERANCE = 5; // 允许均误差像素

    // 图片配置
    private static final int WIDTH = 300;
    private static final int HEIGHT = 150;
    private static final int SLIDER_WIDTH = 50;
    private static final int SLIDER_HEIGHT = 50;

    @Override
    public CaptchaResponse generateCaptcha() {
        String captchaId = UUID.randomUUID().toString();

        try {
            // 1. 生成背景图
            BufferedImage bgImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D gbg = bgImage.createGraphics();

            // 绘制随机背景
            drawRandomBackground(gbg, WIDTH, HEIGHT);

            // 2. 计算随机切片位置
            Random random = new Random();
            int targetX = random.nextInt(WIDTH - SLIDER_WIDTH - 20) + 10; // 留出边距
            int targetY = random.nextInt(HEIGHT - SLIDER_HEIGHT - 10) + 5;

            // 3. 生成滑块图 (透明背景)
            BufferedImage sliderImage = new BufferedImage(SLIDER_WIDTH, SLIDER_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gslider = sliderImage.createGraphics();

            // 4. 抠图逻辑：简单矩形演示，实际可用 Path 绘制拼图形状
            // 从背景图中截取对应位置的图像到滑块
            BufferedImage cutImage = bgImage.getSubimage(targetX, targetY, SLIDER_WIDTH, SLIDER_HEIGHT);
            gslider.drawImage(cutImage, 0, 0, null);

            // 给滑块画个边框
            gslider.setColor(Color.LIGHT_GRAY);
            gslider.setStroke(new BasicStroke(1));
            gslider.drawRect(0, 0, SLIDER_WIDTH - 1, SLIDER_HEIGHT - 1);

            // 5. 在背景图对应位置绘制阴影/缺口
            gbg.setColor(new Color(0, 0, 0, 100)); // 半透明黑
            gbg.fillRect(targetX, targetY, SLIDER_WIDTH, SLIDER_HEIGHT);

            // 释放资源
            gbg.dispose();
            gslider.dispose();

            // 6. 转 Base64
            String bgBase64 = imageToBase64(bgImage);
            String sliderBase64 = imageToBase64(sliderImage);

            // 7. 存入 Redis
            redisTemplate.opsForValue().set(CAPTCHA_KEY_PREFIX + captchaId, String.valueOf(targetX), EXPIRE_SECONDS,
                    TimeUnit.SECONDS);

            return CaptchaResponse.builder()
                    .captchaId(captchaId)
                    .backgroundImage("data:image/png;base64," + bgBase64)
                    .sliderImage("data:image/png;base64," + sliderBase64)
                    .sliderY(targetY)
                    .build();

        } catch (Exception e) {
            log.error("生成验证码失败", e);
            throw BusinessException.of(500, "生成验证码失败");
        }
    }

    @Override
    public CaptchaVerifyResponse verifyCaptcha(CaptchaVerifyRequest request) {
        String key = CAPTCHA_KEY_PREFIX + request.getCaptchaId();
        String targetXStr = redisTemplate.opsForValue().get(key);

        if (targetXStr == null) {
            return CaptchaVerifyResponse.builder().success(false).message("验证码已过期").build();
        }

        int targetX = Integer.parseInt(targetXStr);
        int diff = Math.abs(targetX - request.getSliderX());

        // 验证完成后删除缓存，防止重放
        redisTemplate.delete(key);

        if (diff <= TOLERANCE) {
            // 生成通过凭证 Token
            String token = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set(VERIFIED_TOKEN_PREFIX + token, "true", EXPIRE_SECONDS, TimeUnit.SECONDS);

            return CaptchaVerifyResponse.builder()
                    .success(true)
                    .token(token)
                    .message("验证通过")
                    .build();
        }

        return CaptchaVerifyResponse.builder()
                .success(false)
                .message("验证失败，偏移量过大")
                .build();
    }

    @Override
    public boolean validateToken(String token) {
        if (token == null)
            return false;
        return Boolean.TRUE.equals(redisTemplate.hasKey(VERIFIED_TOKEN_PREFIX + token));
    }

    @Override
    public boolean consumeToken(String token) {
        if (token == null)
            return false;
        return Boolean.TRUE.equals(redisTemplate.delete(VERIFIED_TOKEN_PREFIX + token));
    }

    private void drawRandomBackground(Graphics2D g, int width, int height) {
        Random random = new Random();
        // 渐变背景
        g.setPaint(new GradientPaint(0, 0, getRandomColor(), width, height, getRandomColor()));
        g.fillRect(0, 0, width, height);

        // 绘制干扰线
        for (int i = 0; i < 20; i++) {
            g.setColor(getRandomColor());
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = random.nextInt(width);
            int y2 = random.nextInt(height);
            g.drawLine(x1, y1, x2, y2);
        }

        // 绘制噪点
        for (int i = 0; i < 100; i++) {
            g.setColor(getRandomColor());
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            g.drawOval(x, y, 2, 2);
        }
    }

    private Color getRandomColor() {
        Random random = new Random();
        return new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255));
    }

    private String imageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}
