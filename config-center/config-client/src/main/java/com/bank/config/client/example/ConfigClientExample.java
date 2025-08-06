package com.bank.config.client.example;

import com.bank.config.client.ConfigClient;
import com.bank.config.client.poller.ConfigChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 配置客户端使用示例
 * 
 * @author bank
 */
public class ConfigClientExample {
    private static final Logger logger = LoggerFactory.getLogger(ConfigClientExample.class);

    public static void main(String[] args) {
        System.out.println("=== 配置客户端演示开始 ===");
        
        // 创建配置客户端
        ConfigClient client = new ConfigClient.ConfigClientBuilder()
            .serverUrl("http://localhost:8080")
            .appCode("1003")  // 改为你的应用代码
            .envCode("dev")      // 改为你的环境代码
            .token("Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEwLCJzdWIiOiJjb29sbGlhcyIsImlhdCI6MTc1NDQzOTg1NCwiZXhwIjoxODQwODM5ODU0fQ.joAFmm_rW3Nm_w6848RHIuotBAofqAvjsooC0jzr17A")  // 改为你的有效Token
            .pollInterval(30000) // 30秒拉取间隔
            .cacheFile("/tmp/test-config-cache.yaml")
            .enablePolling(true)
            .enableCache(true)
            .build();

        System.out.println("配置客户端创建成功");
        System.out.println("服务器地址: " + client.getServerUrl());
        System.out.println("应用代码: " + client.getAppCode());
        System.out.println("环境代码: " + client.getEnvCode());

        // 添加配置变更监听器
        client.addConfigChangeListener(new ConfigChangeListener() {
            @Override
            public void onConfigChange(String key, String oldValue, String newValue) {
                System.out.println("配置变更: " + key + " = " + oldValue + " -> " + newValue);
            }

            @Override
            public void onConfigRefresh(Map<String, String> newConfigs) {
                System.out.println("配置刷新，共" + newConfigs.size() + "个配置项");
                for (Map.Entry<String, String> entry : newConfigs.entrySet()) {
                    System.out.println("  " + entry.getKey() + " = " + entry.getValue());
                }
            }
        });

        try {
            System.out.println("\n开始初始化配置客户端...");
            
            // 初始化客户端
            client.initialize();
            System.out.println("✅ 配置客户端初始化成功");

            // 获取单个配置
            System.out.println("\n尝试获取配置项...");
            
            String dbUrl = client.getConfig("database.url");
            System.out.println("数据库URL: " + (dbUrl != null ? dbUrl : "未找到"));

            String apiKey = client.getConfig("api.key");
            System.out.println("API密钥: " + (apiKey != null ? apiKey : "未找到"));

            // 获取配置并指定默认值
            String timeout = client.getConfig("timeout", "5000");
            System.out.println("超时时间: " + timeout);

            // 获取所有配置
            Map<String, String> allConfigs = client.getAllConfigs();
            System.out.println("所有配置项数量: " + allConfigs.size());
            
            if (!allConfigs.isEmpty()) {
                System.out.println("配置项列表:");
                for (Map.Entry<String, String> entry : allConfigs.entrySet()) {
                    System.out.println("  " + entry.getKey() + " = " + entry.getValue());
                }
            } else {
                System.out.println("⚠️  没有获取到任何配置项");
            }

            // 检查客户端健康状态
            boolean isHealthy = client.isHealthy();
            System.out.println("客户端健康状态: " + (isHealthy ? "✅ 健康" : "❌ 不健康"));

            // 获取监控指标
            Map<String, Object> metrics = client.getMetricsData();
            System.out.println("监控指标:");
            for (Map.Entry<String, Object> entry : metrics.entrySet()) {
                System.out.println("  " + entry.getKey() + " = " + entry.getValue());
            }

            // 检查缓存状态
            System.out.println("\n缓存状态:");
            System.out.println("缓存大小: " + client.getCache().size());
            System.out.println("缓存最后更新时间: " + client.getCache().getLastUpdateTime());
            System.out.println("缓存版本: " + client.getCache().getVersion());

            // 检查拉取器状态
            System.out.println("拉取器状态:");
            System.out.println("是否运行: " + client.getPoller().isRunning());
            System.out.println("拉取间隔: " + client.getPoller().getPollInterval() + "ms");

            // 模拟应用运行一段时间
            System.out.println("\n应用运行中，按Ctrl+C退出...");
            System.out.println("将等待30秒观察配置拉取情况...");
            
            for (int i = 0; i < 30; i++) {
                Thread.sleep(1000);
                System.out.print(".");
                if ((i + 1) % 10 == 0) {
                    System.out.println(" (" + (i + 1) + "秒)");
                }
            }
            System.out.println();

        } catch (Exception e) {
            System.err.println("❌ 配置客户端使用示例失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 停止客户端
            System.out.println("\n停止配置客户端...");
            client.stop();
            System.out.println("✅ 配置客户端已停止");
            System.out.println("=== 配置客户端演示结束 ===");
        }
    }
} 