package com.bank.config.service.impl;

import com.bank.config.entity.ConfigItem;
import com.bank.config.entity.ConfigSnapshot;
import com.bank.config.entity.ConfigSnapshotItem;
import com.bank.config.repository.ConfigItemRepository;
import com.bank.config.repository.ConfigSnapshotItemRepository;
import com.bank.config.repository.ConfigSnapshotRepository;
import com.bank.config.service.ConfigSnapshotService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 配置快照Service实现类
 * 
 * @author bank
 */
@Service
@Transactional
public class ConfigSnapshotServiceImpl implements ConfigSnapshotService {

    @Autowired
    private ConfigSnapshotRepository configSnapshotRepository;

    @Autowired
    private ConfigSnapshotItemRepository configSnapshotItemRepository;

    @Autowired
    private ConfigItemRepository configItemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @org.springframework.cache.annotation.CacheEvict(value = "snapshots", allEntries = true)
    public ConfigSnapshot createSnapshot(Long appId, Long envId, String snapshotName, String snapshotDesc,
                                      Map<String, Object> configData, String createdBy) {
        
        // 生成版本号
        String versionNumber = generateVersionNumber(appId, envId);
        
        // 创建快照
        ConfigSnapshot snapshot = new ConfigSnapshot();
        snapshot.setAppId(appId);
        snapshot.setEnvId(envId);
        snapshot.setSnapshotName(snapshotName);
        snapshot.setSnapshotDesc(snapshotDesc);
        snapshot.setVersionNumber(versionNumber);
        snapshot.setSnapshotType(1); // 暂存类型
        snapshot.setStatus(1);
        snapshot.setCreatedBy(createdBy);
        
        // 将配置数据转换为JSON字符串
        try {
            String configDataJson = objectMapper.writeValueAsString(configData);
            snapshot.setConfigData(configDataJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("配置数据序列化失败", e);
        }
        
        // 保存快照
        snapshot = configSnapshotRepository.save(snapshot);
        
        // 保存配置项详情
        saveSnapshotItems(snapshot.getId(), configData);
        
        // 更新配置项数量
        Long configCount = configSnapshotItemRepository.countBySnapshotId(snapshot.getId());
        snapshot.setConfigCount(configCount.intValue());
        configSnapshotRepository.save(snapshot);
        
        return snapshot;
    }

    @Override
    @org.springframework.cache.annotation.CacheEvict(value = "snapshots", allEntries = true)
    public ConfigSnapshot publishSnapshot(Long snapshotId, String publishedBy) {
        Optional<ConfigSnapshot> snapshotOpt = findById(snapshotId);
        if (!snapshotOpt.isPresent()) {
            throw new RuntimeException("快照不存在: " + snapshotId);
        }
        
        ConfigSnapshot snapshot = snapshotOpt.get();
        
        // 创建发布快照
        ConfigSnapshot publishSnapshot = new ConfigSnapshot();
        publishSnapshot.setAppId(snapshot.getAppId());
        publishSnapshot.setEnvId(snapshot.getEnvId());
        publishSnapshot.setSnapshotName("发布版本: " + snapshot.getSnapshotName());
        publishSnapshot.setSnapshotDesc("发布版本: " + snapshot.getSnapshotDesc());
        publishSnapshot.setVersionNumber(snapshot.getVersionNumber());
        publishSnapshot.setSnapshotType(2); // 发布类型
        publishSnapshot.setStatus(1);
        publishSnapshot.setConfigData(snapshot.getConfigData());
        publishSnapshot.setConfigCount(snapshot.getConfigCount());
        publishSnapshot.setCreatedBy(publishedBy);
        
        // 保存发布快照
        publishSnapshot = configSnapshotRepository.save(publishSnapshot);
        
        // 复制配置项详情
        List<ConfigSnapshotItem> items = getSnapshotItems(snapshotId);
        for (ConfigSnapshotItem item : items) {
            ConfigSnapshotItem newItem = new ConfigSnapshotItem();
            newItem.setSnapshotId(publishSnapshot.getId());
            newItem.setConfigKey(item.getConfigKey());
            newItem.setConfigValue(item.getConfigValue());
            newItem.setConfigType(item.getConfigType());
            newItem.setIsEncrypted(item.getIsEncrypted());
            newItem.setIsRequired(item.getIsRequired());
            newItem.setDefaultValue(item.getDefaultValue());
            newItem.setDescription(item.getDescription());
            newItem.setGroupId(item.getGroupId());
            newItem.setSortOrder(item.getSortOrder());
            configSnapshotItemRepository.save(newItem);
        }
        
        return publishSnapshot;
    }

    @Override
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "snapshots", key = "#id")
    public Optional<ConfigSnapshot> findById(Long id) {
        return configSnapshotRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "snapshots", key = "'app:' + #appId + ':env:' + #envId + ':version:' + #versionNumber")
    public Optional<ConfigSnapshot> findByAppIdAndEnvIdAndVersionNumber(Long appId, Long envId, String versionNumber) {
        return configSnapshotRepository.findByAppIdAndEnvIdAndVersionNumber(appId, envId, versionNumber);
    }

    @Override
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "snapshots", key = "'app:' + #appId + ':env:' + #envId + ':list'")
    public List<ConfigSnapshot> findByAppIdAndEnvId(Long appId, Long envId) {
        return configSnapshotRepository.findByAppIdAndEnvIdOrderByCreatedAtDesc(appId, envId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConfigSnapshot> findByAppIdAndEnvId(Long appId, Long envId, Pageable pageable) {
        return configSnapshotRepository.findByAppIdAndEnvIdOrderByCreatedAtDesc(appId, envId, pageable);
    }

    @Override
    @org.springframework.cache.annotation.CacheEvict(value = "snapshots", allEntries = true)
    public void deleteSnapshot(Long id) {
        if (!configSnapshotRepository.existsById(id)) {
            throw new RuntimeException("快照不存在: " + id);
        }
        
        // 删除配置项详情
        configSnapshotItemRepository.deleteBySnapshotId(id);
        
        // 删除快照
        configSnapshotRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public String getLatestVersionNumber(Long appId, Long envId) {
        return configSnapshotRepository.findLatestVersionNumber(appId, envId);
    }

    @Override
    public String generateVersionNumber(Long appId, Long envId) {
        String latestVersion = getLatestVersionNumber(appId, envId);
        
        if (latestVersion == null) {
            // 如果没有版本，从v1.0.0开始
            return "v1.0.0";
        }
        
        try {
            // 解析版本号 v1.2.3
            String versionStr = latestVersion.substring(1); // 去掉v前缀
            String[] parts = versionStr.split("\\.");
            
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
    @Transactional(readOnly = true)
    public List<ConfigSnapshotItem> getSnapshotItems(Long snapshotId) {
        return configSnapshotItemRepository.findBySnapshotIdOrderBySortOrderAscConfigKeyAsc(snapshotId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getSnapshotConfigData(Long snapshotId) {
        Optional<ConfigSnapshot> snapshotOpt = findById(snapshotId);
        if (!snapshotOpt.isPresent()) {
            throw new RuntimeException("快照不存在: " + snapshotId);
        }
        
        ConfigSnapshot snapshot = snapshotOpt.get();
        
        try {
            return objectMapper.readValue(snapshot.getConfigData(), Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("配置数据反序列化失败", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> compareSnapshots(Long snapshotId1, Long snapshotId2) {
        Map<String, Object> config1 = getSnapshotConfigData(snapshotId1);
        Map<String, Object> config2 = getSnapshotConfigData(snapshotId2);
        
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(config1.keySet());
        allKeys.addAll(config2.keySet());
        
        Map<String, Object> allDifferences = new HashMap<>();
        for (String key : allKeys) {
            Object value1 = config1.get(key);
            Object value2 = config2.get(key);
            
            Map<String, Object> diff = new HashMap<>();
            diff.put("snapshot1", value1);
            diff.put("snapshot2", value2);
            diff.put("hasDiff", !Objects.equals(value1, value2));
            allDifferences.put(key, diff);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("snapshot1", snapshotId1);
        result.put("snapshot2", snapshotId2);
        result.put("differences", allDifferences);
        
        return result;
    }

    @Override
    public ConfigSnapshot rollbackToSnapshot(Long appId, Long envId, Long targetSnapshotId, String createdBy) {
        Optional<ConfigSnapshot> targetSnapshot = findById(targetSnapshotId);
        if (!targetSnapshot.isPresent()) {
            throw new RuntimeException("目标快照不存在: " + targetSnapshotId);
        }
        
        ConfigSnapshot snapshot = targetSnapshot.get();
        Map<String, Object> targetConfigs = getSnapshotConfigData(targetSnapshotId);
        
        // 创建回滚快照
        String rollbackName = "回滚到 " + snapshot.getVersionNumber();
        String rollbackDesc = "回滚到快照 " + snapshot.getSnapshotName();
        
        return createSnapshot(appId, envId, rollbackName, rollbackDesc, targetConfigs, createdBy);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConfigSnapshot> getLatestPublishedSnapshot(Long appId, Long envId) {
        List<ConfigSnapshot> snapshots = configSnapshotRepository.findLatestPublishedSnapshots(appId, envId, 
            org.springframework.data.domain.PageRequest.of(0, 1));
        return snapshots.isEmpty() ? Optional.empty() : Optional.of(snapshots.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConfigSnapshot> getLatestStagedSnapshot(Long appId, Long envId) {
        List<ConfigSnapshot> snapshots = configSnapshotRepository.findLatestStagedSnapshots(appId, envId, 
            org.springframework.data.domain.PageRequest.of(0, 1));
        return snapshots.isEmpty() ? Optional.empty() : Optional.of(snapshots.get(0));
    }

    @Override
    @org.springframework.cache.annotation.CacheEvict(value = "configs", allEntries = true)
    public void applySnapshotToEnvironment(Long snapshotId, String appliedBy) {
        Optional<ConfigSnapshot> snapshotOpt = findById(snapshotId);
        if (!snapshotOpt.isPresent()) {
            throw new RuntimeException("快照不存在: " + snapshotId);
        }
        
        ConfigSnapshot snapshot = snapshotOpt.get();
        List<ConfigSnapshotItem> items = getSnapshotItems(snapshotId);
        
        // 获取当前环境的配置项
        List<ConfigItem> currentConfigs = configItemRepository.findByAppIdAndEnvIdAndStatusOrderByConfigKey(
            snapshot.getAppId(), snapshot.getEnvId(), 1);
        
        // 创建配置项映射
        Map<String, ConfigItem> currentConfigMap = currentConfigs.stream()
            .collect(Collectors.toMap(ConfigItem::getConfigKey, item -> item));
        
        // 应用快照配置
        for (ConfigSnapshotItem snapshotItem : items) {
            ConfigItem configItem = currentConfigMap.get(snapshotItem.getConfigKey());
            
            if (configItem == null) {
                // 创建新的配置项
                configItem = new ConfigItem();
                configItem.setAppId(snapshot.getAppId());
                configItem.setEnvId(snapshot.getEnvId());
                configItem.setConfigKey(snapshotItem.getConfigKey());
                configItem.setStatus(1);
            }
            
            // 更新配置项
            configItem.setConfigValue(snapshotItem.getConfigValue());
            configItem.setConfigType(snapshotItem.getConfigType());
            configItem.setIsEncrypted(snapshotItem.getIsEncrypted());
            configItem.setIsRequired(snapshotItem.getIsRequired());
            configItem.setDefaultValue(snapshotItem.getDefaultValue());
            configItem.setDescription(snapshotItem.getDescription());
            configItem.setGroupId(snapshotItem.getGroupId());
            
            configItemRepository.save(configItem);
        }
    }

    @Override
    public boolean validateSnapshotConfig(Long snapshotId) {
        try {
            Map<String, Object> configData = getSnapshotConfigData(snapshotId);
            // 这里可以添加配置验证逻辑
            return configData != null && !configData.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getSnapshotStatistics(Long appId, Long envId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 统计快照数量
        Long totalSnapshots = configSnapshotRepository.countByAppIdAndEnvId(appId, envId);
        statistics.put("totalSnapshots", totalSnapshots);
        
        // 统计暂存快照数量
        List<ConfigSnapshot> stagedSnapshots = configSnapshotRepository.findByAppIdAndEnvIdAndSnapshotTypeOrderByCreatedAtDesc(
            appId, envId, 1);
        statistics.put("stagedSnapshots", stagedSnapshots.size());
        
        // 统计发布快照数量
        List<ConfigSnapshot> publishedSnapshots = configSnapshotRepository.findByAppIdAndEnvIdAndSnapshotTypeOrderByCreatedAtDesc(
            appId, envId, 2);
        statistics.put("publishedSnapshots", publishedSnapshots.size());
        
        // 获取最新快照
        Optional<ConfigSnapshot> latestSnapshot = getLatestPublishedSnapshot(appId, envId);
        if (latestSnapshot.isPresent()) {
            statistics.put("latestVersion", latestSnapshot.get().getVersionNumber());
            statistics.put("latestSnapshotName", latestSnapshot.get().getSnapshotName());
        }
        
        return statistics;
    }

    /**
     * 保存快照配置项
     */
    private void saveSnapshotItems(Long snapshotId, Map<String, Object> configData) {
        int sortOrder = 0;
        List<ConfigSnapshotItem> items = new ArrayList<>();
        
        // 递归处理配置数据，提取所有配置项
        extractConfigItems(snapshotId, configData, "", sortOrder, items);
        
        // 批量保存配置项
        for (ConfigSnapshotItem item : items) {
            configSnapshotItemRepository.save(item);
        }
    }
    
    /**
     * 递归提取配置项
     */
    private int extractConfigItems(Long snapshotId, Map<String, Object> configData, String prefix, int sortOrder, List<ConfigSnapshotItem> items) {
        for (Map.Entry<String, Object> entry : configData.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
            
            if (value instanceof Map) {
                // 如果是嵌套的Map，递归处理
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                sortOrder = extractConfigItems(snapshotId, nestedMap, fullKey, sortOrder, items);
            } else {
                // 如果是叶子节点，创建配置项
                ConfigSnapshotItem item = new ConfigSnapshotItem();
                item.setSnapshotId(snapshotId);
                item.setConfigKey(fullKey);
                item.setConfigValue(value != null ? value.toString() : null);
                item.setConfigType(1); // 默认为字符串类型
                item.setSortOrder(sortOrder);
                
                items.add(item);
                sortOrder++;
            }
        }
        return sortOrder;
    }
}
