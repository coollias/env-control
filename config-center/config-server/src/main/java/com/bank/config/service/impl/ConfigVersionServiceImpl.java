package com.bank.config.service.impl;

import com.bank.config.entity.ConfigChange;
import com.bank.config.entity.ConfigItem;
import com.bank.config.entity.ConfigVersion;
import com.bank.config.repository.ConfigChangeRepository;
import com.bank.config.repository.ConfigItemRepository;
import com.bank.config.repository.ConfigVersionRepository;
import com.bank.config.service.ConfigItemService;
import com.bank.config.service.ConfigVersionService;
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
 * 配置版本Service实现类
 * 
 * @author bank
 */
@Service
@Transactional
public class ConfigVersionServiceImpl implements ConfigVersionService {

    @Autowired
    private ConfigVersionRepository configVersionRepository;

    @Autowired
    private ConfigChangeRepository configChangeRepository;

    @Autowired
    private ConfigItemRepository configItemRepository;



    @Override
    @org.springframework.cache.annotation.CacheEvict(value = "versions", allEntries = true)
    public ConfigVersion createVersion(ConfigVersion version) {
        // 检查版本号是否已存在
        if (existsByAppIdAndEnvIdAndVersionNumber(version.getAppId(), version.getEnvId(), version.getVersionNumber())) {
            throw new RuntimeException("版本号已存在: " + version.getVersionNumber());
        }
        
        return configVersionRepository.save(version);
    }

    @Override
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "versions", key = "#id")
    public Optional<ConfigVersion> findById(Long id) {
        return configVersionRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "versions", key = "'app:' + #appId + ':env:' + #envId + ':version:' + #versionNumber")
    public Optional<ConfigVersion> findByAppIdAndEnvIdAndVersionNumber(Long appId, Long envId, String versionNumber) {
        return configVersionRepository.findByAppIdAndEnvIdAndVersionNumber(appId, envId, versionNumber);
    }

    @Override
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "versions", key = "'app:' + #appId + ':env:' + #envId + ':list'")
    public List<ConfigVersion> findByAppIdAndEnvId(Long appId, Long envId) {
        return configVersionRepository.findByAppIdAndEnvIdOrderByCreatedAtDesc(appId, envId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConfigVersion> findByAppIdAndEnvId(Long appId, Long envId, Pageable pageable) {
        return configVersionRepository.findByAppIdAndEnvIdOrderByCreatedAtDesc(appId, envId, pageable);
    }

    @Override
    @org.springframework.cache.annotation.CacheEvict(value = "versions", allEntries = true)
    public void deleteVersion(Long id) {
        if (!configVersionRepository.existsById(id)) {
            throw new RuntimeException("版本不存在: " + id);
        }
        
        // 删除相关的变更记录
        List<ConfigChange> changes = configChangeRepository.findByVersionIdOrderByConfigKey(id);
        configChangeRepository.deleteAll(changes);
        
        // 删除版本
        configVersionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "versions", key = "'app:' + #appId + ':env:' + #envId + ':latest'")
    public String getLatestVersionNumber(Long appId, Long envId) {
        return configVersionRepository.findLatestVersionNumber(appId, envId);
    }

    @Override
    public String generateVersionNumber(Long appId, Long envId) {
        String latestVersion = getLatestVersionNumber(appId, envId);
        
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
    public ConfigVersion createVersionWithChanges(Long appId, Long envId, String versionName, 
                                               String versionDesc, String createdBy, 
                                               Map<String, String> changes, Integer changeType) {
        
        // 生成版本号
        String versionNumber = generateVersionNumber(appId, envId);
        
        // 创建版本
        ConfigVersion version = new ConfigVersion();
        version.setAppId(appId);
        version.setEnvId(envId);
        version.setVersionNumber(versionNumber);
        version.setVersionName(versionName);
        version.setVersionDesc(versionDesc);
        version.setChangeType(changeType);
        version.setCreatedBy(createdBy);
        
        // 生成变更摘要
        String changeSummary = generateChangeSummary(changes, changeType);
        version.setChangeSummary(changeSummary);
        
        // 保存版本
        version = configVersionRepository.save(version);
        
        // 记录变更详情
        for (Map.Entry<String, String> entry : changes.entrySet()) {
            ConfigChange change = new ConfigChange();
            change.setVersionId(version.getId());
            change.setConfigKey(entry.getKey());
            change.setNewValue(entry.getValue());
            change.setChangeType(changeType);
            
            configChangeRepository.save(change);
        }
        
        return version;
    }

    @Override
    public ConfigVersion rollbackToVersion(Long appId, Long envId, String targetVersionNumber, String createdBy) {
        // 查找目标版本
        Optional<ConfigVersion> targetVersion = findByAppIdAndEnvIdAndVersionNumber(appId, envId, targetVersionNumber);
        if (!targetVersion.isPresent()) {
            throw new RuntimeException("目标版本不存在: " + targetVersionNumber);
        }
        
        // 获取目标版本的配置
        List<ConfigChange> targetChanges = configChangeRepository.findByVersionIdOrderByConfigKey(targetVersion.get().getId());
        Map<String, String> targetConfigs = targetChanges.stream()
                .collect(Collectors.toMap(ConfigChange::getConfigKey, ConfigChange::getNewValue));
        
        // 获取当前配置
        List<ConfigItem> currentConfigs = configItemRepository.findByAppIdAndEnvIdAndStatusOrderByConfigKey(appId, envId, 1);
        Map<String, String> currentConfigMap = currentConfigs.stream()
                .collect(Collectors.toMap(ConfigItem::getConfigKey, ConfigItem::getConfigValue));
        
        // 计算差异
        Map<String, String> changes = new HashMap<>();
        for (Map.Entry<String, String> entry : targetConfigs.entrySet()) {
            String key = entry.getKey();
            String targetValue = entry.getValue();
            String currentValue = currentConfigMap.get(key);
            
            if (!Objects.equals(targetValue, currentValue)) {
                changes.put(key, targetValue);
            }
        }
        
        // 创建回滚版本
        String rollbackVersionName = "回滚到 " + targetVersionNumber;
        String rollbackVersionDesc = "回滚到版本 " + targetVersionNumber;
        
        return createVersionWithChanges(appId, envId, rollbackVersionName, rollbackVersionDesc, createdBy, changes, 2);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> compareVersions(Long appId, Long envId, String version1, String version2) {
        // 获取两个版本的变更记录
        Optional<ConfigVersion> v1 = findByAppIdAndEnvIdAndVersionNumber(appId, envId, version1);
        Optional<ConfigVersion> v2 = findByAppIdAndEnvIdAndVersionNumber(appId, envId, version2);
        
        if (!v1.isPresent() || !v2.isPresent()) {
            throw new RuntimeException("版本不存在");
        }
        
        List<ConfigChange> changes1 = configChangeRepository.findByVersionIdOrderByConfigKey(v1.get().getId());
        List<ConfigChange> changes2 = configChangeRepository.findByVersionIdOrderByConfigKey(v2.get().getId());
        
        Map<String, String> configs1 = changes1.stream()
                .collect(Collectors.toMap(ConfigChange::getConfigKey, ConfigChange::getNewValue));
        Map<String, String> configs2 = changes2.stream()
                .collect(Collectors.toMap(ConfigChange::getConfigKey, ConfigChange::getNewValue));
        
        // 计算差异
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(configs1.keySet());
        allKeys.addAll(configs2.keySet());
        
        Map<String, Object> differences = new HashMap<>();
        for (String key : allKeys) {
            String value1 = configs1.get(key);
            String value2 = configs2.get(key);
            
            if (!Objects.equals(value1, value2)) {
                Map<String, String> diff = new HashMap<>();
                diff.put("version1", value1);
                diff.put("version2", value2);
                differences.put(key, diff);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("version1", version1);
        result.put("version2", version2);
        result.put("differences", differences);
        
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getVersionChanges(Long versionId) {
        List<ConfigChange> changes = configChangeRepository.findByVersionIdOrderByConfigKey(versionId);
        
        return changes.stream().map(change -> {
            Map<String, Object> changeMap = new HashMap<>();
            changeMap.put("configKey", change.getConfigKey());
            changeMap.put("oldValue", change.getOldValue());
            changeMap.put("newValue", change.getNewValue());
            changeMap.put("changeType", change.getChangeType());
            changeMap.put("createdAt", change.getCreatedAt());
            return changeMap;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getConfigChangeHistory(Long appId, Long envId, String configKey) {
        // 获取所有版本
        List<ConfigVersion> versions = findByAppIdAndEnvId(appId, envId);
        
        List<Map<String, Object>> history = new ArrayList<>();
        
        for (ConfigVersion version : versions) {
            List<ConfigChange> changes = configChangeRepository.findByVersionIdAndChangeTypeOrderByConfigKey(version.getId(), null);
            
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
                    history.add(historyItem);
                }
            }
        }
        
        return history;
    }

    private boolean existsByAppIdAndEnvIdAndVersionNumber(Long appId, Long envId, String versionNumber) {
        return configVersionRepository.existsByAppIdAndEnvIdAndVersionNumber(appId, envId, versionNumber);
    }

    private String generateChangeSummary(Map<String, String> changes, Integer changeType) {
        String changeTypeName = getChangeTypeName(changeType);
        return String.format("%s了 %d 个配置项", changeTypeName, changes.size());
    }

    private String getChangeTypeName(Integer changeType) {
        switch (changeType) {
            case 1: return "新增";
            case 2: return "修改";
            case 3: return "删除";
            default: return "变更";
        }
    }
} 