package com.bank.config.repository;

import com.bank.config.entity.ConfigChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 配置变更Repository
 * 
 * @author bank
 */
@Repository
public interface ConfigChangeRepository extends JpaRepository<ConfigChange, Long> {

    /**
     * 根据版本ID查找变更列表
     */
    List<ConfigChange> findByVersionIdOrderByConfigKey(Long versionId);

    /**
     * 根据版本ID和变更类型查找变更列表
     */
    List<ConfigChange> findByVersionIdAndChangeTypeOrderByConfigKey(Long versionId, Integer changeType);

    /**
     * 根据配置键查找变更历史
     */
    List<ConfigChange> findByConfigKeyOrderByCreatedAtDesc(String configKey);

    /**
     * 根据版本ID统计变更数量
     */
    long countByVersionId(Long versionId);

    /**
     * 根据版本ID和变更类型统计变更数量
     */
    long countByVersionIdAndChangeType(Long versionId, Integer changeType);
} 