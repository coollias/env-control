package com.bank.config.controller;

import com.bank.config.common.ApiResponse;
import com.bank.config.common.CacheManagerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存管理控制器
 * 
 * @author bank
 */
@RestController
@RequestMapping("/api/cache")
public class CacheController {

    @Autowired
    private CacheManagerUtil cacheManager;

    @Autowired
    private org.springframework.cache.CacheManager springCacheManager;

    /**
     * 获取缓存状态
     */
    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> getCacheStatus() {
        Map<String, Object> status = new HashMap<>();
        
        for (String cacheName : springCacheManager.getCacheNames()) {
            Cache cache = springCacheManager.getCache(cacheName);
            if (cache != null) {
                Map<String, Object> cacheInfo = new HashMap<>();
                cacheInfo.put("name", cache.getName());
                cacheInfo.put("nativeCache", cache.getNativeCache().getClass().getSimpleName());
                status.put(cacheName, cacheInfo);
            }
        }
        
        return ApiResponse.success("获取缓存状态成功", status);
    }

    /**
     * 清除指定缓存
     */
    @DeleteMapping("/{cacheName}")
    public ApiResponse<String> clearCache(@PathVariable String cacheName) {
        cacheManager.clearCache(cacheName);
        return ApiResponse.success("清除缓存成功: " + cacheName);
    }

    /**
     * 清除所有缓存
     */
    @DeleteMapping("/all")
    public ApiResponse<String> clearAllCaches() {
        cacheManager.clearAllCaches();
        return ApiResponse.success("清除所有缓存成功");
    }

    /**
     * 清除配置项相关缓存
     */
    @DeleteMapping("/config-items")
    public ApiResponse<String> clearConfigItemCaches() {
        cacheManager.clearConfigItemCaches();
        return ApiResponse.success("清除配置项缓存成功");
    }

    /**
     * 清除应用相关缓存
     */
    @DeleteMapping("/applications")
    public ApiResponse<String> clearApplicationCaches() {
        cacheManager.clearApplicationCaches();
        return ApiResponse.success("清除应用缓存成功");
    }

    /**
     * 清除环境相关缓存
     */
    @DeleteMapping("/environments")
    public ApiResponse<String> clearEnvironmentCaches() {
        cacheManager.clearEnvironmentCaches();
        return ApiResponse.success("清除环境缓存成功");
    }

    /**
     * 清除版本相关缓存
     */
    @DeleteMapping("/versions")
    public ApiResponse<String> clearVersionCaches() {
        cacheManager.clearVersionCaches();
        return ApiResponse.success("清除版本缓存成功");
    }

    /**
     * 根据应用ID和环境ID清除相关缓存
     */
    @DeleteMapping("/app/{appId}/env/{envId}")
    public ApiResponse<String> clearCachesByAppAndEnv(@PathVariable Long appId, @PathVariable Long envId) {
        cacheManager.clearCachesByAppAndEnv(appId, envId);
        return ApiResponse.success("清除应用环境相关缓存成功");
    }
} 