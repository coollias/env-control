package com.bank.config.service;

import com.bank.config.entity.ConfigSnapshot;
import com.bank.config.entity.ConfigSnapshotItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 配置快照Service接口
 * 
 * @author bank
 */
public interface ConfigSnapshotService {

    /**
     * 创建配置快照（暂存功能）
     */
    ConfigSnapshot createSnapshot(Long appId, Long envId, String snapshotName, String snapshotDesc, 
                                Map<String, Object> configData, String createdBy);

    /**
     * 发布配置快照
     */
    ConfigSnapshot publishSnapshot(Long snapshotId, String publishedBy);

    /**
     * 根据ID查找快照
     */
    Optional<ConfigSnapshot> findById(Long id);

    /**
     * 根据应用ID、环境ID和版本号查找快照
     */
    Optional<ConfigSnapshot> findByAppIdAndEnvIdAndVersionNumber(Long appId, Long envId, String versionNumber);

    /**
     * 根据应用ID和环境ID查找快照列表
     */
    List<ConfigSnapshot> findByAppIdAndEnvId(Long appId, Long envId);

    /**
     * 根据应用ID和环境ID分页查找快照列表
     */
    Page<ConfigSnapshot> findByAppIdAndEnvId(Long appId, Long envId, Pageable pageable);

    /**
     * 删除快照
     */
    void deleteSnapshot(Long id);

    /**
     * 获取最新版本号
     */
    String getLatestVersionNumber(Long appId, Long envId);

    /**
     * 生成新版本号
     */
    String generateVersionNumber(Long appId, Long envId);

    /**
     * 获取快照的配置项列表
     */
    List<ConfigSnapshotItem> getSnapshotItems(Long snapshotId);

    /**
     * 获取快照的配置数据（JSON格式）
     */
    Map<String, Object> getSnapshotConfigData(Long snapshotId);

    /**
     * 比较两个快照的差异
     */
    Map<String, Object> compareSnapshots(Long snapshotId1, Long snapshotId2);

    /**
     * 回滚到指定快照
     */
    ConfigSnapshot rollbackToSnapshot(Long appId, Long envId, Long targetSnapshotId, String createdBy);

    /**
     * 获取应用环境的最新发布快照
     */
    Optional<ConfigSnapshot> getLatestPublishedSnapshot(Long appId, Long envId);

    /**
     * 获取应用环境的最新暂存快照
     */
    Optional<ConfigSnapshot> getLatestStagedSnapshot(Long appId, Long envId);

    /**
     * 将快照转换为配置项并应用到环境
     */
    void applySnapshotToEnvironment(Long snapshotId, String appliedBy);

    /**
     * 验证快照配置的有效性
     */
    boolean validateSnapshotConfig(Long snapshotId);

    /**
     * 获取快照统计信息
     */
    Map<String, Object> getSnapshotStatistics(Long appId, Long envId);
}
