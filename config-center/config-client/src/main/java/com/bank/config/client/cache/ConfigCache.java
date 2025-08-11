package com.bank.config.client.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 配置缓存类
 * 提供本地缓存功能，支持文件持久化
 * 
 * @author bank
 */
public class ConfigCache {
    private static final Logger logger = LoggerFactory.getLogger(ConfigCache.class);

    private final String cacheFile;
    private final long expireTime;
    private final ObjectMapper objectMapper;

    private Map<String, String> configMap;
    private Map<String, ConfigItem> configDetails;
    private LocalDateTime lastUpdateTime;
    private String version;

    public ConfigCache(String cacheFile, long expireTime) {
        this.cacheFile = cacheFile;
        this.expireTime = expireTime;
        this.objectMapper = new ObjectMapper();
        // 注册Java 8日期时间模块
        this.objectMapper.registerModule(new JavaTimeModule());
        // 禁用将日期写为时间戳
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.configMap = new HashMap<>();
        this.configDetails = new HashMap<>();
    }

    /**
     * 获取配置值
     */
    public String get(String key) {
        return configMap.get(key);
    }

    /**
     * 设置配置值
     */
    public void put(String key, String value) {
        configMap.put(key, value);
    }

    /**
     * 获取所有配置
     */
    public Map<String, String> getAllConfigs() {
        return new HashMap<>(configMap);
    }

    /**
     * 更新配置集合
     */
    public void updateConfigs(Map<String, String> newConfigs) {
        // 记录变更的配置
        for (Map.Entry<String, String> entry : newConfigs.entrySet()) {
            String key = entry.getKey();
            String newValue = entry.getValue();
            String oldValue = configMap.get(key);
            
            if (!newValue.equals(oldValue)) {
                logger.debug("配置变更: {} = {} -> {}", key, oldValue, newValue);
            }
        }
        
        this.configMap = new HashMap<>(newConfigs);
        this.lastUpdateTime = LocalDateTime.now();
    }

    /**
     * 检查缓存是否过期
     */
    public boolean isExpired() {
        if (lastUpdateTime == null) {
            return true;
        }
        
        long elapsed = System.currentTimeMillis() - lastUpdateTime.toInstant(java.time.ZoneOffset.UTC).toEpochMilli();
        return elapsed > expireTime;
    }

    /**
     * 检查缓存是否为空
     */
    public boolean isEmpty() {
        return configMap.isEmpty();
    }

    /**
     * 从文件加载缓存
     */
    public void loadFromFile() {
        if (cacheFile == null || cacheFile.trim().isEmpty()) {
            logger.debug("缓存文件路径为空，跳过加载");
            return;
        }

        File file = new File(cacheFile);
        if (!file.exists()) {
            logger.debug("缓存文件不存在: {}", cacheFile);
            return;
        }

        try {
            if (cacheFile.endsWith(".json")) {
                loadFromJsonFile(file);
            } else if (cacheFile.endsWith(".yaml") || cacheFile.endsWith(".yml")) {
                loadFromYamlFile(file);
            } else {
                loadFromPropertiesFile(file);
            }
            logger.debug("从文件加载缓存成功: {}", cacheFile);
        } catch (Exception e) {
            logger.warn("从文件加载缓存失败: {}", cacheFile, e);
        }
    }

    /**
     * 保存缓存到文件
     */
    public void saveToFile() {
        if (cacheFile == null || cacheFile.trim().isEmpty()) {
            logger.debug("缓存文件路径为空，跳过保存");
            return;
        }

        try {
            File file = new File(cacheFile);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            if (cacheFile.endsWith(".json")) {
                saveToJsonFile(file);
            } else if (cacheFile.endsWith(".yaml") || cacheFile.endsWith(".yml")) {
                saveToYamlFile(file);
            } else {
                saveToPropertiesFile(file);
            }
            logger.debug("保存缓存到文件成功: {}", cacheFile);
        } catch (Exception e) {
            logger.warn("保存缓存到文件失败: {}", cacheFile, e);
        }
    }

    /**
     * 从JSON文件加载缓存
     */
    private void loadFromJsonFile(File file) throws IOException {
        CacheData cacheData = objectMapper.readValue(file, CacheData.class);
        this.configMap = cacheData.getConfigs();
        this.lastUpdateTime = cacheData.getLastUpdateTime();
        this.version = cacheData.getVersion();
    }

    /**
     * 保存缓存到JSON文件
     */
    private void saveToJsonFile(File file) throws IOException {
        CacheData cacheData = new CacheData();
        cacheData.setConfigs(configMap);
        cacheData.setLastUpdateTime(lastUpdateTime);
        cacheData.setVersion(version);
        
        objectMapper.writeValue(file, cacheData);
    }

    /**
     * 从Properties文件加载缓存
     */
    private void loadFromPropertiesFile(File file) throws IOException {
        Properties props = new Properties();
        try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
            props.load(fis);
        }

        configMap.clear();
        for (String key : props.stringPropertyNames()) {
            configMap.put(key, props.getProperty(key));
        }

        // 尝试读取元数据
        String lastUpdateStr = props.getProperty("cache.lastUpdateTime");
        if (lastUpdateStr != null) {
            try {
                this.lastUpdateTime = LocalDateTime.parse(lastUpdateStr);
            } catch (Exception e) {
                logger.warn("解析缓存更新时间失败", e);
            }
        }

        this.version = props.getProperty("cache.version");
    }

    /**
     * 保存缓存到Properties文件
     */
    private void saveToPropertiesFile(File file) throws IOException {
        Properties props = new Properties();
        
        // 保存配置
        for (Map.Entry<String, String> entry : configMap.entrySet()) {
            props.setProperty(entry.getKey(), entry.getValue());
        }
        
        // 保存元数据
        if (lastUpdateTime != null) {
            props.setProperty("cache.lastUpdateTime", lastUpdateTime.toString());
        }
        if (version != null) {
            props.setProperty("cache.version", version);
        }

        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
            props.store(fos, "Config Cache");
        }
    }

    /**
     * 保存缓存到YAML文件
     */
    private void saveToYamlFile(File file) throws IOException {
        com.fasterxml.jackson.dataformat.yaml.YAMLFactory yamlFactory = new com.fasterxml.jackson.dataformat.yaml.YAMLFactory();
        ObjectMapper yamlMapper = new ObjectMapper(yamlFactory);
        
        // 创建YAML结构
        Map<String, Object> yamlData = new HashMap<>();
        
        // 添加配置数据
        Map<String, Object> configs = new HashMap<>();
        for (Map.Entry<String, String> entry : configMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            // 处理嵌套键（如 database.pool.timeout）
            String[] keyParts = key.split("\\.");
            Map<String, Object> current = configs;
            
            for (int i = 0; i < keyParts.length - 1; i++) {
                String part = keyParts[i];
                if (!current.containsKey(part)) {
                    current.put(part, new HashMap<String, Object>());
                }
                current = (Map<String, Object>) current.get(part);
            }
            
            current.put(keyParts[keyParts.length - 1], value);
        }
        
        yamlData.put("configs", configs);
        
        // 添加元数据
        if (lastUpdateTime != null) {
            yamlData.put("lastUpdateTime", lastUpdateTime.toString());
        }
        if (version != null) {
            yamlData.put("version", version);
        }
        
        yamlMapper.writeValue(file, yamlData);
    }

    /**
     * 从YAML文件加载缓存
     */
    private void loadFromYamlFile(File file) throws IOException {
        com.fasterxml.jackson.dataformat.yaml.YAMLFactory yamlFactory = new com.fasterxml.jackson.dataformat.yaml.YAMLFactory();
        ObjectMapper yamlMapper = new ObjectMapper(yamlFactory);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> yamlData = yamlMapper.readValue(file, Map.class);
        
        // 清空当前配置
        configMap.clear();
        
        // 解析配置数据
        if (yamlData.containsKey("configs")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> configs = (Map<String, Object>) yamlData.get("configs");
            flattenYamlMap(configs, "", configMap);
        }
        
        // 解析元数据
        if (yamlData.containsKey("lastUpdateTime")) {
            try {
                this.lastUpdateTime = LocalDateTime.parse(yamlData.get("lastUpdateTime").toString());
            } catch (Exception e) {
                logger.warn("解析YAML缓存更新时间失败", e);
            }
        }
        
        if (yamlData.containsKey("version")) {
            this.version = yamlData.get("version").toString();
        }
    }

    /**
     * 将YAML嵌套结构扁平化为key-value格式
     */
    private void flattenYamlMap(Map<String, Object> map, String prefix, Map<String, String> result) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
            
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                flattenYamlMap(nestedMap, fullKey, result);
            } else {
                result.put(fullKey, value.toString());
            }
        }
    }

    /**
     * 清空缓存
     */
    public void clear() {
        configMap.clear();
        configDetails.clear();
        lastUpdateTime = null;
        version = null;
    }

    /**
     * 获取缓存大小
     */
    public int size() {
        return configMap.size();
    }

    /**
     * 获取最后更新时间
     */
    public LocalDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }

    /**
     * 设置版本
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 获取版本
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * 获取缓存文件路径
     */
    public String getCacheFile() {
        return cacheFile;
    }

    /**
     * 缓存数据类
     */
    public static class CacheData {
        private Map<String, String> configs;
        private LocalDateTime lastUpdateTime;
        private String version;

        public Map<String, String> getConfigs() {
            return configs;
        }

        public void setConfigs(Map<String, String> configs) {
            this.configs = configs;
        }

        public LocalDateTime getLastUpdateTime() {
            return lastUpdateTime;
        }

        public void setLastUpdateTime(LocalDateTime lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    /**
     * 配置项详情类
     */
    public static class ConfigItem {
        private String key;
        private String value;
        private String type;
        private boolean encrypted;
        private String description;

        public ConfigItem() {}

        public ConfigItem(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isEncrypted() {
            return encrypted;
        }

        public void setEncrypted(boolean encrypted) {
            this.encrypted = encrypted;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
} 