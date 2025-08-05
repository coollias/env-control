package com.bank.config.service;

import com.bank.config.entity.ConfigItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
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

    /**
     * 统计所有配置项数量
     */
    long countAll();

    /**
     * 根据状态统计配置项数量
     */
    long countByStatus(Integer status);

    // ==================== 环境级配置覆盖功能 ====================

    /**
     * 获取应用在指定环境下的完整配置（包含继承的配置）
     * 根据环境的sort_order进行配置继承和覆盖
     */
    List<ConfigItem> getMergedConfigsForAppAndEnv(Long appId, Long envId);

    /**
     * 获取应用在指定环境下的配置映射（键值对形式）
     * 包含继承的配置，后排序的环境会覆盖前面环境的配置
     */
    Map<String, ConfigItem> getMergedConfigMapForAppAndEnv(Long appId, Long envId);

    /**
     * 获取指定配置键在环境继承链中的最终值
     */
    Optional<ConfigItem> getConfigWithInheritance(Long appId, Long envId, String configKey);

    /**
     * 获取环境继承链（按sort_order排序）
     */
    List<Long> getEnvironmentInheritanceChain(Long appId, Long targetEnvId);

    /**
     * 获取应用在所有环境下的配置差异
     */
    Map<String, Map<String, String>> getConfigDifferencesAcrossEnvironments(Long appId);

    // ==================== 配置项版本管理功能 ====================

    /**
     * 创建配置项并自动生成版本
     */
    ConfigItem createConfigItemWithVersion(ConfigItem configItem, String createdBy);

    /**
     * 更新配置项并自动生成版本
     */
    ConfigItem updateConfigItemWithVersion(Long id, ConfigItem configItem, String createdBy);

    /**
     * 获取配置项的版本历史
     */
    List<Map<String, Object>> getConfigItemVersionHistory(Long appId, Long envId, String configKey);

    /**
     * 回滚配置项到指定版本
     */
    ConfigItem rollbackConfigItemToVersion(Long appId, Long envId, String configKey, String targetVersionNumber, String createdBy);

    /**
     * 测试缓存方法
     */
    String testCache(Long id);

    /**
     * 获取所有配置项列表（用于下拉选择等）
     */
    List<ConfigItem> findAllConfigItems();
} 