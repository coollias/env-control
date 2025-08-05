package com.bank.config.controller;

import com.bank.config.common.ApiResponse;
import com.bank.config.entity.Application;
import com.bank.config.service.ApplicationService;
import com.bank.config.service.ConfigItemService;
import com.bank.config.service.EnvironmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 缓存测试控制器
 * 
 * @author bank
 */
@RestController
@RequestMapping("/api/cache-test")
public class CacheTestController {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private ConfigItemService configItemService;

    /**
     * 测试应用缓存
     */
    @GetMapping("/applications/{id}")
    public ApiResponse<Map<String, Object>> testApplicationCache(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        System.out.println("=== 开始测试应用缓存 ===");
        
        // 第一次查询
        long start1 = System.currentTimeMillis();
        Optional<Application> app1 = applicationService.findById(id);
        long time1 = System.currentTimeMillis() - start1;
        System.out.println("第一次查询时间: " + time1 + "ms, 结果: " + app1.isPresent());
        
        // 第二次查询（应该从缓存获取）
        long start2 = System.currentTimeMillis();
        Optional<Application> app2 = applicationService.findById(id);
        long time2 = System.currentTimeMillis() - start2;
        System.out.println("第二次查询时间: " + time2 + "ms, 结果: " + app2.isPresent());
        
        // 检查Redis中的键
        Set<String> keys = redisTemplate.keys("applications*");
        System.out.println("Redis中的applications键: " + keys);
        
        result.put("firstQueryTime", time1);
        result.put("secondQueryTime", time2);
        result.put("cacheHit", time2 < time1);
        result.put("app1Present", app1.isPresent());
        result.put("app2Present", app2.isPresent());
        result.put("redisKeys", keys);
        result.put("redisKeyCount", keys != null ? keys.size() : 0);
        
        System.out.println("=== 测试完成 ===");
        
        return ApiResponse.success("缓存测试完成", result);
    }

    /**
     * 测试环境缓存
     */
    @GetMapping("/environments/{id}")
    public ApiResponse<Map<String, Object>> testEnvironmentCache(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        // 第一次查询
        long start1 = System.currentTimeMillis();
        environmentService.findById(id);
        long time1 = System.currentTimeMillis() - start1;
        
        // 第二次查询（应该从缓存获取）
        long start2 = System.currentTimeMillis();
        environmentService.findById(id);
        long time2 = System.currentTimeMillis() - start2;
        
        result.put("firstQueryTime", time1);
        result.put("secondQueryTime", time2);
        result.put("cacheHit", time2 < time1);
        
        return ApiResponse.success("缓存测试完成", result);
    }

    /**
     * 测试配置项缓存
     */
    @GetMapping("/config-items/{id}")
    public ApiResponse<Map<String, Object>> testConfigItemCache(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        // 第一次查询
        long start1 = System.currentTimeMillis();
        configItemService.findById(id);
        long time1 = System.currentTimeMillis() - start1;
        
        // 第二次查询（应该从缓存获取）
        long start2 = System.currentTimeMillis();
        configItemService.findById(id);
        long time2 = System.currentTimeMillis() - start2;
        
        result.put("firstQueryTime", time1);
        result.put("secondQueryTime", time2);
        result.put("cacheHit", time2 < time1);
        
        return ApiResponse.success("缓存测试完成", result);
    }

    /**
     * 获取缓存统计信息
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                Map<String, Object> cacheInfo = new HashMap<>();
                cacheInfo.put("name", cache.getName());
                cacheInfo.put("nativeCache", cache.getNativeCache().getClass().getSimpleName());
                stats.put(cacheName, cacheInfo);
            }
        }
        
        return ApiResponse.success("获取缓存统计成功", stats);
    }

    /**
     * 检查Redis中的所有键
     */
    @GetMapping("/redis-keys")
    public ApiResponse<Map<String, Object>> getRedisKeys() {
        Map<String, Object> result = new HashMap<>();
        
        Set<String> keys = redisTemplate.keys("*");
        result.put("totalKeys", keys != null ? keys.size() : 0);
        result.put("keys", keys);
        
        return ApiResponse.success("获取Redis键成功", result);
    }

    /**
     * 检查特定缓存的所有键
     */
    @GetMapping("/cache-keys/{cacheName}")
    public ApiResponse<Map<String, Object>> getCacheKeys(@PathVariable String cacheName) {
        Map<String, Object> result = new HashMap<>();
        
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            // 获取Redis中该缓存的所有键
            Set<String> keys = redisTemplate.keys(cacheName + "*");
            result.put("cacheName", cacheName);
            result.put("totalKeys", keys != null ? keys.size() : 0);
            result.put("keys", keys);
        } else {
            result.put("error", "缓存不存在: " + cacheName);
        }
        
        return ApiResponse.success("获取缓存键成功", result);
    }

    /**
     * 清除所有缓存
     */
    @DeleteMapping("/clear-all")
    public ApiResponse<String> clearAllCaches() {
        cacheManager.getCacheNames().forEach(name -> {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        });
        return ApiResponse.success("清除所有缓存成功");
    }

    /**
     * 测试Redis连接
     */
    @GetMapping("/redis-test")
    public ApiResponse<Map<String, Object>> testRedisConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 测试写入
            redisTemplate.opsForValue().set("test:key", "test:value");
            String value = (String) redisTemplate.opsForValue().get("test:key");
            
            result.put("writeSuccess", true);
            result.put("readSuccess", "test:value".equals(value));
            result.put("testValue", value);
            
            // 清理测试数据
            redisTemplate.delete("test:key");
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        
        return ApiResponse.success("Redis连接测试完成", result);
    }

    /**
     * 测试简单缓存
     */
    @GetMapping("/simple-test/{id}")
    public ApiResponse<Map<String, Object>> testSimpleCache(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        // 第一次调用
        long start1 = System.currentTimeMillis();
        String value1 = configItemService.testCache(id);
        long time1 = System.currentTimeMillis() - start1;
        
        // 第二次调用（应该从缓存获取）
        long start2 = System.currentTimeMillis();
        String value2 = configItemService.testCache(id);
        long time2 = System.currentTimeMillis() - start2;
        
        result.put("firstCallTime", time1);
        result.put("secondCallTime", time2);
        result.put("firstValue", value1);
        result.put("secondValue", value2);
        result.put("cacheHit", time2 < time1);
        
        return ApiResponse.success("简单缓存测试完成", result);
    }

    /**
     * 测试应用简单缓存
     */
    @GetMapping("/app-simple-test/{id}")
    public ApiResponse<Map<String, Object>> testAppSimpleCache(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        System.out.println("=== 开始测试应用简单缓存 ===");
        
        // 第一次调用
        long start1 = System.currentTimeMillis();
        String value1 = applicationService.testCache(id);
        long time1 = System.currentTimeMillis() - start1;
        System.out.println("第一次调用时间: " + time1 + "ms, 值: " + value1);
        
        // 第二次调用（应该从缓存获取）
        long start2 = System.currentTimeMillis();
        String value2 = applicationService.testCache(id);
        long time2 = System.currentTimeMillis() - start2;
        System.out.println("第二次调用时间: " + time2 + "ms, 值: " + value2);
        
        // 检查Redis中的键
        Set<String> keys = redisTemplate.keys("test*");
        System.out.println("Redis中的test键: " + keys);
        
        result.put("firstCallTime", time1);
        result.put("secondCallTime", time2);
        result.put("firstValue", value1);
        result.put("secondValue", value2);
        result.put("cacheHit", time2 < time1);
        result.put("redisKeys", keys);
        result.put("redisKeyCount", keys != null ? keys.size() : 0);
        
        System.out.println("=== 应用简单缓存测试完成 ===");
        
        return ApiResponse.success("应用简单缓存测试完成", result);
    }
} 