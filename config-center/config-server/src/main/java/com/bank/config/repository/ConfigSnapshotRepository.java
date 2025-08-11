package com.bank.config.repository;

import com.bank.config.entity.ConfigSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 配置快照Repository
 * 
 * @author bank
 */
@Repository
public interface ConfigSnapshotRepository extends JpaRepository<ConfigSnapshot, Long> {

    /**
     * 根据应用ID和环境ID查找快照列表，按创建时间倒序
     */
    List<ConfigSnapshot> findByAppIdAndEnvIdOrderByCreatedAtDesc(Long appId, Long envId);

    /**
     * 根据应用ID和环境ID分页查找快照列表，按创建时间倒序
     */
    Page<ConfigSnapshot> findByAppIdAndEnvIdOrderByCreatedAtDesc(Long appId, Long envId, Pageable pageable);

    /**
     * 根据应用ID、环境ID和版本号查找快照
     */
    Optional<ConfigSnapshot> findByAppIdAndEnvIdAndVersionNumber(Long appId, Long envId, String versionNumber);

    /**
     * 检查版本号是否存在
     */
    boolean existsByAppIdAndEnvIdAndVersionNumber(Long appId, Long envId, String versionNumber);

    /**
     * 根据应用ID、环境ID和快照类型查找快照列表
     */
    List<ConfigSnapshot> findByAppIdAndEnvIdAndSnapshotTypeOrderByCreatedAtDesc(Long appId, Long envId, Integer snapshotType);

    /**
     * 根据应用ID、环境ID和状态查找快照列表
     */
    List<ConfigSnapshot> findByAppIdAndEnvIdAndStatusOrderByCreatedAtDesc(Long appId, Long envId, Integer status);

    /**
     * 根据应用ID和环境ID获取最新版本号
     */
    @Query("SELECT MAX(s.versionNumber) FROM ConfigSnapshot s WHERE s.appId = :appId AND s.envId = :envId")
    String findLatestVersionNumber(@Param("appId") Long appId, @Param("envId") Long envId);

    /**
     * 根据应用ID和环境ID统计快照数量
     */
    @Query("SELECT COUNT(s) FROM ConfigSnapshot s WHERE s.appId = :appId AND s.envId = :envId")
    Long countByAppIdAndEnvId(@Param("appId") Long appId, @Param("envId") Long envId);

    /**
     * 根据应用ID和环境ID获取最新的发布快照
     */
    @Query("SELECT s FROM ConfigSnapshot s WHERE s.appId = :appId AND s.envId = :envId AND s.snapshotType = 2 AND s.status = 1 ORDER BY s.createdAt DESC")
    List<ConfigSnapshot> findLatestPublishedSnapshots(@Param("appId") Long appId, @Param("envId") Long envId, Pageable pageable);

    /**
     * 根据应用ID和环境ID获取最新的暂存快照
     */
    @Query("SELECT s FROM ConfigSnapshot s WHERE s.appId = :appId AND s.envId = :envId AND s.snapshotType = 1 AND s.status = 1 ORDER BY s.createdAt DESC")
    List<ConfigSnapshot> findLatestStagedSnapshots(@Param("appId") Long appId, @Param("envId") Long envId, Pageable pageable);

    /**
     * 根据应用ID、环境ID、版本号和快照类型查找快照
     */
    Optional<ConfigSnapshot> findByAppIdAndEnvIdAndVersionNumberAndSnapshotType(Long appId, Long envId, String versionNumber, Integer snapshotType);
}
