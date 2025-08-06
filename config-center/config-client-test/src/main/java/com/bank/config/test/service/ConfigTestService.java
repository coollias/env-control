package com.bank.config.test.service;

import com.bank.config.client.ConfigClient;
import com.bank.config.client.poller.ConfigChangeListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置测试服务
 * 演示如何在业务代码中使用配置客户端
 * 
 * @author bank
 */
@Service
public class ConfigTestService {
    
    @Autowired
    private ConfigClient configClient;
    
    // 模拟业务配置
    private String databaseUrl;
    private String redisHost;
    private Integer redisPort;
    private Boolean debugMode;
    
    @PostConstruct
    public void init() {
        // 添加配置变更监听器
        configClient.addConfigChangeListener(new ConfigChangeListener() {
            @Override
            public void onConfigChange(String key, String oldValue, String newValue) {
                System.out.println("配置变更: " + key + " = " + oldValue + " -> " + newValue);
                updateBusinessConfig();
            }
            
            @Override
            public void onConfigRefresh(Map<String, String> newConfigs) {
                System.out.println("配置刷新，共" + newConfigs.size() + "个配置项");
                updateBusinessConfig();
            }
        });
        
        // 初始化业务配置
        updateBusinessConfig();
    }
    
    /**
     * 更新业务配置
     */
    private void updateBusinessConfig() {
        try {
            databaseUrl = configClient.getConfig("database.url", "jdbc:mysql://localhost:3306/default");
            redisHost = configClient.getConfig("redis.host", "localhost");
            redisPort = Integer.valueOf(configClient.getConfig("redis.port", "6379"));
            debugMode = Boolean.valueOf(configClient.getConfig("app.debug", "false"));
            
            System.out.println("业务配置已更新:");
            System.out.println("  数据库URL: " + databaseUrl);
            System.out.println("  Redis主机: " + redisHost);
            System.out.println("  Redis端口: " + redisPort);
            System.out.println("  调试模式: " + debugMode);
        } catch (Exception e) {
            System.err.println("更新业务配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前业务配置
     */
    public Map<String, Object> getBusinessConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("databaseUrl", databaseUrl);
        config.put("redisHost", redisHost);
        config.put("redisPort", redisPort);
        config.put("debugMode", debugMode);
        return config;
    }
    
    /**
     * 模拟业务方法 - 使用配置
     */
    public String getDatabaseConnectionInfo() {
        return "连接到数据库: " + databaseUrl;
    }
    
    /**
     * 模拟业务方法 - 使用配置
     */
    public String getRedisConnectionInfo() {
        return "连接到Redis: " + redisHost + ":" + redisPort;
    }
    
    /**
     * 模拟业务方法 - 使用配置
     */
    public String getDebugInfo() {
        if (debugMode) {
            return "调试模式已启用，显示详细信息";
        } else {
            return "调试模式已禁用，仅显示基本信息";
        }
    }
    
    /**
     * 模拟业务方法 - 动态获取配置
     */
    public String getDynamicConfig(String key) {
        return configClient.getConfig(key, "未找到配置: " + key);
    }
} 