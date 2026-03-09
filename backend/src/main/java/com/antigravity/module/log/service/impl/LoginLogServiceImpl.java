package com.antigravity.module.log.service.impl;

import com.antigravity.module.log.entity.LoginLog;
import com.antigravity.module.log.mapper.LoginLogMapper;
import com.antigravity.module.log.service.LoginLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class LoginLogServiceImpl extends ServiceImpl<LoginLogMapper, LoginLog> implements LoginLogService {

    @Override
    public Page<LoginLog> getLoginLogPage(int pageNum, int pageSize, String username, Integer status, String startTime,
            String endTime) {
        Page<LoginLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(username), LoginLog::getUsername, username)
                .eq(status != null, LoginLog::getStatus, status);

        if (StringUtils.hasText(startTime) && StringUtils.hasText(endTime)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            wrapper.between(LoginLog::getLoginTime, LocalDateTime.parse(startTime, formatter),
                    LocalDateTime.parse(endTime, formatter));
        }

        wrapper.orderByDesc(LoginLog::getLoginTime);
        return this.page(page, wrapper);
    }

    @Async
    @Override
    public void asyncRecordLoginLog(String username, String ipAddress, String userAgent, boolean success,
            String message) {
        LoginLog log = new LoginLog();
        log.setUsername(username);
        log.setIpAddress(ipAddress);

        // Simple UA parsing
        String browser = "Unknown";
        String os = "Unknown";
        if (userAgent != null) {
            if (userAgent.contains("MSIE") || userAgent.contains("Trident"))
                browser = "IE";
            else if (userAgent.contains("Edge") || userAgent.contains("Edg"))
                browser = "Edge";
            else if (userAgent.contains("Chrome"))
                browser = "Chrome";
            else if (userAgent.contains("Firefox"))
                browser = "Firefox";
            else if (userAgent.contains("Safari"))
                browser = "Safari";

            if (userAgent.toLowerCase().contains("windows"))
                os = "Windows";
            else if (userAgent.toLowerCase().contains("mac"))
                os = "Mac OS";
            else if (userAgent.toLowerCase().contains("linux"))
                os = "Linux";
            else if (userAgent.toLowerCase().contains("android"))
                os = "Android";
            else if (userAgent.toLowerCase().contains("iphone"))
                os = "iOS";
        }

        log.setBrowser(browser);
        log.setOs(os);
        log.setStatus(success ? 1 : 0);
        log.setMessage(message);
        log.setLoginTime(LocalDateTime.now());

        this.save(log);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        this.removeByIds(ids);
    }

    @Override
    public void cleanAll() {
        this.remove(new LambdaQueryWrapper<>());
    }
}
