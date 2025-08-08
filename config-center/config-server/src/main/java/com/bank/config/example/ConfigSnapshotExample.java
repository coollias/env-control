package com.bank.config.example;

import com.bank.config.service.ConfigSnapshotService;
import com.bank.config.service.ConfigPushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置快照使用示例
 * 
 * @author bank
 */
@Component
public class ConfigSnapshotExample {

    @Autowired
    private ConfigSnapshotService configSnapshotService;

    @Autowired
    private ConfigPushService configPushService;

    /**
     * 演示暂存功能
     */
    public void demonstrateStaging() {
        try {
            // 1. 准备配置数据
            Map<String, Object> configData = new HashMap<>();
            configData.put("server.port", "8080");
            configData.put("database.url", "jdbc:mysql://localhost:3306/test");
            configData.put("redis.host", "localhost");
            configData.put("redis.port", "6379");
            configData.put("logging.level", "INFO");

            // 2. 创建暂存快照
            com.bank.config.entity.ConfigSnapshot snapshot = configSnapshotService.createSnapshot(
                1L, // appId
                1L, // envId
                "开发环境配置暂存",
                "开发环境的配置暂存版本",
                configData,
                "admin"
            );

            System.out.println("暂存快照创建成功: " + snapshot.getVersionNumber());
            System.out.println("快照ID: " + snapshot.getId());
            System.out.println("配置项数量: " + snapshot.getConfigCount());

        } catch (Exception e) {
            System.err.println("暂存失败: " + e.getMessage());
        }
    }

    /**
     * 演示发布功能
     */
    public void demonstratePublishing() {
        try {
            // 1. 获取最新的暂存快照
            java.util.Optional<com.bank.config.entity.ConfigSnapshot> stagedSnapshot = configSnapshotService.getLatestStagedSnapshot(1L, 1L);
            
            if (stagedSnapshot.isPresent()) {
                // 2. 发布快照
                com.bank.config.entity.ConfigSnapshot publishedSnapshot = configSnapshotService.publishSnapshot(
                    stagedSnapshot.get().getId(), 
                    "admin"
                );

                System.out.println("快照发布成功: " + publishedSnapshot.getVersionNumber());

                // 3. 推送配置到客户端
                Map<String, Object> configData = configSnapshotService.getSnapshotConfigData(publishedSnapshot.getId());
                configPushService.pushConfigToApp(1L, 1L, configData);

                System.out.println("配置推送成功");

            } else {
                System.out.println("没有找到暂存快照");
            }

        } catch (Exception e) {
            System.err.println("发布失败: " + e.getMessage());
        }
    }

    /**
     * 演示版本比较
     */
    public void demonstrateComparison() {
        try {
            // 获取两个快照进行比较
            java.util.List<com.bank.config.entity.ConfigSnapshot> snapshots = configSnapshotService.findByAppIdAndEnvId(1L, 1L);
            
            if (snapshots.size() >= 2) {
                com.bank.config.entity.ConfigSnapshot snapshot1 = snapshots.get(0);
                com.bank.config.entity.ConfigSnapshot snapshot2 = snapshots.get(1);

                Map<String, Object> differences = configSnapshotService.compareSnapshots(
                    snapshot1.getId(), 
                    snapshot2.getId()
                );

                System.out.println("版本比较结果:");
                System.out.println("快照1: " + snapshot1.getVersionNumber());
                System.out.println("快照2: " + snapshot2.getVersionNumber());
                System.out.println("差异数量: " + ((Map<?, ?>) differences.get("differences")).size());

            } else {
                System.out.println("快照数量不足，无法比较");
            }

        } catch (Exception e) {
            System.err.println("比较失败: " + e.getMessage());
        }
    }

    /**
     * 演示回滚功能
     */
    public void demonstrateRollback() {
        try {
            // 获取历史快照
            java.util.List<com.bank.config.entity.ConfigSnapshot> snapshots = configSnapshotService.findByAppIdAndEnvId(1L, 1L);
            
            if (!snapshots.isEmpty()) {
                com.bank.config.entity.ConfigSnapshot targetSnapshot = snapshots.get(snapshots.size() - 1); // 选择最旧的快照

                // 回滚到指定快照
                com.bank.config.entity.ConfigSnapshot rollbackSnapshot = configSnapshotService.rollbackToSnapshot(
                    1L, 1L, targetSnapshot.getId(), "admin"
                );

                System.out.println("回滚成功: " + rollbackSnapshot.getVersionNumber());

            } else {
                System.out.println("没有可回滚的快照");
            }

        } catch (Exception e) {
            System.err.println("回滚失败: " + e.getMessage());
        }
    }

    /**
     * 演示统计信息
     */
    public void demonstrateStatistics() {
        try {
            Map<String, Object> statistics = configSnapshotService.getSnapshotStatistics(1L, 1L);
            
            System.out.println("快照统计信息:");
            System.out.println("总快照数: " + statistics.get("totalSnapshots"));
            System.out.println("暂存快照数: " + statistics.get("stagedSnapshots"));
            System.out.println("发布快照数: " + statistics.get("publishedSnapshots"));
            
            if (statistics.get("latestVersion") != null) {
                System.out.println("最新版本: " + statistics.get("latestVersion"));
                System.out.println("最新快照名称: " + statistics.get("latestSnapshotName"));
            }

        } catch (Exception e) {
            System.err.println("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 完整的工作流程演示
     */
    public void demonstrateWorkflow() {
        System.out.println("=== 配置快照工作流程演示 ===");
        
        // 1. 暂存配置
        System.out.println("\n1. 暂存配置...");
        demonstrateStaging();
        
        // 2. 发布配置
        System.out.println("\n2. 发布配置...");
        demonstratePublishing();
        
        // 3. 查看统计
        System.out.println("\n3. 查看统计信息...");
        demonstrateStatistics();
        
        // 4. 比较版本
        System.out.println("\n4. 比较版本...");
        demonstrateComparison();
        
        System.out.println("\n=== 演示完成 ===");
    }
}
