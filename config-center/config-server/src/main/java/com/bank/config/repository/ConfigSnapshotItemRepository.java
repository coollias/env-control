package com.bank.config.repository;

import com.bank.config.entity.ConfigSnapshotItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 配置快照详情Repository
 * 
 * @author bank
 */
@Repository
public interface ConfigSnapshotItemRepository extends JpaRepository<ConfigSnapshotItem, Long> {

    /**
     * 根据快照ID查找配置项列表，按排序和配置键排序
     */
    List<ConfigSnapshotItem> findBySnapshotIdOrderBySortOrderAscConfigKeyAsc(Long snapshotId);

    /**
     * 根据快照ID和配置组ID查找配置项列表
     */
    List<ConfigSnapshotItem> findBySnapshotIdAndGroupIdOrderBySortOrderAscConfigKeyAsc(Long snapshotId, Long groupId);

    /**
     * 根据快照ID和配置键查找配置项
     */
    ConfigSnapshotItem findBySnapshotIdAndConfigKey(Long snapshotId, String configKey);

    /**
     * 根据快照ID统计配置项数量
     */
    @Query("SELECT COUNT(i) FROM ConfigSnapshotItem i WHERE i.snapshotId = :snapshotId")
    Long countBySnapshotId(@Param("snapshotId") Long snapshotId);

    /**
     * 根据快照ID删除所有配置项
     */
    void deleteBySnapshotId(Long snapshotId);

    /**
     * 根据快照ID和配置键删除配置项
     */
    void deleteBySnapshotIdAndConfigKey(Long snapshotId, String configKey);
}
