package com.bank.config.service.impl;

import com.bank.config.entity.ConfigItem;
import com.bank.config.repository.ConfigItemRepository;
import com.bank.config.service.ConfigItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

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
        
        return configItemRepository.save(configItem);
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
        
        return configItemRepository.save(existing);
    }

    @Override
    public void deleteConfigItem(Long id) {
        if (!configItemRepository.existsById(id)) {
            throw new RuntimeException("配置项不存在: " + id);
        }
        configItemRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConfigItem> findById(Long id) {
        return configItemRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConfigItem> findByAppIdAndEnvIdAndConfigKey(Long appId, Long envId, String configKey) {
        return configItemRepository.findByAppIdAndEnvIdAndConfigKey(appId, envId, configKey);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfigItem> findByAppIdAndEnvId(Long appId, Long envId) {
        return configItemRepository.findByAppIdAndEnvIdAndStatusOrderByConfigKey(appId, envId, 1);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConfigItem> findConfigItems(Long appId, Long envId, String keyword, Integer status, Pageable pageable) {
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
} 