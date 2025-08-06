package com.bank.config.client.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * 配置重试类
 * 提供重试机制，支持指数退避
 * 
 * @author bank
 */
public class ConfigRetry {
    private static final Logger logger = LoggerFactory.getLogger(ConfigRetry.class);

    private final int maxRetries;
    private final long retryDelay;
    private final double retryMultiplier;
    private final long maxRetryDelay;

    public ConfigRetry() {
        this(3, 1000, 2.0, 10000);
    }

    public ConfigRetry(int maxRetries, long retryDelay, double retryMultiplier, long maxRetryDelay) {
        this.maxRetries = maxRetries;
        this.retryDelay = retryDelay;
        this.retryMultiplier = retryMultiplier;
        this.maxRetryDelay = maxRetryDelay;
    }

    /**
     * 执行带重试的操作
     */
    public <T> T executeWithRetry(Supplier<T> supplier) {
        return executeWithRetry((Callable<T>) () -> supplier.get());
    }

    /**
     * 执行带重试的操作
     */
    public <T> T executeWithRetry(Callable<T> callable) {
        Exception lastException = null;
        
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return callable.call();
            } catch (Exception e) {
                lastException = e;
                
                if (attempt == maxRetries) {
                    logger.error("重试{}次后仍然失败", maxRetries, e);
                    throw new RuntimeException("重试失败", e);
                }
                
                long delay = calculateDelay(attempt);
                logger.warn("操作失败，{}ms后进行第{}次重试: {}", delay, attempt + 1, e.getMessage());
                
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试被中断", ie);
                }
            }
        }
        
        throw new RuntimeException("重试失败", lastException);
    }

    /**
     * 执行带重试的操作（无返回值）
     */
    public void executeWithRetry(Runnable runnable) {
        executeWithRetry((Callable<Void>) () -> {
            runnable.run();
            return null;
        });
    }

    /**
     * 计算重试延迟时间
     */
    private long calculateDelay(int attempt) {
        long delay = (long) (retryDelay * Math.pow(retryMultiplier, attempt));
        return Math.min(delay, maxRetryDelay);
    }

    /**
     * 检查是否应该重试
     */
    public boolean shouldRetry(Exception exception) {
        // 可以根据异常类型决定是否重试
        if (exception instanceof RuntimeException) {
            String message = exception.getMessage();
            if (message != null) {
                // 网络相关异常通常可以重试
                return message.contains("Connection") ||
                       message.contains("Timeout") ||
                       message.contains("Network") ||
                       message.contains("Socket");
            }
        }
        return true;
    }

    /**
     * 带条件重试的执行
     */
    public <T> T executeWithConditionalRetry(Callable<T> callable) {
        Exception lastException = null;
        
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return callable.call();
            } catch (Exception e) {
                lastException = e;
                
                if (attempt == maxRetries || !shouldRetry(e)) {
                    logger.error("重试{}次后仍然失败或不需要重试", attempt, e);
                    throw new RuntimeException("重试失败", e);
                }
                
                long delay = calculateDelay(attempt);
                logger.warn("操作失败，{}ms后进行第{}次重试: {}", delay, attempt + 1, e.getMessage());
                
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试被中断", ie);
                }
            }
        }
        
        throw new RuntimeException("重试失败", lastException);
    }

    // Getter方法
    public int getMaxRetries() {
        return maxRetries;
    }

    public long getRetryDelay() {
        return retryDelay;
    }

    public double getRetryMultiplier() {
        return retryMultiplier;
    }

    public long getMaxRetryDelay() {
        return maxRetryDelay;
    }
} 