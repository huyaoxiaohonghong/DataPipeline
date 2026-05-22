package com.antigravity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * SeaTunnel 配置属性
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "seatunnel.api")
public class SeaTunnelProperties {

    /**
     * SeaTunnel REST API 基础地址
     */
    private String baseUrl = "http://localhost:5801";

    /**
     * 请求超时时间（毫秒）
     */
    private int timeout = 30000;

}
