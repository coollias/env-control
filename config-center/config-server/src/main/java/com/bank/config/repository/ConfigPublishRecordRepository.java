package com.bank.config.repository;

import com.bank.config.entity.ConfigPublishRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 配置发布记录Repository
 * 
 * @author bank
 */
@Repository
public interface ConfigPublishRecordRepository extends JpaRepository<ConfigPublishRecord, Long> {

    /**
     * 根据应用ID和环境ID查找发布记录列表，按创建时间倒序
     */
    List<ConfigPublishRecord> findByAppIdAndEnvIdOrderByCreatedAtDesc(Long appId, Long envId);

    /**
     * 根据应用ID和环境ID分页查找发布记录列表，按创建时间倒序
     */
    Page<ConfigPublishRecord> findByAppIdAndEnvIdOrderByCreatedAtDesc(Long appId, Long envId, Pageable pageable);

    /**
     * 根据快照ID查找发布记录
     */
    List<ConfigPublishRecord> findBySnapshotIdOrderByCreatedAtDesc(Long snapshotId);

    /**
     * 根据发布状态查找发布记录
     */
    List<ConfigPublishRecord> findByPublishStatusOrderByCreatedAtDesc(Integer publishStatus);

    /**
     * 根据应用ID、环境ID和发布状态查找发布记录
     */
    List<ConfigPublishRecord> findByAppIdAndEnvIdAndPublishStatusOrderByCreatedAtDesc(Long appId, Long envId, Integer publishStatus);

    /**
     * 根据版本号查找发布记录
     */
    List<ConfigPublishRecord> findByVersionNumberOrderByCreatedAtDesc(String versionNumber);

    /**
     * 根据应用ID和环境ID统计发布记录数量
     */
    @Query("SELECT COUNT(r) FROM ConfigPublishRecord r WHERE r.appId = :appId AND r.envId = :envId")
    Long countByAppIdAndEnvId(@Param("appId") Long appId, @Param("envId") Long envId);

    /**
     * 根据应用ID和环境ID统计成功发布记录数量
     */
    @Query("SELECT COUNT(r) FROM ConfigPublishRecord r WHERE r.appId = :appId AND r.envId = :envId AND r.publishStatus = 2")
    Long countSuccessfulPublishes(@Param("appId") Long appId, @Param("envId") Long envId);

    /**
     * 根据应用ID和环境ID统计失败发布记录数量
     */
    @Query("SELECT COUNT(r) FROM ConfigPublishRecord r WHERE r.appId = :appId AND r.envId = :envId AND r.publishStatus = 3")
    Long countFailedPublishes(@Param("appId") Long appId, @Param("envId") Long envId);
}
