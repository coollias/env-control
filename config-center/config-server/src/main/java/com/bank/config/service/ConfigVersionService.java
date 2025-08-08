package com.bank.config.service;

import com.bank.config.dto.ConfigVersionDTO;
import com.bank.config.entity.ConfigVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 配置版本Service接口
 * 
 * @author bank
 */
public interface ConfigVersionService {

    /**
     * 创建配置版本
     */
    ConfigVersion createVersion(ConfigVersion version);

    /**
     * 根据ID查找版本
     */
    Optional<ConfigVersion> findById(Long id);

    /**
     * 根据应用ID、环境ID和版本号查找版本
     */
    Optional<ConfigVersion> findByAppIdAndEnvIdAndVersionNumber(Long appId, Long envId, String versionNumber);

    /**
     * 根据应用ID和环境ID查找版本列表
     */
    List<ConfigVersion> findByAppIdAndEnvId(Long appId, Long envId);

    /**
     * 根据应用ID和环境ID查找版本DTO列表
     */
    List<ConfigVersionDTO> findDTOsByAppIdAndEnvId(Long appId, Long envId);

    /**
     * 根据应用ID和环境ID分页查找版本列表
     */
    Page<ConfigVersion> findByAppIdAndEnvId(Long appId, Long envId, Pageable pageable);

    /**
     * 删除版本
     */
    void deleteVersion(Long id);

    /**
     * 获取最新版本号
     */
    String getLatestVersionNumber(Long appId, Long envId);

    /**
     * 生成新版本号
     */
    String generateVersionNumber(Long appId, Long envId);

    /**
     * 创建版本并记录变更
     */
    ConfigVersion createVersionWithChanges(Long appId, Long envId, String versionName, 
                                        String versionDesc, String createdBy, 
                                        Map<String, String> changes, Integer changeType);

    /**
     * 回滚到指定版本
     */
    ConfigVersion rollbackToVersion(Long appId, Long envId, String targetVersionNumber, String createdBy);

    /**
     * 比较两个版本的差异
     */
    Map<String, Object> compareVersions(Long appId, Long envId, String version1, String version2);

    /**
     * 获取版本的变更详情
     */
    List<Map<String, Object>> getVersionChanges(Long versionId);

    /**
     * 获取配置项的变更历史
     */
    List<Map<String, Object>> getConfigChangeHistory(Long appId, Long envId, String configKey);
} 