package com.bank.config.client.example;

import com.bank.config.client.ConfigClient;
import com.bank.config.client.hotupdate.ConfigValue;

/**
 * 配置热更新使用示例
 * 展示如何使用@ConfigValue注解和热更新功能
 * 
 * @author bank
 */
public class HotUpdateExample {
    
    // 使用@ConfigValue注解标记需要热更新的字段
    @ConfigValue("database.url")
    private String databaseUrl;
    
    @ConfigValue("database.username")
    private String databaseUsername;
    
    @ConfigValue("database.pool.maxConnections")
    private Integer maxConnections;
    
    @ConfigValue("logging.level")
    private String logLevel;
    
    @ConfigValue("app.feature.enableCache")
    private Boolean enableCache;
    
    // 使用前缀的配置
    @ConfigValue(prefix = "redis.")
    private String host;
    
    @ConfigValue(prefix = "redis.")
    private Integer port;
    
    // 带默认值的配置
    @ConfigValue(value = "app.timeout", defaultValue = "30000")
    private Long timeout;
    
    // 必填配置
    @ConfigValue(value = "app.secret", required = true)
    private String secret;
    
    private ConfigClient configClient;
    
    public HotUpdateExample(ConfigClient configClient) {
        this.configClient = configClient;
        
        // 启用热更新
        this.configClient.enableHotUpdate(this);
        
        // 也可以手动绑定配置字段
        this.configClient.bindConfigField("custom.config", this, "customField");
    }
    
    /**
     * 使用@ConfigValue注解标记的方法
     */
    @ConfigValue("app.refresh.interval")
    public void setRefreshInterval(Integer interval) {
        System.out.println("设置刷新间隔: " + interval);
        // 这里可以执行具体的业务逻辑
    }
    
    /**
     * 手动绑定配置到方法
     */
    public void initializeManualBindings() {
        // 手动绑定配置字段
        configClient.bindConfigField("database.password", this, "databasePassword");
        
        // 手动绑定配置方法
        configClient.bindConfigMethod("app.notification.enabled", this, "setNotificationEnabled", Boolean.class);
    }
    
    /**
     * 获取配置值的方法
     */
    public void printCurrentConfigs() {
        System.out.println("=== 当前配置值 ===");
        System.out.println("数据库URL: " + databaseUrl);
        System.out.println("数据库用户名: " + databaseUsername);
        System.out.println("最大连接数: " + maxConnections);
        System.out.println("日志级别: " + logLevel);
        System.out.println("启用缓存: " + enableCache);
        System.out.println("Redis主机: " + host);
        System.out.println("Redis端口: " + port);
        System.out.println("超时时间: " + timeout);
        System.out.println("密钥: " + secret);
        System.out.println("==================");
    }
    
    /**
     * 业务方法示例
     */
    public void performBusinessOperation() {
        // 使用配置值执行业务逻辑
        if (enableCache != null && enableCache) {
            System.out.println("使用缓存执行业务操作");
        } else {
            System.out.println("不使用缓存执行业务操作");
        }
        
        if (maxConnections != null && maxConnections > 0) {
            System.out.println("使用连接池，最大连接数: " + maxConnections);
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
    
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    
    public Long getTimeout() { return timeout; }
    public void setTimeout(Long timeout) { this.timeout = timeout; }
    
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    
    // 手动绑定的字段
    private String databasePassword;
    private String customField;
    
    public String getDatabasePassword() { return databasePassword; }
    public void setDatabasePassword(String databasePassword) { this.databasePassword = databasePassword; }
    
    public String getCustomField() { return customField; }
    public void setCustomField(String customField) { this.customField = customField; }
    
    /**
     * 手动绑定的方法
     */
    public void setNotificationEnabled(Boolean enabled) {
        System.out.println("设置通知启用状态: " + enabled);
        // 这里可以执行具体的业务逻辑
    }
    
    /**
     * 主方法示例
     */
    public static void main(String[] args) {
        // 创建配置客户端
        ConfigClient configClient = new ConfigClient.ConfigClientBuilder()
            .serverUrl("http://localhost:8080")
            .appCode("example-app")
            .envCode("dev")
            .token("your-token")
            .enableWebSocket(true)
            .appId(1L)
            .instanceId("instance-001")
            .instanceIp("127.0.0.1")
            .build();
        
        // 启动客户端
        configClient.start();
        
        // 创建热更新示例对象
        HotUpdateExample example = new HotUpdateExample(configClient);
        
        // 打印初始配置
        example.printCurrentConfigs();
        
        // 执行业务操作
        example.performBusinessOperation();
        
        // 模拟配置变更（在实际使用中，配置变更会通过WebSocket自动推送）
        System.out.println("\n等待配置变更...");
        System.out.println("当配置中心发布配置变更时，相关字段会自动更新");
        
        // 保持程序运行，观察配置变更
        try {
            Thread.sleep(60000); // 运行1分钟
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 再次打印配置，查看是否有变更
        example.printCurrentConfigs();
        
        // 停止客户端
        configClient.stop();
    }
}
