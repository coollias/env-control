package com.bank.config.test.controller;

import com.bank.config.client.ConfigClient;
import com.bank.config.client.poller.ConfigChangeListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置客户端测试控制器
 * 
 * @author bank
 */
@RestController
@RequestMapping("/api/config")
public class ConfigTestController {
    
    @Autowired
    private ConfigClient configClient;
    
    /**
     * 获取单个配置项
     */
    @GetMapping("/{key}")
    public Map<String, Object> getConfig(@PathVariable String key) {
        Map<String, Object> result = new HashMap<>();
        try {
            String value = configClient.getConfig(key);
            result.put("success", true);
            result.put("key", key);
            result.put("value", value);
            result.put("found", value != null);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
    
    /**
     * 获取所有配置项
     */
    @GetMapping("/all")
    public Map<String, Object> getAllConfigs() {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, String> configs = configClient.getAllConfigs();
            result.put("success", true);
            result.put("configs", configs);
            result.put("count", configs.size());
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
    
    /**
     * 手动刷新配置
     */
    @PostMapping("/refresh")
    public Map<String, Object> refreshConfig() {
        Map<String, Object> result = new HashMap<>();
        try {
            configClient.refreshConfig();
            result.put("success", true);
            result.put("message", "配置刷新成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
    
    /**
     * 获取客户端健康状态
     */
    @GetMapping("/health")
    public Map<String, Object> getHealth() {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean isHealthy = configClient.isHealthy();
            Map<String, Object> metrics = configClient.getMetricsData();
            
            result.put("success", true);
            result.put("healthy", isHealthy);
            result.put("metrics", metrics);
            result.put("cacheSize", configClient.getCache().size());
            result.put("lastUpdateTime", configClient.getCache().getLastUpdateTime());
            result.put("pollerRunning", configClient.getPoller().isRunning());
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
    
    /**
     * 启动客户端
     */
    @PostMapping("/start")
    public Map<String, Object> startClient() {
        Map<String, Object> result = new HashMap<>();
        try {
            configClient.initialize();
            result.put("success", true);
            result.put("message", "客户端启动成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
    
    /**
     * 停止客户端
     */
    @PostMapping("/stop")
    public Map<String, Object> stopClient() {
        Map<String, Object> result = new HashMap<>();
        try {
            configClient.stop();
            result.put("success", true);
            result.put("message", "客户端停止成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
    
    /**
     * 获取缓存信息
     */
    @GetMapping("/cache")
    public Map<String, Object> getCacheInfo() {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("cacheSize", configClient.getCache().size());
            result.put("lastUpdateTime", configClient.getCache().getLastUpdateTime());
            result.put("version", configClient.getCache().getVersion());
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
} 