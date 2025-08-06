package com.bank.config.client.poller;

import java.util.Map;

/**
 * 配置变更监听器接口
 * 用于监听配置变更事件
 * 
 * @author bank
 */
public interface ConfigChangeListener {
    
    /**
     * 配置项变更事件
     * 
     * @param key 配置键
     * @param oldValue 旧值
     * @param newValue 新值
     */
    void onConfigChange(String key, String oldValue, String newValue);
    
    /**
     * 配置刷新事件
     * 
     * @param newConfigs 新的配置集合
     */
    void onConfigRefresh(Map<String, String> newConfigs);
} 