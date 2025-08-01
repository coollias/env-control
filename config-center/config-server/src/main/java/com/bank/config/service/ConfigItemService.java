package com.bank.config.service;

import com.bank.config.entity.ConfigItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 配置项Service接口
 * 
 * @author bank
 */
public interface ConfigItemService {

    /**
     * 创建配置项
     */
    ConfigItem createConfigItem(ConfigItem configItem);

    /**
     * 更新配置项
     */
    ConfigItem updateConfigItem(Long id, ConfigItem configItem);

    /**
     * 删除配置项
     */
    void deleteConfigItem(Long id);

    /**
     * 根据ID查找配置项
     */
    Optional<ConfigItem> findById(Long id);

    /**
     * 根据应用ID、环境ID和配置键查找配置项
     */
    Optional<ConfigItem> findByAppIdAndEnvIdAndConfigKey(Long appId, Long envId, String configKey);

    /**
     * 根据应用ID和环境ID查找配置项列表
     */
    List<ConfigItem> findByAppIdAndEnvId(Long appId, Long envId);

    /**
     * 分页查询配置项
     */
    Page<ConfigItem> findConfigItems(Long appId, Long envId, String keyword, Integer status, Pageable pageable);

    /**
     * 根据配置组ID查找配置项
     */
    List<ConfigItem> findByAppIdAndEnvIdAndGroupId(Long appId, Long envId, Long groupId);

    /**
     * 批量创建配置项
     */
    List<ConfigItem> batchCreateConfigItems(List<ConfigItem> configItems);

    /**
     * 批量更新配置项
     */
    List<ConfigItem> batchUpdateConfigItems(List<ConfigItem> configItems);

    /**
     * 启用/禁用配置项
     */
    ConfigItem updateStatus(Long id, Integer status);

    /**
     * 获取配置项数量
     */
    long countByAppIdAndEnvId(Long appId, Long envId);
} 