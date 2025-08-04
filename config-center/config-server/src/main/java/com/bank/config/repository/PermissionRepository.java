package com.bank.config.repository;

import com.bank.config.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 权限Repository
 * 
 * @author bank
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * 根据权限编码查找权限
     */
    Optional<Permission> findByPermCode(String permCode);

    /**
     * 根据状态查找权限列表
     */
    List<Permission> findByStatusOrderByCreatedAtDesc(Integer status);

    /**
     * 根据资源类型和资源ID查找权限
     */
    List<Permission> findByResourceTypeAndResourceIdAndStatus(String resourceType, Long resourceId, Integer status);

    /**
     * 检查权限编码是否存在
     */
    boolean existsByPermCode(String permCode);

    /**
     * 根据角色ID查找权限列表
     */
    @Query("SELECT p FROM Permission p " +
           "JOIN RolePermission rp ON p.id = rp.permissionId " +
           "WHERE rp.roleId = :roleId AND p.status = 1")
    List<Permission> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID查找权限列表
     */
    @Query("SELECT DISTINCT p FROM Permission p " +
           "JOIN RolePermission rp ON p.id = rp.permissionId " +
           "JOIN UserRole ur ON rp.roleId = ur.roleId " +
           "WHERE ur.userId = :userId AND p.status = 1")
    List<Permission> findByUserId(@Param("userId") Long userId);
} 