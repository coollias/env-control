package com.bank.config.client.fallback;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认配置降级实现
 * 提供基本的默认值支持
 * 
 * @author bank
 */
public class DefaultConfigFallback implements ConfigFallback {
    
    private final Map<String, String> defaultValues;
    
    public DefaultConfigFallback() {
        this.defaultValues = new HashMap<>();
        initializeDefaultValues();
    }
    
    /**
     * 初始化默认值
     */
    private void initializeDefaultValues() {
        // 数据库相关默认值
        defaultValues.put("database.url", "jdbc:mysql://localhost:3306/test");
        defaultValues.put("database.username", "root");
        defaultValues.put("database.password", "");
        defaultValues.put("database.driver", "com.mysql.cj.jdbc.Driver");
        
        // Redis相关默认值
        defaultValues.put("redis.host", "localhost");
        defaultValues.put("redis.port", "6379");
        defaultValues.put("redis.password", "");
        defaultValues.put("redis.database", "0");
        
        // 应用相关默认值
        defaultValues.put("app.name", "default-app");
        defaultValues.put("app.version", "1.0.0");
        defaultValues.put("app.port", "8080");
        
        // 日志相关默认值
        defaultValues.put("logging.level", "INFO");
        defaultValues.put("logging.pattern", "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n");
        
        // 安全相关默认值
        defaultValues.put("security.enabled", "true");
        defaultValues.put("security.jwt.secret", "default-secret");
        defaultValues.put("security.jwt.expiration", "86400");
        
        // 缓存相关默认值
        defaultValues.put("cache.enabled", "true");
        defaultValues.put("cache.ttl", "300");
        defaultValues.put("cache.maxSize", "1000");
        
        // 线程池相关默认值
        defaultValues.put("thread.pool.coreSize", "10");
        defaultValues.put("thread.pool.maxSize", "50");
        defaultValues.put("thread.pool.queueSize", "100");
        defaultValues.put("thread.pool.keepAlive", "60");
        
        // 超时相关默认值
        defaultValues.put("timeout.connect", "5000");
        defaultValues.put("timeout.read", "10000");
        defaultValues.put("timeout.write", "10000");
        
        // 重试相关默认值
        defaultValues.put("retry.maxAttempts", "3");
        defaultValues.put("retry.delay", "1000");
        defaultValues.put("retry.multiplier", "2");
        
        // 监控相关默认值
        defaultValues.put("monitoring.enabled", "true");
        defaultValues.put("monitoring.metrics.enabled", "true");
        defaultValues.put("monitoring.health.enabled", "true");
        
        // 文件上传相关默认值
        defaultValues.put("upload.maxFileSize", "10MB");
        defaultValues.put("upload.allowedTypes", "jpg,jpeg,png,gif,pdf,doc,docx");
        defaultValues.put("upload.path", "/tmp/uploads");
        
        // 邮件相关默认值
        defaultValues.put("mail.host", "localhost");
        defaultValues.put("mail.port", "25");
        defaultValues.put("mail.username", "");
        defaultValues.put("mail.password", "");
        defaultValues.put("mail.from", "noreply@example.com");
        
        // 短信相关默认值
        defaultValues.put("sms.provider", "default");
        defaultValues.put("sms.apiKey", "");
        defaultValues.put("sms.secretKey", "");
        defaultValues.put("sms.signName", "");
        
        // 支付相关默认值
        defaultValues.put("payment.provider", "default");
        defaultValues.put("payment.apiKey", "");
        defaultValues.put("payment.secretKey", "");
        defaultValues.put("payment.notifyUrl", "");
        
        // 第三方服务相关默认值
        defaultValues.put("thirdparty.api.baseUrl", "https://api.example.com");
        defaultValues.put("thirdparty.api.timeout", "5000");
        defaultValues.put("thirdparty.api.retry", "3");
    }
    
    @Override
    public String getDefaultValue(String key) {
        return defaultValues.get(key);
    }
    
    @Override
    public boolean hasDefaultValue(String key) {
        return defaultValues.containsKey(key);
    }
    
    @Override
    public Map<String, String> getAllDefaultValues() {
        return new HashMap<>(defaultValues);
    }
    
    /**
     * 添加自定义默认值
     * 
     * @param key 配置键
     * @param value 默认值
     */
    public void addDefaultValue(String key, String value) {
        defaultValues.put(key, value);
    }
    
    /**
     * 批量添加默认值
     * 
     * @param values 默认值映射
     */
    public void addDefaultValues(Map<String, String> values) {
        defaultValues.putAll(values);
    }
    
    /**
     * 移除默认值
     * 
     * @param key 配置键
     */
    public void removeDefaultValue(String key) {
        defaultValues.remove(key);
    }
    
    /**
     * 清空所有默认值
     */
    public void clearDefaultValues() {
        defaultValues.clear();
    }
} 