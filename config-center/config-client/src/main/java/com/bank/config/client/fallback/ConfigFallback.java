package com.bank.config.client.fallback;

/**
 * 配置降级接口
 * 当配置获取失败时提供默认值
 * 
 * @author bank
 */
public interface ConfigFallback {
    
    /**
     * 获取默认配置值
     * 
     * @param key 配置键
     * @return 默认值，如果没有默认值则返回null
     */
    String getDefaultValue(String key);
    
    /**
     * 检查是否有默认值
     * 
     * @param key 配置键
     * @return 是否有默认值
     */
    boolean hasDefaultValue(String key);
    
    /**
     * 获取所有默认配置
     * 
     * @return 默认配置映射
     */
    java.util.Map<String, String> getAllDefaultValues();
} 