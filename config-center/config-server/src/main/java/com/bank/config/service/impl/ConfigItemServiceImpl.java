package com.bank.config.service.impl;

import com.bank.config.entity.ConfigItem;
import com.bank.config.entity.ConfigVersion;
import com.bank.config.entity.ConfigChange;
import com.bank.config.entity.Environment;
import com.bank.config.repository.ConfigItemRepository;
import com.bank.config.repository.ConfigVersionRepository;
import com.bank.config.repository.ConfigChangeRepository;
import com.bank.config.repository.EnvironmentRepository;
import com.bank.config.service.ConfigItemService;
import com.bank.config.service.RedisCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 配置项Service实现类
 * 
 * @author bank
 */
@Service
@Transactional
public class ConfigItemServiceImpl implements ConfigItemService {

    @Autowired
    private ConfigItemRepository configItemRepository;

    @Autowired
    private EnvironmentRepository environmentRepository;

    @Autowired
    private ConfigVersionRepository configVersionRepository;

    @Autowired
    private ConfigChangeRepository configChangeRepository;

    @Autowired
    private RedisCacheService redisCacheService;

    @Override
    public ConfigItem createConfigItem(ConfigItem configItem) {
        // 检查配置键是否已存在
        if (existsByAppIdAndEnvIdAndConfigKey(configItem.getAppId(), configItem.getEnvId(), configItem.getConfigKey())) {
            throw new RuntimeException("配置键已存在: " + configItem.getConfigKey());
        }
        
        // 设置默认值
        if (configItem.getStatus() == null) {
            configItem.setStatus(1);
        }
        if (configItem.getConfigType() == null) {
            configItem.setConfigType(1);
        }
        if (configItem.getIsEncrypted() == null) {
            configItem.setIsEncrypted(0);
        }
        if (configItem.getIsRequired() == null) {
            configItem.setIsRequired(0);
        }
        if (configItem.getGroupId() == null) {
            configItem.setGroupId(0L);
        }
        
        ConfigItem saved = configItemRepository.save(configItem);
        
        // 清理相关缓存
        redisCacheService.deleteByPattern("config*");
        
        return saved;
    }

    @Override
    public ConfigItem updateConfigItem(Long id, ConfigItem configItem) {
        Optional<ConfigItem> existingConfig = configItemRepository.findById(id);
        if (!existingConfig.isPresent()) {
            throw new RuntimeException("配置项不存在: " + id);
        }
        
        ConfigItem existing = existingConfig.get();
        
        // 检查配置键是否与其他配置项冲突
        if (!existing.getConfigKey().equals(configItem.getConfigKey()) && 
            existsByAppIdAndEnvIdAndConfigKey(configItem.getAppId(), configItem.getEnvId(), configItem.getConfigKey())) {
            throw new RuntimeException("配置键已存在: " + configItem.getConfigKey());
        }
        
        // 更新字段
        existing.setConfigKey(configItem.getConfigKey());
        existing.setConfigValue(configItem.getConfigValue());
        existing.setConfigType(configItem.getConfigType());
        existing.setIsEncrypted(configItem.getIsEncrypted());
        existing.setIsRequired(configItem.getIsRequired());
        existing.setDefaultValue(configItem.getDefaultValue());
        existing.setDescription(configItem.getDescription());
        existing.setGroupId(configItem.getGroupId());
        
        ConfigItem saved = configItemRepository.save(existing);
        
        // 清理相关缓存
        redisCacheService.deleteByPattern("config*");
        
        return saved;
    }

    @Override
    public void deleteConfigItem(Long id) {
        if (!configItemRepository.existsById(id)) {
            throw new RuntimeException("配置项不存在: " + id);
        }
        configItemRepository.deleteById(id);
        
        // 清理相关缓存
        redisCacheService.deleteByPattern("config*");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConfigItem> findById(Long id) {
        // 尝试从缓存获取
        String cacheKey = "config:" + id;
        Optional<ConfigItem> cached = redisCacheService.get(cacheKey, ConfigItem.class);
        if (cached.isPresent()) {
            System.out.println("=== 配置项缓存命中: " + cacheKey + " ===");
            return cached;
        }
        
        System.out.println("=== 配置项缓存未命中: " + cacheKey + " ===");
        
        // 从数据库获取
        Optional<ConfigItem> config = configItemRepository.findById(id);
        if (config.isPresent()) {
            // 存入缓存
            redisCacheService.set(cacheKey, config.get());
            System.out.println("=== 配置项已缓存: " + cacheKey + " ===");
        }
        
        return config;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConfigItem> findByAppIdAndEnvIdAndConfigKey(Long appId, Long envId, String configKey) {
        // 尝试从缓存获取
        String cacheKey = "config:key:" + appId + ":" + envId + ":" + configKey;
        Optional<ConfigItem> cached = redisCacheService.get(cacheKey, ConfigItem.class);
        if (cached.isPresent()) {
            return cached;
        }
        
        // 从数据库获取
        Optional<ConfigItem> config = configItemRepository.findByAppIdAndEnvIdAndConfigKey(appId, envId, configKey);
        if (config.isPresent()) {
            // 存入缓存
            redisCacheService.set(cacheKey, config.get());
        }
        
        return config;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfigItem> findByAppIdAndEnvId(Long appId, Long envId) {
        // 尝试从缓存获取
        String cacheKey = "config:list:" + appId + ":" + envId;
        Optional<List<ConfigItem>> cached = redisCacheService.get(cacheKey, new com.fasterxml.jackson.core.type.TypeReference<List<ConfigItem>>() {});
        if (cached.isPresent()) {
            System.out.println("=== 配置项列表缓存命中: " + cacheKey + " ===");
            return cached.get();
        }
        
        System.out.println("=== 配置项列表缓存未命中: " + cacheKey + " ===");
        
        // 从数据库获取
        List<ConfigItem> configs = configItemRepository.findByAppIdAndEnvIdAndStatusOrderByConfigKey(appId, envId, 1);
        
        // 存入缓存
        redisCacheService.set(cacheKey, configs);
        System.out.println("=== 配置项列表已缓存: " + cacheKey + " ===");
        
        return configs;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConfigItem> findConfigItems(Long appId, Long envId, String keyword, Integer status, Pageable pageable) {
        // 对于分页查询，不缓存，直接查询数据库
        // 因为分页查询变化频繁，缓存效果不明显
        if (StringUtils.hasText(keyword)) {
            // 有关键字时，需要指定appId和envId
            if (appId == null || envId == null) {
                // 如果appId或envId为null，使用全局搜索
                return configItemRepository.findByKeywordAndStatus(keyword, status, pageable);
            }
            return configItemRepository.findByKeywordAndAppIdAndEnvIdAndStatus(keyword, appId, envId, status, pageable);
        } else {
            // 没有关键字时，根据appId和envId是否为null来决定查询方式
            if (appId == null && envId == null) {
                // 查询所有配置项
                return configItemRepository.findByStatusOrderByConfigKey(status, pageable);
            } else if (appId == null) {
                // 只按环境查询
                return configItemRepository.findByEnvIdAndStatusOrderByConfigKey(envId, status, pageable);
            } else if (envId == null) {
                // 只按应用查询
                return configItemRepository.findByAppIdAndStatusOrderByConfigKey(appId, status, pageable);
            } else {
                // 按应用和环境查询
                return configItemRepository.findByAppIdAndEnvIdAndStatusOrderByConfigKey(appId, envId, status, pageable);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfigItem> findByAppIdAndEnvIdAndGroupId(Long appId, Long envId, Long groupId) {
        return configItemRepository.findByAppIdAndEnvIdAndGroupIdAndStatusOrderByConfigKey(appId, envId, groupId, 1);
    }

    @Override
    public List<ConfigItem> batchCreateConfigItems(List<ConfigItem> configItems) {
        // 检查是否有重复的配置键
        for (ConfigItem configItem : configItems) {
            if (existsByAppIdAndEnvIdAndConfigKey(configItem.getAppId(), configItem.getEnvId(), configItem.getConfigKey())) {
                throw new RuntimeException("配置键已存在: " + configItem.getConfigKey());
            }
        }
        
        return configItemRepository.saveAll(configItems);
    }

    @Override
    public List<ConfigItem> batchUpdateConfigItems(List<ConfigItem> configItems) {
        return configItemRepository.saveAll(configItems);
    }

    @Override
    public ConfigItem updateStatus(Long id, Integer status) {
        Optional<ConfigItem> optional = configItemRepository.findById(id);
        if (!optional.isPresent()) {
            throw new RuntimeException("配置项不存在: " + id);
        }
        
        ConfigItem configItem = optional.get();
        configItem.setStatus(status);
        return configItemRepository.save(configItem);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByAppIdAndEnvId(Long appId, Long envId) {
        return configItemRepository.countByAppIdAndStatus(appId, 1);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAll() {
        return configItemRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(Integer status) {
        return configItemRepository.countByStatus(status);
    }

    private boolean existsByAppIdAndEnvIdAndConfigKey(Long appId, Long envId, String configKey) {
        return configItemRepository.existsByAppIdAndEnvIdAndConfigKey(appId, envId, configKey);
    }

    // ==================== 环境级配置覆盖功能实现 ====================

    @Override
    @Transactional(readOnly = true)
    public List<ConfigItem> getMergedConfigsForAppAndEnv(Long appId, Long envId) {
        // 获取环境继承链
        List<Long> envChain = getEnvironmentInheritanceChain(appId, envId);
        
        // 按环境顺序收集配置，后面的环境会覆盖前面的配置
        Map<String, ConfigItem> mergedConfigs = new LinkedHashMap<>();
        
        for (Long currentEnvId : envChain) {
            List<ConfigItem> envConfigs = configItemRepository.findByAppIdAndEnvIdAndStatusOrderByConfigKey(appId, currentEnvId, 1);
            for (ConfigItem config : envConfigs) {
                mergedConfigs.put(config.getConfigKey(), config);
            }
        }
        
        return new ArrayList<>(mergedConfigs.values());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, ConfigItem> getMergedConfigMapForAppAndEnv(Long appId, Long envId) {
        // 获取环境继承链
        List<Long> envChain = getEnvironmentInheritanceChain(appId, envId);
        
        // 按环境顺序收集配置，后面的环境会覆盖前面的配置
        Map<String, ConfigItem> mergedConfigs = new LinkedHashMap<>();
        
        for (Long currentEnvId : envChain) {
            List<ConfigItem> envConfigs = configItemRepository.findByAppIdAndEnvIdAndStatusOrderByConfigKey(appId, currentEnvId, 1);
            for (ConfigItem config : envConfigs) {
                mergedConfigs.put(config.getConfigKey(), config);
            }
        }
        
        return mergedConfigs;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConfigItem> getConfigWithInheritance(Long appId, Long envId, String configKey) {
        // 获取环境继承链
        List<Long> envChain = getEnvironmentInheritanceChain(appId, envId);
        
        // 从高优先级环境开始查找配置
        for (Long currentEnvId : envChain) {
            Optional<ConfigItem> config = configItemRepository.findByAppIdAndEnvIdAndConfigKey(appId, currentEnvId, configKey);
            if (config.isPresent() && config.get().getStatus() == 1) {
                return config;
            }
        }
        
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getEnvironmentInheritanceChain(Long appId, Long targetEnvId) {
        // 获取目标环境信息
        Optional<Environment> targetEnv = environmentRepository.findById(targetEnvId);
        if (!targetEnv.isPresent()) {
            return Collections.emptyList();
        }
        
        Integer targetSortOrder = targetEnv.get().getSortOrder();
        
        // 获取所有启用的环境，按sort_order排序
        List<Environment> allEnvs = environmentRepository.findByStatusOrderBySortOrder(1);
        
        // 筛选出sort_order小于等于目标环境的环境ID
        return allEnvs.stream()
                .filter(env -> env.getSortOrder() <= targetSortOrder)
                .sorted(Comparator.comparing(Environment::getSortOrder))
                .map(Environment::getId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Map<String, String>> getConfigDifferencesAcrossEnvironments(Long appId) {
        // 获取所有启用的环境，按sort_order排序
        List<Environment> allEnvs = environmentRepository.findByStatusOrderBySortOrder(1);
        
        Map<String, Map<String, String>> differences = new HashMap<>();
        
        // 为每个环境获取合并后的配置
        for (Environment env : allEnvs) {
            Map<String, ConfigItem> envConfigs = getMergedConfigMapForAppAndEnv(appId, env.getId());
            Map<String, String> configValues = envConfigs.values().stream()
                    .collect(Collectors.toMap(
                            ConfigItem::getConfigKey,
                            ConfigItem::getConfigValue
                    ));
            differences.put(env.getEnvCode(), configValues);
        }
        
        return differences;
    }

    // ==================== 配置项版本管理功能实现 ====================

    /**
     * 生成版本号
     */
    private String generateVersionNumber(Long appId, Long envId) {
        String latestVersion = configVersionRepository.findLatestVersionNumber(appId, envId);
        
        if (latestVersion == null) {
            // 如果没有版本，从v1.0.0开始
            return "v1.0.0";
        }
        
        // 解析版本号并递增
        try {
            String[] parts = latestVersion.substring(1).split("\\.");
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            int patch = Integer.parseInt(parts[2]);
            
            // 递增补丁版本
            patch++;
            
            return String.format("v%d.%d.%d", major, minor, patch);
        } catch (Exception e) {
            // 如果版本号格式不正确，使用时间戳
            return "v" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        }
    }

    @Override
    public ConfigItem createConfigItemWithVersion(ConfigItem configItem, String createdBy) {
        // 创建配置项
        ConfigItem created = createConfigItem(configItem);
        
        // 自动生成版本
        String versionNumber = generateVersionNumber(created.getAppId(), created.getEnvId());
        
        // 创建版本记录
        ConfigVersion version = new ConfigVersion();
        version.setAppId(created.getAppId());
        version.setEnvId(created.getEnvId());
        version.setVersionNumber(versionNumber);
        version.setVersionName("新增配置项: " + created.getConfigKey());
        version.setVersionDesc("新增配置项 " + created.getConfigKey());
        version.setChangeType(1);
        version.setCreatedBy(createdBy);
        version.setChangeSummary("新增了 1 个配置项");
        
        version = configVersionRepository.save(version);
        
        // 创建变更记录
        ConfigChange change = new ConfigChange();
        change.setVersionId(version.getId());
        change.setConfigKey(created.getConfigKey());
        change.setOldValue(null); // 新增时原值为null
        change.setNewValue(created.getConfigValue());
        change.setChangeType(1);
        
        configChangeRepository.save(change);
        
        return created;
    }

    @Override
    public ConfigItem updateConfigItemWithVersion(Long id, ConfigItem configItem, String createdBy) {
        // 获取原配置项
        Optional<ConfigItem> existingConfig = findById(id);
        if (!existingConfig.isPresent()) {
            throw new RuntimeException("配置项不存在: " + id);
        }
        
        ConfigItem existing = existingConfig.get();
        
        // 保存原始值，因为updateConfigItem会直接修改existing对象
        String oldValue = existing.getConfigValue();
        
        // 更新配置项
        ConfigItem updated = updateConfigItem(id, configItem);
        
        // 自动生成版本
        String versionNumber = generateVersionNumber(updated.getAppId(), updated.getEnvId());
        
        // 创建版本记录
        ConfigVersion version = new ConfigVersion();
        version.setAppId(updated.getAppId());
        version.setEnvId(updated.getEnvId());
        version.setVersionNumber(versionNumber);
        version.setVersionName("修改配置项: " + updated.getConfigKey());
        version.setVersionDesc("修改配置项 " + updated.getConfigKey() + "，原值: " + oldValue + "，新值: " + updated.getConfigValue());
        version.setChangeType(2);
        version.setCreatedBy(createdBy);
        version.setChangeSummary("修改了 1 个配置项");
        
        version = configVersionRepository.save(version);
        
        // 创建变更记录
        ConfigChange change = new ConfigChange();
        change.setVersionId(version.getId());
        change.setConfigKey(updated.getConfigKey());
        change.setOldValue(oldValue);
        change.setNewValue(updated.getConfigValue());
        change.setChangeType(2);

        configChangeRepository.save(change);
        
        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getConfigItemVersionHistory(Long appId, Long envId, String configKey) {
        // 获取所有版本
        List<ConfigVersion> versions = configVersionRepository.findByAppIdAndEnvIdOrderByCreatedAtDesc(appId, envId);
        
        List<Map<String, Object>> history = new ArrayList<>();
        
        for (ConfigVersion version : versions) {
            List<ConfigChange> changes = configChangeRepository.findByVersionIdOrderByConfigKey(version.getId());
            
            for (ConfigChange change : changes) {
                if (change.getConfigKey().equals(configKey)) {
                    Map<String, Object> historyItem = new HashMap<>();
                    historyItem.put("versionNumber", version.getVersionNumber());
                    historyItem.put("versionName", version.getVersionName());
                    historyItem.put("oldValue", change.getOldValue());
                    historyItem.put("newValue", change.getNewValue());
                    historyItem.put("changeType", change.getChangeType());
                    historyItem.put("createdBy", version.getCreatedBy());
                    historyItem.put("createdAt", version.getCreatedAt());
                    historyItem.put("versionDesc", version.getVersionDesc());
                    history.add(historyItem);
                }
            }
        }
        
        // 按创建时间倒序排序
        history.sort((a, b) -> {
            LocalDateTime timeA = (LocalDateTime) a.get("createdAt");
            LocalDateTime timeB = (LocalDateTime) b.get("createdAt");
            return timeB.compareTo(timeA);
        });
        
        return history;
    }

    @Override
    public ConfigItem rollbackConfigItemToVersion(Long appId, Long envId, String configKey, String targetVersionNumber, String createdBy) {
        // 获取目标版本的配置值
        Optional<ConfigVersion> targetVersion = configVersionRepository.findByAppIdAndEnvIdAndVersionNumber(appId, envId, targetVersionNumber);
        if (!targetVersion.isPresent()) {
            throw new RuntimeException("目标版本不存在: " + targetVersionNumber);
        }
        
        List<ConfigChange> changes = configChangeRepository.findByVersionIdOrderByConfigKey(targetVersion.get().getId());
        String targetValue = null;
        
        for (ConfigChange change : changes) {
            if (change.getConfigKey().equals(configKey)) {
                targetValue = change.getNewValue();
                break;
            }
        }
        
        if (targetValue == null) {
            throw new RuntimeException("在目标版本中未找到配置项: " + configKey);
        }
        
        // 获取当前配置项
        Optional<ConfigItem> currentConfig = findByAppIdAndEnvIdAndConfigKey(appId, envId, configKey);
        if (!currentConfig.isPresent()) {
            throw new RuntimeException("当前配置项不存在: " + configKey);
        }
        
        ConfigItem current = currentConfig.get();
        
        // 更新配置项值
        current.setConfigValue(targetValue);
        ConfigItem updated = updateConfigItem(current.getId(), current);
        
        // 生成回滚版本
        String versionNumber = generateVersionNumber(appId, envId);
        
        // 创建版本记录
        ConfigVersion version = new ConfigVersion();
        version.setAppId(appId);
        version.setEnvId(envId);
        version.setVersionNumber(versionNumber);
        version.setVersionName("回滚配置项: " + configKey);
        version.setVersionDesc("回滚配置项 " + configKey + " 到版本 " + targetVersionNumber);
        version.setChangeType(2);
        version.setCreatedBy(createdBy);
        version.setChangeSummary("回滚了 1 个配置项");
        
        version = configVersionRepository.save(version);
        
        // 创建变更记录
        ConfigChange change = new ConfigChange();
        change.setVersionId(version.getId());
        change.setConfigKey(configKey);
        change.setOldValue(current.getConfigValue()); // 当前值作为原值
        change.setNewValue(targetValue); // 目标版本的值作为新值
        change.setChangeType(2);
        
        configChangeRepository.save(change);
        
        return updated;
    }

    /**
     * 测试缓存方法
     */
    public String testCache(Long id) {
        return "test-value-" + id;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfigItem> findAllConfigItems() {
        // 尝试从缓存获取
        String cacheKey = "config:all";
        Optional<List<ConfigItem>> cached = redisCacheService.get(cacheKey, new com.fasterxml.jackson.core.type.TypeReference<List<ConfigItem>>() {});
        if (cached.isPresent()) {
            System.out.println("=== 从缓存获取所有配置项列表 ===");
            return cached.get();
        }
        
        System.out.println("=== 缓存未命中所有配置项列表 ===");
        
        // 从数据库获取
        List<ConfigItem> configs = configItemRepository.findAll().stream()
                .filter(config -> config.getStatus() != null && config.getStatus() == 1)
                .sorted((a, b) -> a.getConfigKey().compareTo(b.getConfigKey()))
                .collect(java.util.stream.Collectors.toList());
        
        // 存入缓存
        redisCacheService.set(cacheKey, configs);
        System.out.println("=== 所有配置项列表已缓存 ===");
        
        return configs;
    }
} 