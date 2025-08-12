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
import com.bank.config.client.websocket.WebSocketConfigClient;
import com.bank.config.client.hotupdate.ConfigHotUpdateManager;
import com.bank.config.client.hotupdate.ConfigHotUpdateProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
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
    
    // 热更新相关组件
    private final ConfigHotUpdateManager hotUpdateManager;
    private final ConfigHotUpdateProcessor hotUpdateProcessor;
    
    // WebSocket客户端
    private WebSocketConfigClient webSocketClient;
    private final boolean enableWebSocket;
    private final Long appId;
    private final String instanceId;
    private final String instanceIp;
    private final String clientVersion;

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
        
        // 初始化热更新组件
        this.hotUpdateManager = new ConfigHotUpdateManager(this.cache);
        this.hotUpdateProcessor = new ConfigHotUpdateProcessor(this.hotUpdateManager, this.cache);

        // 初始化拉取器
        this.poller = new ConfigPoller(this, pollInterval);
        
        // 初始化WebSocket客户端
        this.enableWebSocket = builder.enableWebSocket;
        this.appId = builder.appId;
        this.instanceId = builder.instanceId;
        this.instanceIp = builder.instanceIp;
        this.clientVersion = builder.clientVersion;
        
        // 如果启用WebSocket，创建WebSocket客户端
        if (this.enableWebSocket && this.appId != null && this.instanceId != null) {
            this.webSocketClient = new WebSocketConfigClient(
                this.serverUrl, 
                this.appId, 
                this.instanceId, 
                this.instanceIp, 
                this.clientVersion
            );
            // 设置WebSocket监听器
            setupWebSocketListeners();
        }
        
        if (enableWebSocket) {
            this.webSocketClient = new WebSocketConfigClient(
                serverUrl,
                appId,
                instanceId,
                instanceIp,
                clientVersion
            );
            
            // 设置WebSocket监听器
            setupWebSocketListeners();
        }
    }

    /**
     * 设置WebSocket监听器
     */
    private void setupWebSocketListeners() {
        if (webSocketClient != null) {
            // 设置配置更新监听器
            webSocketClient.setConfigUpdateListener(new WebSocketConfigClient.ConfigUpdateListener() {
                @Override
                public void onConfigUpdate(Long appId, Long envId, Map<String, Object> configData) {
                    logger.info("收到WebSocket配置更新: appId={}, envId={}", appId, envId);
                    
                    // 将配置数据转换为Map<String, String>格式
                    Map<String, String> newConfigs = convertConfigData(configData);
                    
                    // 更新本地缓存
                    if (enableCache) {
                        cache.updateConfigs(newConfigs);
                    }
                    
                    // 通知所有监听器
                    notifyConfigRefresh(newConfigs);
                    
                    // 触发热更新处理
                    triggerHotUpdate(newConfigs);
                }
            });
            
            // 设置配置变更通知监听器
            webSocketClient.setNotificationListener(new WebSocketConfigClient.ConfigChangeNotificationListener() {
                @Override
                public void onConfigChangeNotification(Long appId, Long envId, String versionNumber, String changeType) {
                    logger.info("收到配置变更通知: appId={}, envId={}, version={}, type={}", 
                        appId, envId, versionNumber, changeType);
                    
                    // 可以在这里添加特定的通知处理逻辑
                    if ("PUBLISH".equals(changeType)) {
                        logger.info("配置已发布，版本: {}", versionNumber);
                    }
                }
            });
        }
    }
    
    /**
     * 转换配置数据格式
     */
    private Map<String, String> convertConfigData(Map<String, Object> configData) {
        Map<String, String> result = new java.util.HashMap<>();
        convertConfigDataRecursive(configData, "", result);
        return result;
    }
    
    /**
     * 递归转换配置数据
     */
    private void convertConfigDataRecursive(Map<String, Object> configData, String prefix, Map<String, String> result) {
        for (Map.Entry<String, Object> entry : configData.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
            
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                convertConfigDataRecursive(nestedMap, fullKey, result);
            } else {
                result.put(fullKey, value != null ? value.toString() : "");
            }
        }
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
                
                // 初始化WebSocket客户端（如果还没有初始化）
                if (enableWebSocket && webSocketClient == null && appId != null && instanceId != null) {
                    this.webSocketClient = new WebSocketConfigClient(
                        this.serverUrl, 
                        this.appId, 
                        this.instanceId, 
                        this.instanceIp, 
                        this.clientVersion
                    );
                    // 设置WebSocket监听器
                    setupWebSocketListeners();
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
        
        // 触发热更新处理
        triggerHotUpdate(newConfigs);
    }
    
    /**
     * 触发热更新处理
     */
    private void triggerHotUpdate(Map<String, String> newConfigs) {
        try {
            // 这里可以添加热更新逻辑
            // 例如：重新处理所有已绑定的对象
            logger.debug("配置刷新，触发热更新处理，共{}个配置项", newConfigs.size());
        } catch (Exception e) {
            logger.error("热更新处理失败", e);
        }
    }

    /**
     * 启动客户端
     */
    public void start() {
        if (!initialized.get()) {
            initialize();
        }
        if (!running.get()) {
            // 启动WebSocket客户端
            if (enableWebSocket && webSocketClient != null) {
                webSocketClient.connect();
                logger.info("WebSocket客户端已连接");
            }
            
            // 启动轮询器
            if (enablePolling) {
                poller.startPolling();
            }
            
            running.set(true);
            logger.info("配置客户端启动成功");
        }
    }

    /**
     * 停止客户端
     */
    public void stop() {
        if (running.compareAndSet(true, false)) {
            // 停止WebSocket客户端
            if (enableWebSocket && webSocketClient != null) {
                webSocketClient.disconnect();
                logger.info("WebSocket客户端已断开");
            }
            
            // 停止轮询器
            if (enablePolling) {
                poller.stopPolling();
            }
            
            try {
                httpClient.close();
            } catch (Exception e) {
                logger.error("关闭HTTP客户端失败", e);
            }
            
            logger.info("配置客户端已停止");
        }
    }

    /**
     * 检查客户端是否健康
     */
    public boolean isHealthy() {
        return healthCheck.isHealthy();
    }
    
    // ==================== 热更新相关方法 ====================
    
    /**
     * 启用对象的配置热更新
     * 使用@ConfigValue注解标记的字段将自动更新
     * 
     * @param target 目标对象
     */
    public void enableHotUpdate(Object target) {
        if (hotUpdateProcessor != null) {
            hotUpdateProcessor.processObject(target);
            logger.info("已启用对象热更新: {}", target.getClass().getSimpleName());
        }
    }
    
    /**
     * 手动绑定配置字段到对象属性
     * 
     * @param configKey 配置键
     * @param target 目标对象
     * @param fieldName 字段名
     */
    public void bindConfigField(String configKey, Object target, String fieldName) {
        if (hotUpdateManager != null) {
            hotUpdateManager.bindConfigField(configKey, target, fieldName);
        }
    }
    
    /**
     * 手动绑定配置到方法调用
     * 
     * @param configKey 配置键
     * @param target 目标对象
     * @param methodName 方法名
     * @param parameterTypes 参数类型
     */
    public void bindConfigMethod(String configKey, Object target, String methodName, Class<?>... parameterTypes) {
        if (hotUpdateManager != null) {
            hotUpdateManager.bindConfigMethod(configKey, target, methodName, parameterTypes);
        }
    }
    
    /**
     * 获取热更新管理器
     */
    public ConfigHotUpdateManager getHotUpdateManager() {
        return hotUpdateManager;
    }
    
    /**
     * 获取热更新处理器
     */
    public ConfigHotUpdateProcessor getHotUpdateProcessor() {
        return hotUpdateProcessor;
    }
    
    /**
     * 获取WebSocket客户端
     */
    public WebSocketConfigClient getWebSocketClient() {
        return webSocketClient;
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
        private boolean enableWebSocket = false;
        private Long appId;
        private String instanceId;
        private String instanceIp;
        private String clientVersion = "1.0.0";

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
        
        public ConfigClientBuilder enableWebSocket(boolean enableWebSocket) {
            this.enableWebSocket = enableWebSocket;
            return this;
        }
        
        public ConfigClientBuilder appId(Long appId) {
            this.appId = appId;
            return this;
        }
        
        public ConfigClientBuilder instanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }
        
        public ConfigClientBuilder instanceIp(String instanceIp) {
            this.instanceIp = instanceIp;
            return this;
        }
        
        public ConfigClientBuilder clientVersion(String clientVersion) {
            this.clientVersion = clientVersion;
            return this;
        }

        public ConfigClient build() {
            if (serverUrl == null || appCode == null || envCode == null) {
                throw new IllegalArgumentException("serverUrl, appCode, envCode 不能为空");
            }
            
            if (enableWebSocket && (appId == null || instanceId == null)) {
                throw new IllegalArgumentException("启用WebSocket时，appId和instanceId不能为空");
            }
            
            return new ConfigClient(this);
        }
    }
} 