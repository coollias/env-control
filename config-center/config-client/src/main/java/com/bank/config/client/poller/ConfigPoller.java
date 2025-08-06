package com.bank.config.client.poller;

import com.bank.config.client.ConfigClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 配置拉取器
 * 负责定时从配置中心拉取配置
 * 
 * @author bank
 */
public class ConfigPoller {
    private static final Logger logger = LoggerFactory.getLogger(ConfigPoller.class);

    private final ConfigClient configClient;
    private final long pollInterval;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ScheduledExecutorService scheduler;

    public ConfigPoller(ConfigClient configClient, long pollInterval) {
        this.configClient = configClient;
        this.pollInterval = pollInterval;
    }

    /**
     * 启动定时拉取
     */
    public void startPolling() {
        if (running.compareAndSet(false, true)) {
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "config-poller");
                thread.setDaemon(true);
                return thread;
            });

            scheduler.scheduleAtFixedRate(() -> {
                try {
                    pollOnce();
                } catch (Exception e) {
                    logger.error("配置拉取失败", e);
                }
            }, pollInterval, pollInterval, TimeUnit.MILLISECONDS);

            logger.info("配置拉取器启动，拉取间隔: {}ms", pollInterval);
        }
    }

    /**
     * 停止定时拉取
     */
    public void stopPolling() {
        if (running.compareAndSet(true, false)) {
            if (scheduler != null) {
                scheduler.shutdown();
                try {
                    if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                        scheduler.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    scheduler.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
            logger.info("配置拉取器已停止");
        }
    }

    /**
     * 执行一次拉取
     */
    public void pollOnce() {
        try {
            logger.debug("开始拉取配置");
            
            // 检查配置版本是否有更新
            String serverVersion = getServerVersion();
            String localVersion = configClient.getCache().getVersion();
            
            if (serverVersion != null && !serverVersion.equals(localVersion)) {
                logger.info("检测到配置版本更新: {} -> {}", localVersion, serverVersion);
                configClient.refreshConfig();
                configClient.getCache().setVersion(serverVersion);
            } else {
                logger.debug("配置版本无更新，跳过拉取");
            }
            
        } catch (Exception e) {
            logger.error("配置拉取失败", e);
        }
    }

    /**
     * 获取服务器配置版本
     */
    private String getServerVersion() {
        try {
            // 这里可以调用配置中心的版本检查接口
            // 暂时返回null，表示无法获取版本信息
            return null;
        } catch (Exception e) {
            logger.warn("获取服务器版本失败", e);
            return null;
        }
    }

    /**
     * 检查是否正在运行
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * 获取拉取间隔
     */
    public long getPollInterval() {
        return pollInterval;
    }
} 