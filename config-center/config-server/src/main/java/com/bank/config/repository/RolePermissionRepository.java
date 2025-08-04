package com.bank.config.repository;

import com.bank.config.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色权限Repository
 * 
 * @author bank
 */
@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    /**
     * 根据角色ID查找角色权限关联
     */
    List<RolePermission> findByRoleId(Long roleId);

    /**
     * 根据权限ID查找角色权限关联
     */
    List<RolePermission> findByPermissionId(Long permissionId);

    /**
     * 根据角色ID和权限ID查找
     */
    RolePermission findByRoleIdAndPermissionId(Long roleId, Long permissionId);

    /**
     * 根据角色ID查找权限ID列表
     */
    @Query("SELECT rp.permissionId FROM RolePermission rp WHERE rp.roleId = :roleId")
    List<Long> findPermissionIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除角色的所有权限
     */
    void deleteByRoleId(Long roleId);

    /**
     * 删除权限的所有角色
     */
    void deleteByPermissionId(Long permissionId);
} 