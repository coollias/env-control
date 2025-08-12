package com.bank.config.client.example;

import com.bank.config.client.ConfigClient;
import com.bank.config.client.hotupdate.ConfigValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Spring Boot集成的配置热更新示例
 * 展示如何在Spring应用中使用热更新功能
 * 
 * @author bank
 */
@SpringBootApplication
public class SpringHotUpdateExample {
    
    public static void main(String[] args) {
        SpringApplication.run(SpringHotUpdateExample.class, args);
    }
    
    /**
     * 配置客户端Bean
     */
    @Bean
    public ConfigClient configClient() {
        // 从环境变量或系统属性读取配置，如果没有则使用默认值
        String serverUrl = System.getProperty("config.server.url", "http://localhost:8080");
        String appCode = System.getProperty("config.app.code", "1003");
        String envCode = System.getProperty("config.env.code", "dev");
        String token = System.getProperty("config.token", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEwLCJzdWIiOiJjb29sbGlhcyIsImlhdCI6MTc1NDYxOTA2NywiZXhwIjoxODQxMDE5MDY3fQ.novfXtGlQJtwxgadMCocFhO6n4CMlmsVzkMkvF6dDi0");
        Long appId = Long.valueOf(System.getProperty("config.app.id", "3"));
        String instanceId = System.getProperty("config.instance.id", "spring-instance-001");
        String instanceIp = System.getProperty("config.instance.ip", "127.0.0.1");
        
        ConfigClient configClient = new ConfigClient.ConfigClientBuilder()
            .serverUrl(serverUrl)
            .appCode(appCode)
            .envCode(envCode)
            .token(token)
            .enableWebSocket(true)
            .appId(appId)
            .instanceId(instanceId)
            .instanceIp(instanceIp)
            .build();
        
        // 启动客户端
        configClient.start();
        
        return configClient;
    }
    
    /**
     * 配置热更新服务示例
     */
    @Component
    public static class ConfigHotUpdateService implements CommandLineRunner {
        
        private static final Logger logger = LoggerFactory.getLogger(ConfigHotUpdateService.class);
        
        @Autowired
        private ConfigClient configClient;
        
        // 使用@ConfigValue注解标记需要热更新的字段
        @ConfigValue("database.url")
        private String databaseUrl;
        
        @ConfigValue("database.username")
        private String databaseUsername;
        
        @ConfigValue("database.pool.maxConnections")
        private Integer maxConnections;
        
        @ConfigValue("logging.level")
        private String logLevel;
        
        @ConfigValue("feature.enableCache")
        private Boolean enableCache;
        
        @ConfigValue("feature.enableMetrics")
        private Boolean enableMetrics;
        
        @ConfigValue("feature.enableHealthCheck")
        private Boolean enableHealthCheck;
        
        @ConfigValue(value = "app.timeout", defaultValue = "30000")
        private Long timeout;
        
        @ConfigValue(value = "app.secret", defaultValue = "default-secret-key")
        private String secret;
        
        @Override
        public void run(String... args) throws Exception {
            // 等待WebSocket连接建立
            logger.info("等待WebSocket连接建立...");
            Thread.sleep(3000);
            
            // 检查WebSocket连接状态
            checkWebSocketStatus();
            
            // 启用热更新
            logger.info("启用热更新功能...");
            configClient.enableHotUpdate(this);
            
            // 打印初始配置
            printCurrentConfigs();
            
            // 执行业务操作
            performBusinessOperation();
            
            // 启动定时任务，定期检查配置变更
            startConfigMonitoring();
            
            // 添加配置变更监听器
            addConfigChangeListener();
        }
        
        /**
         * 打印当前配置值
         */
        public void printCurrentConfigs() {
            System.out.println("=== Spring应用当前配置值 ===");
            System.out.println("数据库URL: " + databaseUrl);
            System.out.println("数据库用户名: " + databaseUsername);
            System.out.println("最大连接数: " + maxConnections);
            System.out.println("日志级别: " + logLevel);
            System.out.println("启用缓存: " + enableCache);
            System.out.println("启用监控: " + enableMetrics);
            System.out.println("启用健康检查: " + enableHealthCheck);
            System.out.println("超时时间: " + timeout);
            System.out.println("密钥: " + secret);
            System.out.println("=============================");
        }
        
        /**
         * 执行业务操作
         */
        public void performBusinessOperation() {
            System.out.println("\n=== 执行业务操作 ===");
            
            // 根据配置值执行业务逻辑
            if (enableCache != null && enableCache) {
                System.out.println("✓ 缓存功能已启用");
            } else {
                System.out.println("✗ 缓存功能已禁用");
            }
            
            if (enableMetrics != null && enableMetrics) {
                System.out.println("✓ 监控功能已启用");
            } else {
                System.out.println("✗ 监控功能已禁用");
            }
            
            if (enableHealthCheck != null && enableHealthCheck) {
                System.out.println("✓ 健康检查已启用");
            } else {
                System.out.println("✗ 健康检查已禁用");
            }
            
            if (maxConnections != null && maxConnections > 0) {
                System.out.println("✓ 数据库连接池配置: " + maxConnections + " 个连接");
            }
            
            System.out.println("=====================\n");
        }
        
        /**
         * 检查WebSocket连接状态
         */
        private void checkWebSocketStatus() {
            try {
                // 获取WebSocket客户端状态
                if (configClient.getWebSocketClient() != null) {
                    logger.info("WebSocket客户端已创建");
                    // 这里可以添加更多状态检查
                } else {
                    logger.warn("WebSocket客户端未创建");
                }
                
                // 检查客户端健康状态
                boolean isHealthy = configClient.isHealthy();
                logger.info("客户端健康状态: {}", isHealthy);
                
                // 获取当前配置
                Map<String, String> currentConfigs = configClient.getAllConfigs();
                logger.info("当前缓存配置数量: {}", currentConfigs.size());
                
            } catch (Exception e) {
                logger.error("检查WebSocket状态失败", e);
            }
        }
        
        /**
         * 添加配置变更监听器
         */
        private void addConfigChangeListener() {
            configClient.addConfigChangeListener(new com.bank.config.client.poller.ConfigChangeListener() {
                @Override
                public void onConfigChange(String key, String oldValue, String newValue) {
                    logger.info("=== 配置变更通知 ===");
                    logger.info("配置项: {}", key);
                    logger.info("旧值: {}", oldValue);
                    logger.info("新值: {}", newValue);
                    logger.info("时间: {}", java.time.LocalDateTime.now());
                    logger.info("==================");
                    
                    // 重新打印当前配置
                    printCurrentConfigs();
                }
                
                @Override
                public void onConfigRefresh(Map<String, String> newConfigs) {
                    logger.info("=== 配置刷新通知 ===");
                    logger.info("收到 {} 个配置项", newConfigs.size());
                    logger.info("时间: {}", java.time.LocalDateTime.now());
                    logger.info("==================");
                    
                    // 重新打印当前配置
                    printCurrentConfigs();
                }
            });
            
            logger.info("配置变更监听器已添加");
        }
        
        /**
         * 启动配置监控
         */
        private void startConfigMonitoring() {
            Thread monitoringThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(30000); // 每30秒检查一次
                        
                        // 打印当前配置（用于演示配置变更）
                        System.out.println("\n[配置监控] 当前时间: " + java.time.LocalDateTime.now());
                        printCurrentConfigs();
                        
                        // 手动刷新配置（用于测试）
                        manualRefreshConfig();
                        
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
            
            monitoringThread.setDaemon(true);
            monitoringThread.setName("config-monitoring");
            monitoringThread.start();
            
            System.out.println("配置监控已启动，每30秒检查一次配置状态");
        }
        
        /**
         * 手动刷新配置（用于测试）
         */
        private void manualRefreshConfig() {
            try {
                logger.info("手动刷新配置...");
                configClient.refreshConfig();
                
                // 重新打印配置
                printCurrentConfigs();
                
                logger.info("手动刷新配置完成");
            } catch (Exception e) {
                logger.error("手动刷新配置失败", e);
            }
        }
        
        // Getter和Setter方法
        public String getDatabaseUrl() { return databaseUrl; }
        public void setDatabaseUrl(String databaseUrl) { this.databaseUrl = databaseUrl; }
        
        public String getDatabaseUsername() { return databaseUsername; }
        public void setDatabaseUsername(String databaseUsername) { this.databaseUsername = databaseUsername; }
        
        public Integer getMaxConnections() { return maxConnections; }
        public void setMaxConnections(Integer maxConnections) { this.maxConnections = maxConnections; }
        
        public String getLogLevel() { return logLevel; }
        public void setLogLevel(String logLevel) { this.logLevel = logLevel; }
        
        public Boolean getEnableCache() { return enableCache; }
        public void setEnableCache(Boolean enableCache) { this.enableCache = enableCache; }
        
        public Boolean getEnableMetrics() { return enableMetrics; }
        public void setEnableMetrics(Boolean enableMetrics) { this.enableMetrics = enableMetrics; }
        
        public Boolean getEnableHealthCheck() { return enableHealthCheck; }
        public void setEnableHealthCheck(Boolean enableHealthCheck) { this.enableHealthCheck = enableHealthCheck; }
        
        public Long getTimeout() { return timeout; }
        public void setTimeout(Long timeout) { this.timeout = timeout; }
        
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
    }
    
    /**
     * 配置变更监听器示例
     */
    @Component
    public static class ConfigChangeListenerExample implements com.bank.config.client.poller.ConfigChangeListener {
        
        @Override
        public void onConfigChange(String key, String oldValue, String newValue) {
            System.out.println("\n[配置变更] 配置项: " + key);
            System.out.println("  旧值: " + oldValue);
            System.out.println("  新值: " + newValue);
            System.out.println("  时间: " + java.time.LocalDateTime.now());
        }
        
        @Override
        public void onConfigRefresh(java.util.Map<String, String> newConfigs) {
            System.out.println("\n[配置刷新] 收到 " + newConfigs.size() + " 个配置项");
            System.out.println("  时间: " + java.time.LocalDateTime.now());
            
            // 可以在这里执行一些配置刷新后的逻辑
            // 例如：重新初始化某些组件、发送通知等
        }
    }
}
