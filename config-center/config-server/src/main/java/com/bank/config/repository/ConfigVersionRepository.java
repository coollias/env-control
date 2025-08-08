package com.bank.config.repository;

import com.bank.config.dto.ConfigVersionDTO;
import com.bank.config.entity.ConfigVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 配置版本Repository
 * 
 * @author bank
 */
@Repository
public interface ConfigVersionRepository extends JpaRepository<ConfigVersion, Long> {

    /**
     * 根据应用ID和环境ID查找版本列表，按创建时间倒序
     */
    List<ConfigVersion> findByAppIdAndEnvIdOrderByCreatedAtDesc(Long appId, Long envId);

    /**
     * 根据应用ID和环境ID查找版本DTO列表，按创建时间倒序
     */
    @Query("SELECT new com.bank.config.dto.ConfigVersionDTO(" +
           "v.id, v.appId, v.envId, v.versionNumber, v.versionName, v.versionDesc, " +
           "v.changeType, v.changeSummary, v.createdBy, v.createdAt, v.updatedAt) " +
           "FROM ConfigVersion v WHERE v.appId = :appId AND v.envId = :envId " +
           "ORDER BY v.createdAt DESC")
    List<ConfigVersionDTO> findDTOsByAppIdAndEnvIdOrderByCreatedAtDesc(@Param("appId") Long appId, @Param("envId") Long envId);

    /**
     * 根据应用ID和环境ID分页查找版本列表，按创建时间倒序
     */
    Page<ConfigVersion> findByAppIdAndEnvIdOrderByCreatedAtDesc(Long appId, Long envId, Pageable pageable);

    /**
     * 根据应用ID、环境ID和版本号查找版本
     */
    Optional<ConfigVersion> findByAppIdAndEnvIdAndVersionNumber(Long appId, Long envId, String versionNumber);

    /**
     * 检查版本号是否存在
     */
    boolean existsByAppIdAndEnvIdAndVersionNumber(Long appId, Long envId, String versionNumber);

    /**
     * 根据应用ID和环境ID获取最新版本号
     */
    @Query("SELECT MAX(v.versionNumber) FROM ConfigVersion v WHERE v.appId = :appId AND v.envId = :envId")
    String findLatestVersionNumber(@Param("appId") Long appId, @Param("envId") Long envId);

    /**
     * 根据应用ID和环境ID统计版本数量
     */
    long countByAppIdAndEnvId(Long appId, Long envId);

    /**
     * 根据创建人查找版本列表
     */
    List<ConfigVersion> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    /**
     * 根据变更类型查找版本列表
     */
    List<ConfigVersion> findByChangeTypeOrderByCreatedAtDesc(Integer changeType);
} 