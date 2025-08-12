package com.bank.config.client.example;

import com.bank.config.client.ConfigClient;
import com.bank.config.client.poller.ConfigChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * WebSocket配置客户端示例
 * 演示如何使用WebSocket接收配置更新
 * 
 * @author bank
 */
public class WebSocketConfigClientExample {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfigClientExample.class);
    
    public static void main(String[] args) {
        // 创建配置客户端，启用WebSocket
        ConfigClient client = new ConfigClient.ConfigClientBuilder()
                .serverUrl("http://localhost:8080")
                .appCode("1003")
                .envCode("dev")
                .token("Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEwLCJzdWIiOiJjb29sbGlhcyIsImlhdCI6MTc1NDYxOTA2NywiZXhwIjoxODQxMDE5MDY3fQ.novfXtGlQJtwxgadMCocFhO6n4CMlmsVzkMkvF6dDi0")
                .enableWebSocket(true)
                .appId(3L)  // 应用ID - 修改为实际的应用ID
                .instanceId("instance-001")  // 实例ID
                .instanceIp("192.168.1.100")  // 实例IP
                .clientVersion("1.0.0")  // 客户端版本
                .enablePolling(false)  // 禁用轮询，只使用WebSocket
                .enableCache(true)
                .cacheFile("config-cache.yaml")
                .build();
        
        // 添加配置变更监听器
        client.addConfigChangeListener(new ConfigChangeListener() {
            @Override
            public void onConfigChange(String key, String oldValue, String newValue) {
                logger.info("配置变更: {} = {} -> {}", key, oldValue, newValue);
            }
            
            @Override
            public void onConfigRefresh(Map<String, String> newConfigs) {
                logger.info("配置刷新，共{}个配置项", newConfigs.size());
                newConfigs.forEach((key, value) -> {
                    logger.info("  {} = {}", key, value);
                });
            }
        });
        
        try {
            // 启动客户端
            logger.info("正在启动WebSocket配置客户端...");
            client.start();
            logger.info("WebSocket配置客户端已启动");
            
            // 等待一下让WebSocket连接建立
            Thread.sleep(2000);
            
            // 检查客户端状态
            logger.info("客户端健康状态: {}", client.isHealthy());
            
            // 获取初始配置
            Map<String, String> configs = client.getAllConfigs();
            logger.info("初始配置: {}", configs);
            
            // 保持运行，等待WebSocket消息
            CountDownLatch latch = new CountDownLatch(1);
            
            // 添加关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("正在关闭客户端...");
                client.stop();
                latch.countDown();
            }));
            
            logger.info("客户端正在运行，等待WebSocket消息...");
            logger.info("请在另一个终端中发布配置，观察这里的日志输出");
            
            // 等待关闭信号
            latch.await();
            
        } catch (Exception e) {
            logger.error("客户端运行失败", e);
        } finally {
            client.stop();
        }
    }
}
