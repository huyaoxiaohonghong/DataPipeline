package com.antigravity.module.sync.controller;

import com.antigravity.common.Result;
import com.antigravity.module.sync.engine.SeaTunnelClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * SeaTunnel 集群管理 Controller
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/seatunnel")
@RequiredArgsConstructor
public class SeaTunnelController {

    private final SeaTunnelClient seaTunnelClient;

    /**
     * 获取 SeaTunnel 集群概览
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> overview = seaTunnelClient.getClusterOverview();
        return Result.success(overview);
    }

    /**
     * 获取 SeaTunnel 运行中的任务
     */
    @GetMapping("/running-jobs")
    public Result<List<Map<String, Object>>> runningJobs() {
        List<Map<String, Object>> jobs = seaTunnelClient.getRunningJobs();
        return Result.success(jobs);
    }

}
