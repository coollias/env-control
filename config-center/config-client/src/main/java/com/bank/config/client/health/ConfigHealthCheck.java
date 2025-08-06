package com.bank.config.client.health;

import com.bank.config.client.ConfigClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置健康检查类
 * 检查配置客户端的健康状态
 * 
 * @author bank
 */
public class ConfigHealthCheck {
    private static final Logger logger = LoggerFactory.getLogger(ConfigHealthCheck.class);

    private final ConfigClient configClient;
    private final long healthCheckTimeout;

    public ConfigHealthCheck(ConfigClient configClient) {
        this(configClient, 5000); // 默认5秒超时
    }

    public ConfigHealthCheck(ConfigClient configClient, long healthCheckTimeout) {
        this.configClient = configClient;
        this.healthCheckTimeout = healthCheckTimeout;
    }

    /**
     * 检查客户端是否健康
     */
    public boolean isHealthy() {
        try {
            // 检查配置中心连接
            return checkServerConnection() && 
                   checkCacheHealth() && 
                   checkPollerHealth();
        } catch (Exception e) {
            logger.error("健康检查失败", e);
            return false;
        }
    }

    /**
     * 获取详细的健康状态
     */
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // 检查各个组件
            boolean serverConnection = checkServerConnection();
            boolean cacheHealth = checkCacheHealth();
            boolean pollerHealth = checkPollerHealth();
            
            health.put("overall", serverConnection && cacheHealth && pollerHealth);
            health.put("serverConnection", serverConnection);
            health.put("cacheHealth", cacheHealth);
            health.put("pollerHealth", pollerHealth);
            
            // 添加指标信息
            Map<String, Object> metrics = configClient.getMetrics().getHealthStatus();
            health.put("metrics", metrics);
            
            // 添加配置信息
            health.put("appCode", configClient.getAppCode());
            health.put("envCode", configClient.getEnvCode());
            health.put("serverUrl", configClient.getServerUrl());
            health.put("cacheSize", configClient.getCache().size());
            health.put("pollerRunning", configClient.getPoller().isRunning());
            
        } catch (Exception e) {
            logger.error("获取健康状态失败", e);
            health.put("overall", false);
            health.put("error", e.getMessage());
        }
        
        return health;
    }

    /**
     * 检查服务器连接
     */
    private boolean checkServerConnection() {
        try {
            // 尝试获取一个简单的配置项来测试连接
            String testKey = "health.check";
            String result = configClient.getConfig(testKey);
            
            // 如果能够正常调用API，说明连接正常
            // 即使配置项不存在，也应该返回null而不是抛出异常
            return true;
        } catch (Exception e) {
            logger.warn("服务器连接检查失败", e);
            return false;
        }
    }

    /**
     * 检查缓存健康状态
     */
    private boolean checkCacheHealth() {
        try {
            // 检查缓存是否可用
            configClient.getCache().get("test");
            return true;
        } catch (Exception e) {
            logger.warn("缓存健康检查失败", e);
            return false;
        }
    }

    /**
     * 检查拉取器健康状态
     */
    private boolean checkPollerHealth() {
        try {
            // 检查拉取器是否正常运行
            return configClient.getPoller().isRunning();
        } catch (Exception e) {
            logger.warn("拉取器健康检查失败", e);
            return false;
        }
    }

    /**
     * 执行完整的健康检查
     */
    public HealthCheckResult performHealthCheck() {
        HealthCheckResult result = new HealthCheckResult();
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 检查服务器连接
            boolean serverOk = checkServerConnection();
            result.setServerConnection(serverOk);
            
            // 检查缓存
            boolean cacheOk = checkCacheHealth();
            result.setCacheHealth(cacheOk);
            
            // 检查拉取器
            boolean pollerOk = checkPollerHealth();
            result.setPollerHealth(pollerOk);
            
            // 检查指标
            Map<String, Object> metrics = configClient.getMetrics().getHealthStatus();
            result.setMetrics(metrics);
            
            // 设置总体状态
            result.setHealthy(serverOk && cacheOk && pollerOk);
            
        } catch (Exception e) {
            logger.error("健康检查执行失败", e);
            result.setHealthy(false);
            result.setError(e.getMessage());
        }
        
        result.setCheckTime(System.currentTimeMillis() - startTime);
        return result;
    }

    /**
     * 健康检查结果类
     */
    public static class HealthCheckResult {
        private boolean healthy;
        private boolean serverConnection;
        private boolean cacheHealth;
        private boolean pollerHealth;
        private Map<String, Object> metrics;
        private String error;
        private long checkTime;

        public boolean isHealthy() {
            return healthy;
        }

        public void setHealthy(boolean healthy) {
            this.healthy = healthy;
        }

        public boolean isServerConnection() {
            return serverConnection;
        }

        public void setServerConnection(boolean serverConnection) {
            this.serverConnection = serverConnection;
        }

        public boolean isCacheHealth() {
            return cacheHealth;
        }

        public void setCacheHealth(boolean cacheHealth) {
            this.cacheHealth = cacheHealth;
        }

        public boolean isPollerHealth() {
            return pollerHealth;
        }

        public void setPollerHealth(boolean pollerHealth) {
            this.pollerHealth = pollerHealth;
        }

        public Map<String, Object> getMetrics() {
            return metrics;
        }

        public void setMetrics(Map<String, Object> metrics) {
            this.metrics = metrics;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public long getCheckTime() {
            return checkTime;
        }

        public void setCheckTime(long checkTime) {
            this.checkTime = checkTime;
        }

        @Override
        public String toString() {
            return String.format("HealthCheckResult{healthy=%s, serverConnection=%s, cacheHealth=%s, pollerHealth=%s, checkTime=%dms}",
                    healthy, serverConnection, cacheHealth, pollerHealth, checkTime);
        }
    }
} 