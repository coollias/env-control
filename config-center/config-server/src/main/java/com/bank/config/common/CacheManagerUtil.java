package com.bank.config.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * 缓存管理工具类
 * 
 * @author bank
 */
@Component
public class CacheManagerUtil {

    @Autowired
    private org.springframework.cache.CacheManager cacheManager;

    /**
     * 清除指定缓存的所有条目
     */
    public void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    /**
     * 清除所有缓存
     */
    public void clearAllCaches() {
        cacheManager.getCacheNames().forEach(this::clearCache);
    }

    /**
     * 清除配置项相关缓存
     */
    public void clearConfigItemCaches() {
        clearCache("configItems");
        clearCache("mergedConfigs");
    }

    /**
     * 清除应用相关缓存
     */
    public void clearApplicationCaches() {
        clearCache("applications");
    }

    /**
     * 清除环境相关缓存
     */
    public void clearEnvironmentCaches() {
        clearCache("environments");
    }

    /**
     * 清除版本相关缓存
     */
    public void clearVersionCaches() {
        clearCache("versions");
    }

    /**
     * 根据应用ID和环境ID清除相关缓存
     */
    public void clearCachesByAppAndEnv(Long appId, Long envId) {
        // 清除配置项缓存
        clearCache("configItems");
        clearCache("mergedConfigs");
        // 清除版本缓存
        clearCache("versions");
    }

    /**
     * 根据应用ID清除相关缓存
     */
    public void clearCachesByApp(Long appId) {
        clearCache("applications");
        clearCache("configItems");
        clearCache("mergedConfigs");
        clearCache("versions");
    }

    /**
     * 根据环境ID清除相关缓存
     */
    public void clearCachesByEnv(Long envId) {
        clearCache("environments");
        clearCache("configItems");
        clearCache("mergedConfigs");
        clearCache("versions");
    }
} 