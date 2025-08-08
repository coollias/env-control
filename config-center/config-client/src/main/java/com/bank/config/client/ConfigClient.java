package com.bank.config.client;

import com.bank.config.client.cache.ConfigCache;
import com.bank.config.client.poller.ConfigPoller;
import com.bank.config.client.poller.ConfigChangeListener;
import com.bank.config.client.parser.ConfigParser;
import com.bank.config.client.security.ConfigSecurity;
import com.bank.config.client.fallback.ConfigFallback;
import com.bank.config.client.fallback.DefaultConfigFallback;
import com.bank.config.client.retry.ConfigRetry;
import com.bank.config.client.metrics.ConfigMetrics;
import com.bank.config.client.health.ConfigHealthCheck;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 配置客户端核心类
 * 提供配置拉取、缓存、监听等功能
 * 
 * @author bank
 */
public class ConfigClient {
    private static final Logger logger = LoggerFactory.getLogger(ConfigClient.class);

    private final String serverUrl;
    private final String appCode;
    private final String envCode;
    private final String token;
    private final long pollInterval;
    private final String cacheFile;
    private final boolean enablePolling;
    private final boolean enableCache;
    private final long cacheExpireTime;

    private final ConfigCache cache;
    private final ConfigPoller poller;
    private final ConfigParser parser;
    private final ConfigSecurity security;
    private final ConfigFallback fallback;
    private final ConfigRetry retry;
    private final ConfigMetrics metrics;
    private final ConfigHealthCheck healthCheck;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    private final List<ConfigChangeListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);

    private ConfigClient(ConfigClientBuilder builder) {
        this.serverUrl = builder.serverUrl;
        this.appCode = builder.appCode;
        this.envCode = builder.envCode;
        this.token = builder.token;
        this.pollInterval = builder.pollInterval;
        this.cacheFile = builder.cacheFile;
        this.enablePolling = builder.enablePolling;
        this.enableCache = builder.enableCache;
        this.cacheExpireTime = builder.cacheExpireTime;

        // 初始化组件
        this.cache = new ConfigCache(cacheFile, cacheExpireTime);
        this.parser = new ConfigParser();
        this.security = new ConfigSecurity(token, appCode, envCode);
        this.fallback = new DefaultConfigFallback();
        this.retry = new ConfigRetry();
        this.metrics = new ConfigMetrics();
        this.healthCheck = new ConfigHealthCheck(this);
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();

        // 初始化拉取器
        this.poller = new ConfigPoller(this, pollInterval);
    }

    /**
     * 初始化客户端
     */
    public void initialize() {
        if (initialized.compareAndSet(false, true)) {
            logger.info("初始化配置客户端: appCode={}, envCode={}", appCode, envCode);
            
            try {
                // 加载本地缓存
                if (enableCache) {
                    cache.loadFromFile();
                }

                // 如果缓存过期或不存在，从服务器拉取
                if (cache.isExpired() || cache.isEmpty()) {
                    refreshConfig();
                }

                // 启动定时拉取
                if (enablePolling) {
                    poller.startPolling();
                    running.set(true);
                }

                logger.info("配置客户端初始化完成");
            } catch (Exception e) {
                logger.error("配置客户端初始化失败", e);
                throw new RuntimeException("配置客户端初始化失败", e);
            }
        }
    }

    /**
     * 获取单个配置项
     */
    public String getConfig(String key) {
        return getConfig(key, null);
    }

    /**
     * 获取单个配置项，支持默认值
     */
    public String getConfig(String key, String defaultValue) {
        try {
            // 1. 尝试从缓存获取
            if (enableCache) {
                String value = cache.get(key);
                if (value != null) {
                    metrics.recordCacheHit();
                    return value;
                }
                metrics.recordCacheMiss();
            }

            // 2. 尝试从服务器拉取
            refreshConfig();
            if (enableCache) {
                String value = cache.get(key);
                if (value != null) {
                    return value;
                }
            }

            // 3. 使用默认值
            return defaultValue != null ? defaultValue : fallback.getDefaultValue(key);

        } catch (Exception e) {
            logger.error("获取配置失败: {}", key, e);
            return defaultValue != null ? defaultValue : fallback.getDefaultValue(key);
        }
    }

    /**
     * 获取所有配置
     */
    public Map<String, String> getAllConfigs() {
        try {
            if (enableCache) {
                Map<String, String> cachedConfigs = cache.getAllConfigs();
                if (!cachedConfigs.isEmpty()) {
                    return cachedConfigs;
                }
            }

            refreshConfig();
            return enableCache ? cache.getAllConfigs() : new HashMap<>();

        } catch (Exception e) {
            logger.error("获取所有配置失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 刷新配置
     */
    public void refreshConfig() {
        retry.executeWithRetry((Callable<Void>) () -> {
            try {
                metrics.recordPull();
                
                // 从服务器拉取配置
                Map<String, String> newConfigs = fetchConfigsFromServer();
                
                // 更新缓存
                if (enableCache) {
                    cache.updateConfigs(newConfigs);
                    cache.saveToFile();
                }

                // 通知监听器
                notifyConfigRefresh(newConfigs);
                
                metrics.recordPullSuccess();
                logger.debug("配置刷新成功，共{}个配置项", newConfigs.size());
                
                return null;
            } catch (Exception e) {
                metrics.recordPullError();
                throw e;
            }
        });
    }

    /**
     * 从服务器拉取配置
     */
    private Map<String, String> fetchConfigsFromServer() throws Exception {
        String url = String.format("%s/api/client/configs/%s/%s", serverUrl, appCode, envCode);
        
        // 创建HTTP请求
        org.apache.http.client.methods.HttpGet request = 
            new org.apache.http.client.methods.HttpGet(url);
        
        // 添加认证头
        security.addAuthHeaders(request);
        
        // 执行请求
        try (org.apache.http.client.methods.CloseableHttpResponse response = 
                httpClient.execute(request)) {
            
            if (response.getStatusLine().getStatusCode() == 200) {
                String responseBody = org.apache.http.util.EntityUtils.toString(response.getEntity());
                return parseResponse(responseBody);
            } else {
                throw new RuntimeException("服务器响应错误: " + response.getStatusLine().getStatusCode());
            }
        }
    }

    /**
     * 解析服务器响应
     */
    private Map<String, String> parseResponse(String responseBody) throws Exception {
        // 解析JSON响应
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        
        if (response.containsKey("data")) {
            @SuppressWarnings("unchecked")
            Map<String, String> configs = (Map<String, String>) response.get("data");
            return configs;
        } else {
            throw new RuntimeException("响应格式错误");
        }
    }

    /**
     * 添加配置变更监听器
     */
    public void addConfigChangeListener(ConfigChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * 移除配置变更监听器
     */
    public void removeConfigChangeListener(ConfigChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * 通知配置变更
     */
    void notifyConfigChange(String key, String oldValue, String newValue) {
        for (ConfigChangeListener listener : listeners) {
            try {
                listener.onConfigChange(key, oldValue, newValue);
            } catch (Exception e) {
                logger.error("配置变更监听器执行失败", e);
            }
        }
    }

    /**
     * 通知配置刷新
     */
    void notifyConfigRefresh(Map<String, String> newConfigs) {
        for (ConfigChangeListener listener : listeners) {
            try {
                listener.onConfigRefresh(newConfigs);
            } catch (Exception e) {
                logger.error("配置刷新监听器执行失败", e);
            }
        }
    }

    /**
     * 启动客户端
     */
    public void start() {
        if (!initialized.get()) {
            initialize();
        }
        if (enablePolling && !running.get()) {
            poller.startPolling();
            running.set(true);
        }
    }

    /**
     * 停止客户端
     */
    public void stop() {
        if (running.compareAndSet(true, false)) {
            poller.stopPolling();
            try {
                httpClient.close();
            } catch (Exception e) {
                logger.error("关闭HTTP客户端失败", e);
            }
        }
    }

    /**
     * 检查客户端是否健康
     */
    public boolean isHealthy() {
        return healthCheck.isHealthy();
    }



    // Getter方法
    public String getServerUrl() { return serverUrl; }
    public String getAppCode() { return appCode; }
    public String getEnvCode() { return envCode; }
    public ConfigCache getCache() { return cache; }
    public ConfigPoller getPoller() { return poller; }
    public ConfigMetrics getMetrics() { return metrics; }
    
    /**
     * 获取监控指标
     */
    public Map<String, Object> getMetricsData() {
        return metrics.getMetrics();
    }

    /**
     * 配置客户端构建器
     */
    public static class ConfigClientBuilder {
        private String serverUrl;
        private String appCode;
        private String envCode;
        private String token;
        private long pollInterval = 30000; // 30秒
        private String cacheFile;
        private boolean enablePolling = true;
        private boolean enableCache = true;
        private long cacheExpireTime = 300000; // 5分钟

        public ConfigClientBuilder serverUrl(String serverUrl) {
            this.serverUrl = serverUrl;
            return this;
        }

        public ConfigClientBuilder appCode(String appCode) {
            this.appCode = appCode;
            return this;
        }

        public ConfigClientBuilder envCode(String envCode) {
            this.envCode = envCode;
            return this;
        }

        public ConfigClientBuilder token(String token) {
            this.token = token;
            return this;
        }

        public ConfigClientBuilder pollInterval(long pollInterval) {
            this.pollInterval = pollInterval;
            return this;
        }

        public ConfigClientBuilder cacheFile(String cacheFile) {
            this.cacheFile = cacheFile;
            return this;
        }

        public ConfigClientBuilder enablePolling(boolean enablePolling) {
            this.enablePolling = enablePolling;
            return this;
        }

        public ConfigClientBuilder enableCache(boolean enableCache) {
            this.enableCache = enableCache;
            return this;
        }

        public ConfigClientBuilder cacheExpireTime(long cacheExpireTime) {
            this.cacheExpireTime = cacheExpireTime;
            return this;
        }

        public ConfigClient build() {
            if (serverUrl == null || appCode == null || envCode == null) {
                throw new IllegalArgumentException("serverUrl, appCode, envCode 不能为空");
            }
            return new ConfigClient(this);
        }
    }
} 