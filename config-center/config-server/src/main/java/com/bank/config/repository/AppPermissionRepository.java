package com.bank.config.repository;

import com.bank.config.entity.AppPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 应用权限Repository
 * 
 * @author bank
 */
@Repository
public interface AppPermissionRepository extends JpaRepository<AppPermission, Long> {

    /**
     * 根据用户ID和应用ID查找权限
     */
    Optional<AppPermission> findByUserIdAndAppIdAndStatus(Long userId, Long appId, Integer status);

    /**
     * 根据用户ID查找所有权限
     */
    List<AppPermission> findByUserIdAndStatus(Long userId, Integer status);

    /**
     * 根据用户ID和权限类型查找权限
     */
    List<AppPermission> findByUserIdAndPermissionTypeAndStatus(Long userId, String permissionType, Integer status);

    /**
     * 根据应用ID查找所有权限
     */
    List<AppPermission> findByAppIdAndStatus(Long appId, Integer status);

    /**
     * 检查用户是否有指定应用的权限
     */
    @Query("SELECT COUNT(ap) > 0 FROM AppPermission ap WHERE ap.userId = :userId AND ap.appId = :appId AND ap.status = 1")
    boolean hasPermission(@Param("userId") Long userId, @Param("appId") Long appId);

    /**
     * 检查用户是否有指定应用的指定权限类型
     */
    @Query("SELECT COUNT(ap) > 0 FROM AppPermission ap WHERE ap.userId = :userId AND ap.appId = :appId AND ap.permissionType = :permissionType AND ap.status = 1")
    boolean hasPermissionType(@Param("userId") Long userId, @Param("appId") Long appId, @Param("permissionType") String permissionType);

    /**
     * 获取用户有权限的应用ID列表
     */
    @Query("SELECT DISTINCT ap.appId FROM AppPermission ap WHERE ap.userId = :userId AND ap.status = 1")
    List<Long> findUserAppIds(@Param("userId") Long userId);
} 