package com.bank.config.service;

import com.bank.config.entity.AppPermission;
import com.bank.config.entity.Permission;
import com.bank.config.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 权限Service接口
 * 
 * @author bank
 */
public interface PermissionService {

    // ==================== RBAC权限管理 ====================

    /**
     * 创建权限
     */
    Permission createPermission(Permission permission);

    /**
     * 更新权限
     */
    Permission updatePermission(Long id, Permission permission);

    /**
     * 删除权限
     */
    void deletePermission(Long id);

    /**
     * 根据ID查找权限
     */
    Optional<Permission> findById(Long id);

    /**
     * 根据权限编码查找权限
     */
    Optional<Permission> findByPermCode(String permCode);

    /**
     * 分页查询权限
     */
    Page<Permission> findPermissions(String keyword, Integer status, Pageable pageable);

    /**
     * 查找所有启用的权限
     */
    List<Permission> findAllEnabled();

    /**
     * 检查权限编码是否存在
     */
    boolean existsByPermCode(String permCode);

    /**
     * 更新权限状态
     */
    Permission updateStatus(Long id, Integer status);

    /**
     * 为角色分配权限
     */
    void assignPermissionToRole(Long roleId, Long permissionId);

    /**
     * 撤销角色的权限
     */
    void revokePermissionFromRole(Long roleId, Long permissionId);

    /**
     * 获取角色的所有权限
     */
    List<Permission> getRolePermissions(Long roleId);

    /**
     * 获取用户的所有权限（通过角色）
     */
    List<Permission> getUserPermissions(Long userId);

    /**
     * 检查用户是否有指定权限
     */
    boolean hasPermission(Long userId, String permCode);

    // ==================== 应用权限管理 ====================

    /**
     * 检查用户是否有应用权限
     */
    boolean hasAppPermission(Long userId, Long appId);

    /**
     * 检查用户是否有应用特定类型权限
     */
    boolean hasAppPermissionType(Long userId, Long appId, String permissionType);

    /**
     * 获取用户的应用权限列表
     */
    List<AppPermission> getUserAppPermissions(Long userId);

    /**
     * 获取应用的用户权限列表
     */
    List<AppPermission> getAppUserPermissions(Long appId);

    /**
     * 获取所有应用权限列表
     */
    List<AppPermission> getAllAppPermissions();

    /**
     * 获取所有应用权限列表（包含用户名和应用名称）
     */
    List<com.bank.config.dto.AppPermissionDTO> getAllAppPermissionsWithDetails();

    /**
     * 为用户分配应用权限
     */
    void assignAppPermission(Long userId, Long appId, String permissionType);

    /**
     * 撤销用户的应用权限
     */
    void revokeAppPermission(Long userId, Long appId);

    /**
     * 获取用户有权限的应用ID列表
     */
    List<Long> getUserAppIds(Long userId);

    // ==================== 综合权限检查 ====================

    /**
     * 检查用户是否有系统权限（RBAC）
     */
    boolean hasSystemPermission(Long userId, String permCode);

    /**
     * 检查用户是否有应用数据权限
     */
    boolean hasAppDataPermission(Long userId, Long appId, String action);

    /**
     * 获取用户的所有权限信息
     */
    UserPermissions getUserAllPermissions(Long userId);

    /**
     * 用户权限信息类
     */
    class UserPermissions {
        private List<Permission> systemPermissions;
        private List<AppPermission> appPermissions;
        private List<Role> roles;

        // 构造函数、getter和setter
        public UserPermissions() {}

        public UserPermissions(List<Permission> systemPermissions, List<AppPermission> appPermissions, List<Role> roles) {
            this.systemPermissions = systemPermissions;
            this.appPermissions = appPermissions;
            this.roles = roles;
        }

        public List<Permission> getSystemPermissions() {
            return systemPermissions;
        }

        public void setSystemPermissions(List<Permission> systemPermissions) {
            this.systemPermissions = systemPermissions;
        }

        public List<AppPermission> getAppPermissions() {
            return appPermissions;
        }

        public void setAppPermissions(List<AppPermission> appPermissions) {
            this.appPermissions = appPermissions;
        }

        public List<Role> getRoles() {
            return roles;
        }

        public void setRoles(List<Role> roles) {
            this.roles = roles;
        }
    }
} 