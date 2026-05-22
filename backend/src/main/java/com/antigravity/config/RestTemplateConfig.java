package com.antigravity.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 配置（用于调用 SeaTunnel REST API）
 *
 * @author Antigravity Team
 * @since 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    private final SeaTunnelProperties seaTunnelProperties;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(seaTunnelProperties.getTimeout());
        factory.setReadTimeout(seaTunnelProperties.getTimeout());
        return new RestTemplate(factory);
    }

}
