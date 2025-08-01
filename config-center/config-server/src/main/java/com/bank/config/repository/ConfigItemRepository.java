package com.bank.config.repository;

import com.bank.config.entity.ConfigItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 配置项Repository
 * 
 * @author bank
 */
@Repository
public interface ConfigItemRepository extends JpaRepository<ConfigItem, Long> {

    /**
     * 根据应用ID、环境ID和配置键查找配置项
     */
    Optional<ConfigItem> findByAppIdAndEnvIdAndConfigKey(Long appId, Long envId, String configKey);

    /**
     * 根据应用ID和环境ID查找配置项列表
     */
    List<ConfigItem> findByAppIdAndEnvIdAndStatusOrderByConfigKey(Long appId, Long envId, Integer status);

    /**
     * 根据应用ID、环境ID和状态分页查询配置项
     */
    Page<ConfigItem> findByAppIdAndEnvIdAndStatusOrderByConfigKey(Long appId, Long envId, Integer status, Pageable pageable);

    /**
     * 根据应用ID、环境ID、配置组ID和状态查找配置项
     */
    List<ConfigItem> findByAppIdAndEnvIdAndGroupIdAndStatusOrderByConfigKey(Long appId, Long envId, Long groupId, Integer status);

    /**
     * 根据配置键模糊查询
     */
    @Query("SELECT c FROM ConfigItem c WHERE c.appId = :appId AND c.envId = :envId AND c.status = :status AND c.configKey LIKE %:keyword%")
    Page<ConfigItem> findByKeywordAndAppIdAndEnvIdAndStatus(@Param("keyword") String keyword, @Param("appId") Long appId, @Param("envId") Long envId, @Param("status") Integer status, Pageable pageable);

    /**
     * 检查配置键是否存在
     */
    boolean existsByAppIdAndEnvIdAndConfigKey(Long appId, Long envId, String configKey);

    /**
     * 根据应用ID查找配置项数量
     */
    long countByAppIdAndStatus(Long appId, Integer status);

    /**
     * 根据环境ID查找配置项数量
     */
    long countByEnvIdAndStatus(Long envId, Integer status);

    /**
     * 删除应用下的所有配置项
     */
    void deleteByAppId(Long appId);

    /**
     * 删除环境下的所有配置项
     */
    void deleteByEnvId(Long envId);
} 