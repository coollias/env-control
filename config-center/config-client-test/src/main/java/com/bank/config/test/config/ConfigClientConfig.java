package com.bank.config.test.config;

import com.bank.config.client.ConfigClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置客户端配置类
 * 
 * @author bank
 */
@Configuration
public class ConfigClientConfig {
    
    @Bean
    public ConfigClient configClient() {
        return new ConfigClient.ConfigClientBuilder()
            .serverUrl("http://localhost:8080")
            .appCode("1003")
            .envCode("dev")
            .token("Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEwLCJzdWIiOiJjb29sbGlhcyIsImlhdCI6MTc1NDQzOTg1NCwiZXhwIjoxODQwODM5ODU0fQ.joAFmm_rW3Nm_w6848RHIuotBAofqAvjsooC0jzr17A")
            .pollInterval(30000)
            .cacheFile("/Users/coollias/Documents/Code/env/env-control/config-center/config-client-test/src/main/resources/config-client-test.yaml")
            .enablePolling(true)
            .enableCache(true)
            .build();
    }
} 