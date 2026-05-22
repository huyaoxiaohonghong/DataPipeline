package com.antigravity.module.sync.engine;

import com.antigravity.common.BusinessException;
import com.antigravity.config.SeaTunnelProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * SeaTunnel REST API 客户端
 * <p>
 * 封装 SeaTunnel 2.3.3 Hazelcast REST API 调用：
 * - 提交任务
 * - 查询运行中任务
 * - 查询任务详情
 * - 停止任务
 * - 集群概览
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeaTunnelClient {

    private final RestTemplate restTemplate;
    private final SeaTunnelProperties properties;
    private final ObjectMapper objectMapper;

    // ==================== SeaTunnel 2.3.3 Hazelcast REST API 端点 ====================
    private static final String SUBMIT_JOB = "/hazelcast/rest/maps/submit-job";
    private static final String RUNNING_JOBS = "/hazelcast/rest/maps/running-jobs";
    private static final String RUNNING_JOB = "/hazelcast/rest/maps/running-job/";
    private static final String STOP_JOB = "/hazelcast/rest/maps/stop-job";
    private static final String OVERVIEW = "/hazelcast/rest/maps/overview";

    /**
     * 提交同步任务到 SeaTunnel
     *
     * @param jobConfigJson SeaTunnel Job 配置 JSON
     * @return 返回 jobId
     */
    public String submitJob(String jobConfigJson) {
        String url = properties.getBaseUrl() + SUBMIT_JOB;
        log.info("提交 SeaTunnel 任务: url={}", url);
        log.debug("任务配置: {}", jobConfigJson);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(jobConfigJson, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> result = objectMapper.readValue(
                        response.getBody(), new TypeReference<>() {});
                Object jobId = result.get("jobId");
                if (jobId != null) {
                    String jobIdStr = jobId.toString();
                    log.info("SeaTunnel 任务提交成功: jobId={}", jobIdStr);
                    return jobIdStr;
                }
                // 某些版本返回 jobName 作为标识
                Object jobName = result.get("jobName");
                if (jobName != null) {
                    log.info("SeaTunnel 任务提交成功: jobName={}", jobName);
                    return jobName.toString();
                }
                log.warn("SeaTunnel 返回结果中未找到 jobId: {}", response.getBody());
                throw BusinessException.of("任务提交成功但未获取到任务ID");
            }

            throw BusinessException.of("SeaTunnel 任务提交失败: HTTP " + response.getStatusCode());
        } catch (RestClientException e) {
            log.error("调用 SeaTunnel 提交任务接口失败: {}", e.getMessage());
            throw BusinessException.of("SeaTunnel 服务连接失败: " + e.getMessage());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("解析 SeaTunnel 响应失败: {}", e.getMessage());
            throw BusinessException.of("SeaTunnel 响应解析失败: " + e.getMessage());
        }
    }

    /**
     * 获取运行中的任务列表
     *
     * @return 任务列表（每项包含 jobId, jobName, jobStatus 等）
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getRunningJobs() {
        String url = properties.getBaseUrl() + RUNNING_JOBS;
        log.debug("查询 SeaTunnel 运行中任务: url={}", url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            }
            return Collections.emptyList();
        } catch (RestClientException e) {
            log.warn("查询 SeaTunnel 运行中任务失败: {}", e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("解析 SeaTunnel 运行中任务响应失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 获取指定任务的详情
     *
     * @param jobId SeaTunnel 任务ID
     * @return 任务详情 Map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getJobInfo(String jobId) {
        String url = properties.getBaseUrl() + RUNNING_JOB + jobId;
        log.debug("查询 SeaTunnel 任务详情: url={}", url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            }
            return Collections.emptyMap();
        } catch (RestClientException e) {
            log.warn("查询 SeaTunnel 任务详情失败: jobId={}, error={}", jobId, e.getMessage());
            return Collections.emptyMap();
        } catch (Exception e) {
            log.warn("解析 SeaTunnel 任务详情响应失败: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * 停止指定任务
     *
     * @param jobId SeaTunnel 任务ID
     * @return 是否成功
     */
    public boolean stopJob(String jobId) {
        String url = properties.getBaseUrl() + STOP_JOB;
        log.info("停止 SeaTunnel 任务: jobId={}", jobId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String body = objectMapper.writeValueAsString(Map.of("jobId", jobId));
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            boolean success = response.getStatusCode().is2xxSuccessful();
            log.info("停止 SeaTunnel 任务结果: jobId={}, success={}", jobId, success);
            return success;
        } catch (RestClientException e) {
            log.error("停止 SeaTunnel 任务失败: jobId={}, error={}", jobId, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("停止 SeaTunnel 任务异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取 SeaTunnel 集群概览
     *
     * @return 集群概览信息
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getClusterOverview() {
        String url = properties.getBaseUrl() + OVERVIEW;
        log.debug("查询 SeaTunnel 集群概览: url={}", url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            }
            return Collections.emptyMap();
        } catch (RestClientException e) {
            log.warn("查询 SeaTunnel 集群概览失败: {}", e.getMessage());
            return Collections.singletonMap("error", "SeaTunnel 服务不可用: " + e.getMessage());
        } catch (Exception e) {
            log.warn("解析 SeaTunnel 集群概览响应失败: {}", e.getMessage());
            return Collections.singletonMap("error", "响应解析失败: " + e.getMessage());
        }
    }

}
