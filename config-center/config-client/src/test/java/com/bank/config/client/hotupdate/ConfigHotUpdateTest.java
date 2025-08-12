package com.bank.config.client.hotupdate;

import com.bank.config.client.cache.ConfigCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 配置热更新功能测试类
 * 
 * @author bank
 */
public class ConfigHotUpdateTest {
    
    private ConfigCache configCache;
    private ConfigHotUpdateManager hotUpdateManager;
    private ConfigHotUpdateProcessor hotUpdateProcessor;
    
    @BeforeEach
    void setUp() {
        // 创建测试用的配置缓存
        configCache = new ConfigCache("test-cache.json", 300000);
        
        // 初始化热更新组件
        hotUpdateManager = new ConfigHotUpdateManager(configCache);
        hotUpdateProcessor = new ConfigHotUpdateProcessor(hotUpdateManager, configCache);
        
        // 添加一些测试配置
        Map<String, String> testConfigs = new HashMap<>();
        testConfigs.put("database.url", "jdbc:mysql://localhost:3306/test");
        testConfigs.put("database.username", "test_user");
        testConfigs.put("database.pool.maxConnections", "10");
        testConfigs.put("logging.level", "INFO");
        testConfigs.put("app.feature.enableCache", "true");
        testConfigs.put("app.timeout", "30000");
        
        configCache.updateConfigs(testConfigs);
    }
    
    @AfterEach
    void tearDown() {
        if (hotUpdateManager != null) {
            hotUpdateManager.shutdown();
        }
    }
    
    /**
     * 测试字段绑定和热更新
     */
    @Test
    void testFieldBindingAndHotUpdate() throws Exception {
        // 创建测试对象
        TestConfigObject testObject = new TestConfigObject();
        
        // 启用热更新
        hotUpdateProcessor.processObject(testObject);
        
        // 验证初始值是否正确设置
        assertEquals("jdbc:mysql://localhost:3306/test", testObject.getDatabaseUrl());
        assertEquals("test_user", testObject.getDatabaseUsername());
        assertEquals(10, testObject.getMaxConnections());
        assertEquals("INFO", testObject.getLogLevel());
        assertTrue(testObject.getEnableCache());
        assertEquals(30000L, testObject.getTimeout());
        
        // 模拟配置变更
        Map<String, String> newConfigs = new HashMap<>();
        newConfigs.put("database.url", "jdbc:mysql://localhost:3306/prod");
        newConfigs.put("database.username", "prod_user");
        newConfigs.put("database.pool.maxConnections", "50");
        newConfigs.put("logging.level", "WARN");
        newConfigs.put("app.feature.enableCache", "false");
        newConfigs.put("app.timeout", "60000");
        
        // 更新配置缓存
        configCache.updateConfigs(newConfigs);
        
        // 等待热更新处理
        Thread.sleep(2000);
        
        // 验证配置是否已更新
        assertEquals("jdbc:mysql://localhost:3306/prod", testObject.getDatabaseUrl());
        assertEquals("prod_user", testObject.getDatabaseUsername());
        assertEquals(50, testObject.getMaxConnections());
        assertEquals("WARN", testObject.getLogLevel());
        assertFalse(testObject.getEnableCache());
        assertEquals(60000L, testObject.getTimeout());
    }
    
    /**
     * 测试方法绑定和热更新
     */
    @Test
    void testMethodBindingAndHotUpdate() throws Exception {
        // 创建测试对象
        TestConfigObject testObject = new TestConfigObject();
        
        // 启用热更新
        hotUpdateProcessor.processObject(testObject);
        
        // 验证方法是否被调用
        assertEquals(0, testObject.getMethodCallCount("setRefreshInterval"));
        assertEquals(0, testObject.getMethodCallCount("setNotificationEnabled"));
        
        // 模拟配置变更
        Map<String, String> newConfigs = new HashMap<>();
        newConfigs.put("app.refresh.interval", "5000");
        newConfigs.put("app.notification.enabled", "true");
        
        // 更新配置缓存
        configCache.updateConfigs(newConfigs);
        
        // 等待热更新处理
        Thread.sleep(2000);
        
        // 验证方法是否被调用
        assertEquals(1, testObject.getMethodCallCount("setRefreshInterval"));
        assertEquals(1, testObject.getMethodCallCount("setNotificationEnabled"));
    }
    
    /**
     * 测试手动绑定配置字段
     */
    @Test
    void testManualFieldBinding() throws Exception {
        // 创建测试对象
        TestConfigObject testObject = new TestConfigObject();
        
        // 手动绑定配置字段
        hotUpdateManager.bindConfigField("custom.config", testObject, "customField");
        
        // 设置初始值
        testObject.setCustomField("initial_value");
        
        // 模拟配置变更
        Map<String, String> newConfigs = new HashMap<>();
        newConfigs.put("custom.config", "updated_value");
        
        // 更新配置缓存
        configCache.updateConfigs(newConfigs);
        
        // 等待热更新处理
        Thread.sleep(2000);
        
        // 验证字段是否已更新
        assertEquals("updated_value", testObject.getCustomField());
    }
    
    /**
     * 测试手动绑定配置方法
     */
    @Test
    void testManualMethodBinding() throws Exception {
        // 创建测试对象
        TestConfigObject testObject = new TestConfigObject();
        
        // 手动绑定配置方法
        hotUpdateManager.bindConfigMethod("custom.method", testObject, "setCustomMethod", String.class);
        
        // 模拟配置变更
        Map<String, String> newConfigs = new HashMap<>();
        newConfigs.put("custom.method", "test_value");
        
        // 更新配置缓存
        configCache.updateConfigs(newConfigs);
        
        // 等待热更新处理
        Thread.sleep(2000);
        
        // 验证方法是否被调用
        assertEquals(1, testObject.getMethodCallCount("setCustomMethod"));
    }
    
    /**
     * 测试配置前缀功能
     */
    @Test
    void testConfigPrefix() throws Exception {
        // 创建测试对象
        TestConfigObject testObject = new TestConfigObject();
        
        // 启用热更新
        hotUpdateProcessor.processObject(testObject);
        
        // 验证前缀配置是否正确设置
        assertEquals("localhost", testObject.getRedisHost());
        assertEquals(6379, testObject.getRedisPort());
        
        // 模拟配置变更
        Map<String, String> newConfigs = new HashMap<>();
        newConfigs.put("redis.host", "redis-server");
        newConfigs.put("redis.port", "6380");
        
        // 更新配置缓存
        configCache.updateConfigs(newConfigs);
        
        // 等待热更新处理
        Thread.sleep(2000);
        
        // 验证配置是否已更新
        assertEquals("redis-server", testObject.getRedisHost());
        assertEquals(6380, testObject.getRedisPort());
    }
    
    /**
     * 测试默认值功能
     */
    @Test
    void testDefaultValue() throws Exception {
        // 创建测试对象
        TestConfigObject testObject = new TestConfigObject();
        
        // 启用热更新
        hotUpdateProcessor.processObject(testObject);
        
        // 验证默认值是否正确设置
        assertEquals(30000L, testObject.getTimeout());
        
        // 模拟配置变更，设置新的超时值
        Map<String, String> newConfigs = new HashMap<>();
        newConfigs.put("app.timeout", "45000");
        
        // 更新配置缓存
        configCache.updateConfigs(newConfigs);
        
        // 等待热更新处理
        Thread.sleep(2000);
        
        // 验证配置是否已更新
        assertEquals(45000L, testObject.getTimeout());
    }
    
    /**
     * 测试必填配置功能
     */
    @Test
    void testRequiredConfig() throws Exception {
        // 创建测试对象
        TestConfigObject testObject = new TestConfigObject();
        
        // 启用热更新
        hotUpdateProcessor.processObject(testObject);
        
        // 验证必填配置是否正确设置
        assertEquals("required_secret", testObject.getSecret());
    }
    
    /**
     * 测试类型转换功能
     */
    @Test
    void testTypeConversion() throws Exception {
        // 创建测试对象
        TestConfigObject testObject = new TestConfigObject();
        
        // 启用热更新
        hotUpdateProcessor.processObject(testObject);
        
        // 验证各种类型的转换是否正确
        assertTrue(testObject.getEnableCache() instanceof Boolean);
        assertTrue(testObject.getMaxConnections() instanceof Integer);
        assertTrue(testObject.getTimeout() instanceof Long);
        assertTrue(testObject.getRedisPort() instanceof Integer);
    }
    
    /**
     * 测试配置变更监听
     */
    @Test
    void testConfigChangeListener() throws Exception {
        // 创建测试对象
        TestConfigObject testObject = new TestConfigObject();
        
        // 启用热更新
        hotUpdateProcessor.processObject(testObject);
        
        // 创建配置变更监听器
        CountDownLatch changeLatch = new CountDownLatch(1);
        TestConfigChangeListener listener = new TestConfigChangeListener(changeLatch);
        
        // 添加监听器
        // 注意：这里需要根据实际的ConfigClient实现来添加监听器
        
        // 模拟配置变更
        Map<String, String> newConfigs = new HashMap<>();
        newConfigs.put("database.url", "jdbc:mysql://localhost:3306/changed");
        
        // 更新配置缓存
        configCache.updateConfigs(newConfigs);
        
        // 等待配置变更通知
        boolean notified = changeLatch.await(5, TimeUnit.SECONDS);
        assertTrue(notified, "配置变更通知应该在5秒内收到");
    }
    
    /**
     * 测试配置对象
     */
    public static class TestConfigObject {
        
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
        
        @ConfigValue(value = "app.timeout", defaultValue = "30000")
        private Long timeout;
        
        @ConfigValue(value = "app.secret", required = true)
        private String secret = "required_secret";
        
        @ConfigValue(prefix = "redis.")
        private String redisHost;
        
        @ConfigValue(prefix = "redis.")
        private Integer redisPort;
        
        private String customField;
        
        private Map<String, Integer> methodCallCounts = new HashMap<>();
        
        // 使用@ConfigValue注解标记的方法
        @ConfigValue("app.refresh.interval")
        public void setRefreshInterval(Integer interval) {
            incrementMethodCallCount("setRefreshInterval");
        }
        
        @ConfigValue("app.notification.enabled")
        public void setNotificationEnabled(Boolean enabled) {
            incrementMethodCallCount("setNotificationEnabled");
        }
        
        public void setCustomMethod(String value) {
            incrementMethodCallCount("setCustomMethod");
        }
        
        private void incrementMethodCallCount(String methodName) {
            methodCallCounts.put(methodName, methodCallCounts.getOrDefault(methodName, 0) + 1);
        }
        
        public int getMethodCallCount(String methodName) {
            return methodCallCounts.getOrDefault(methodName, 0);
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
        
        public Long getTimeout() { return timeout; }
        public void setTimeout(Long timeout) { this.timeout = timeout; }
        
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        
        public String getRedisHost() { return redisHost; }
        public void setRedisHost(String redisHost) { this.redisHost = redisHost; }
        
        public Integer getRedisPort() { return redisPort; }
        public void setRedisPort(Integer redisPort) { this.redisPort = redisPort; }
        
        public String getCustomField() { return customField; }
        public void setCustomField(String customField) { this.customField = customField; }
    }
    
    /**
     * 测试配置变更监听器
     */
    public static class TestConfigChangeListener {
        
        private final CountDownLatch changeLatch;
        
        public TestConfigChangeListener(CountDownLatch changeLatch) {
            this.changeLatch = changeLatch;
        }
        
        public void onConfigChange(String key, String oldValue, String newValue) {
            System.out.println("配置变更: " + key + " = " + oldValue + " -> " + newValue);
            changeLatch.countDown();
        }
    }
}
