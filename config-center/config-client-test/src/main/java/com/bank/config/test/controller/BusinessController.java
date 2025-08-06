package com.bank.config.test.controller;

import com.bank.config.test.service.ConfigTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 业务测试控制器
 * 演示业务代码如何使用配置客户端
 * 
 * @author bank
 */
@RestController
@RequestMapping("/api/business")
public class BusinessController {
    
    @Autowired
    private ConfigTestService configTestService;
    
    /**
     * 获取业务配置
     */
    @GetMapping("/config")
    public Map<String, Object> getBusinessConfig() {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> config = configTestService.getBusinessConfig();
            result.put("success", true);
            result.put("config", config);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
    
    /**
     * 获取数据库连接信息
     */
    @GetMapping("/database")
    public Map<String, Object> getDatabaseInfo() {
        Map<String, Object> result = new HashMap<>();
        try {
            String info = configTestService.getDatabaseConnectionInfo();
            result.put("success", true);
            result.put("info", info);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
    
    /**
     * 获取Redis连接信息
     */
    @GetMapping("/redis")
    public Map<String, Object> getRedisInfo() {
        Map<String, Object> result = new HashMap<>();
        try {
            String info = configTestService.getRedisConnectionInfo();
            result.put("success", true);
            result.put("info", info);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
    
    /**
     * 获取调试信息
     */
    @GetMapping("/debug")
    public Map<String, Object> getDebugInfo() {
        Map<String, Object> result = new HashMap<>();
        try {
            String info = configTestService.getDebugInfo();
            result.put("success", true);
            result.put("info", info);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
    
    /**
     * 动态获取配置
     */
    @GetMapping("/dynamic/{key}")
    public Map<String, Object> getDynamicConfig(@PathVariable String key) {
        Map<String, Object> result = new HashMap<>();
        try {
            String value = configTestService.getDynamicConfig(key);
            result.put("success", true);
            result.put("key", key);
            result.put("value", value);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
} 