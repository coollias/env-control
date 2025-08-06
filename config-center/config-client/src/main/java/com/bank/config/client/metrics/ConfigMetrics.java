package com.bank.config.client.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 配置监控指标类
 * 收集和统计配置客户端的各种指标
 * 
 * @author bank
 */
public class ConfigMetrics {
    
    // 拉取相关指标
    private final AtomicLong pullCount = new AtomicLong(0);
    private final AtomicLong pullSuccessCount = new AtomicLong(0);
    private final AtomicLong pullErrorCount = new AtomicLong(0);
    private final AtomicLong pullLatency = new AtomicLong(0);
    
    // 缓存相关指标
    private final AtomicLong cacheHitCount = new AtomicLong(0);
    private final AtomicLong cacheMissCount = new AtomicLong(0);
    private final AtomicLong cacheSize = new AtomicLong(0);
    
    // 重试相关指标
    private final AtomicLong retryCount = new AtomicLong(0);
    private final AtomicLong retrySuccessCount = new AtomicLong(0);
    
    // 错误相关指标
    private final AtomicLong errorCount = new AtomicLong(0);
    private final AtomicLong timeoutCount = new AtomicLong(0);
    private final AtomicLong networkErrorCount = new AtomicLong(0);
    
    // 时间相关指标
    private final AtomicLong lastPullTime = new AtomicLong(0);
    private final AtomicLong lastSuccessTime = new AtomicLong(0);
    private final AtomicLong lastErrorTime = new AtomicLong(0);
    
    /**
     * 记录拉取操作
     */
    public void recordPull() {
        pullCount.incrementAndGet();
        lastPullTime.set(System.currentTimeMillis());
    }
    
    /**
     * 记录拉取成功
     */
    public void recordPullSuccess() {
        pullSuccessCount.incrementAndGet();
        lastSuccessTime.set(System.currentTimeMillis());
    }
    
    /**
     * 记录拉取错误
     */
    public void recordPullError() {
        pullErrorCount.incrementAndGet();
        lastErrorTime.set(System.currentTimeMillis());
    }
    
    /**
     * 记录拉取延迟
     */
    public void recordPullLatency(long latency) {
        pullLatency.addAndGet(latency);
    }
    
    /**
     * 记录缓存命中
     */
    public void recordCacheHit() {
        cacheHitCount.incrementAndGet();
    }
    
    /**
     * 记录缓存未命中
     */
    public void recordCacheMiss() {
        cacheMissCount.incrementAndGet();
    }
    
    /**
     * 更新缓存大小
     */
    public void updateCacheSize(int size) {
        cacheSize.set(size);
    }
    
    /**
     * 记录重试操作
     */
    public void recordRetry() {
        retryCount.incrementAndGet();
    }
    
    /**
     * 记录重试成功
     */
    public void recordRetrySuccess() {
        retrySuccessCount.incrementAndGet();
    }
    
    /**
     * 记录错误
     */
    public void recordError() {
        errorCount.incrementAndGet();
    }
    
    /**
     * 记录超时错误
     */
    public void recordTimeout() {
        timeoutCount.incrementAndGet();
    }
    
    /**
     * 记录网络错误
     */
    public void recordNetworkError() {
        networkErrorCount.incrementAndGet();
    }
    
    /**
     * 获取所有指标
     */
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // 拉取指标
        long totalPulls = pullCount.get();
        long successfulPulls = pullSuccessCount.get();
        long failedPulls = pullErrorCount.get();
        
        metrics.put("pull.total", totalPulls);
        metrics.put("pull.success", successfulPulls);
        metrics.put("pull.error", failedPulls);
        metrics.put("pull.successRate", totalPulls > 0 ? (double) successfulPulls / totalPulls : 0.0);
        metrics.put("pull.errorRate", totalPulls > 0 ? (double) failedPulls / totalPulls : 0.0);
        metrics.put("pull.averageLatency", totalPulls > 0 ? (double) pullLatency.get() / totalPulls : 0.0);
        
        // 缓存指标
        long cacheHits = cacheHitCount.get();
        long cacheMisses = cacheMissCount.get();
        long totalCacheAccess = cacheHits + cacheMisses;
        
        metrics.put("cache.hits", cacheHits);
        metrics.put("cache.misses", cacheMisses);
        metrics.put("cache.hitRate", totalCacheAccess > 0 ? (double) cacheHits / totalCacheAccess : 0.0);
        metrics.put("cache.size", cacheSize.get());
        
        // 重试指标
        long totalRetries = retryCount.get();
        long successfulRetries = retrySuccessCount.get();
        
        metrics.put("retry.total", totalRetries);
        metrics.put("retry.success", successfulRetries);
        metrics.put("retry.successRate", totalRetries > 0 ? (double) successfulRetries / totalRetries : 0.0);
        
        // 错误指标
        metrics.put("error.total", errorCount.get());
        metrics.put("error.timeout", timeoutCount.get());
        metrics.put("error.network", networkErrorCount.get());
        
        // 时间指标
        metrics.put("time.lastPull", lastPullTime.get());
        metrics.put("time.lastSuccess", lastSuccessTime.get());
        metrics.put("time.lastError", lastErrorTime.get());
        
        // 计算时间差
        long now = System.currentTimeMillis();
        metrics.put("time.sinceLastPull", now - lastPullTime.get());
        metrics.put("time.sinceLastSuccess", now - lastSuccessTime.get());
        metrics.put("time.sinceLastError", now - lastErrorTime.get());
        
        return metrics;
    }
    
    /**
     * 获取指标摘要
     */
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        long totalPulls = pullCount.get();
        long successfulPulls = pullSuccessCount.get();
        long cacheHits = cacheHitCount.get();
        long cacheMisses = cacheMissCount.get();
        long totalCacheAccess = cacheHits + cacheMisses;
        
        summary.put("totalPulls", totalPulls);
        summary.put("successRate", totalPulls > 0 ? String.format("%.2f%%", (double) successfulPulls / totalPulls * 100) : "0.00%");
        summary.put("cacheHitRate", totalCacheAccess > 0 ? String.format("%.2f%%", (double) cacheHits / totalCacheAccess * 100) : "0.00%");
        summary.put("cacheSize", cacheSize.get());
        summary.put("errorCount", errorCount.get());
        
        return summary;
    }
    
    /**
     * 重置所有指标
     */
    public void reset() {
        pullCount.set(0);
        pullSuccessCount.set(0);
        pullErrorCount.set(0);
        pullLatency.set(0);
        cacheHitCount.set(0);
        cacheMissCount.set(0);
        cacheSize.set(0);
        retryCount.set(0);
        retrySuccessCount.set(0);
        errorCount.set(0);
        timeoutCount.set(0);
        networkErrorCount.set(0);
        lastPullTime.set(0);
        lastSuccessTime.set(0);
        lastErrorTime.set(0);
    }
    
    /**
     * 获取健康状态
     */
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        
        long totalPulls = pullCount.get();
        long successfulPulls = pullSuccessCount.get();
        long lastSuccess = lastSuccessTime.get();
        long now = System.currentTimeMillis();
        
        // 计算成功率
        double successRate = totalPulls > 0 ? (double) successfulPulls / totalPulls : 0.0;
        
        // 判断健康状态
        String status = "UNKNOWN";
        if (totalPulls == 0) {
            status = "UNKNOWN";
        } else if (successRate >= 0.95 && (now - lastSuccess) < 300000) { // 95%成功率且5分钟内成功过
            status = "HEALTHY";
        } else if (successRate >= 0.8 && (now - lastSuccess) < 600000) { // 80%成功率且10分钟内成功过
            status = "WARNING";
        } else {
            status = "UNHEALTHY";
        }
        
        health.put("status", status);
        health.put("successRate", successRate);
        health.put("lastSuccessTime", lastSuccess);
        health.put("timeSinceLastSuccess", now - lastSuccess);
        health.put("totalPulls", totalPulls);
        health.put("errorCount", errorCount.get());
        
        return health;
    }
} 