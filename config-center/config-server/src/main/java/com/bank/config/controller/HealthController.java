package com.bank.config.controller;

import com.bank.config.common.ApiResponse;
import com.bank.config.service.ApplicationService;
import com.bank.config.service.ConfigItemService;
import com.bank.config.service.EnvironmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查Controller
 * 
 * @author bank
 */
@RestController
@RequestMapping("/api/health")
//@CrossOrigin(origins = "*")
public class HealthController {

    @Autowired
    private ApplicationService applicationService;
    
    @Autowired
    private EnvironmentService environmentService;
    
    @Autowired
    private ConfigItemService configItemService;

    /**
     * 健康检查
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "config-server");
        health.put("version", "1.0.0");
        return ApiResponse.success(health);
    }

    /**
     * 获取系统统计数据
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 获取应用总数
            long applicationsCount = applicationService.countAllEnabled();
            stats.put("applications", applicationsCount);
            
            // 获取环境总数
            long environmentsCount = environmentService.countAllEnabled();
            stats.put("environments", environmentsCount);
            
            // 获取配置总数
            long configsCount = configItemService.countAll();
            stats.put("configs", configsCount);
            
            // 获取活跃配置数（状态为启用的配置）
            long activeConfigsCount = configItemService.countByStatus(1);
            stats.put("active", activeConfigsCount);
            
            return ApiResponse.success(stats);
        } catch (Exception e) {
            return ApiResponse.error("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 简单测试接口
     */
    @GetMapping("/test")
    public ApiResponse<String> test() {
        return ApiResponse.success("配置中心服务运行正常");
    }
} 