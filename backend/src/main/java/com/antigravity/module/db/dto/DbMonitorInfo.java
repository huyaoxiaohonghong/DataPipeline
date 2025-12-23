package com.antigravity.module.db.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 数据库监控信息
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DbMonitorInfo {

    /**
     * 连接状态: CONNECTED, DISCONNECTED, ERROR
     */
    private String status;

    /**
     * 数据库版本
     */
    private String version;

    /**
     * 当前连接数
     */
    private Integer activeConnections;

    /**
     * 最大连接数
     */
    private Integer maxConnections;

    /**
     * 数据库大小（MB）
     */
    private Double databaseSizeMB;

    /**
     * 表数量
     */
    private Integer tableCount;

    /**
     * 表列表
     */
    private List<String> tables;

    /**
     * 错误信息（如果有）
     */
    private String errorMessage;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTimeMs;

}
