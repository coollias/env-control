package com.bank.config.service.impl;

import com.bank.config.dto.AppPermissionDTO;
import com.bank.config.entity.AppPermission;
import com.bank.config.entity.Application;
import com.bank.config.entity.Permission;
import com.bank.config.entity.Role;
import com.bank.config.entity.RolePermission;
import com.bank.config.entity.User;
import com.bank.config.repository.AppPermissionRepository;
import com.bank.config.repository.ApplicationRepository;
import com.bank.config.repository.PermissionRepository;
import com.bank.config.repository.RolePermissionRepository;
import com.bank.config.repository.UserRepository;
import com.bank.config.service.PermissionService;
import com.bank.config.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 权限服务实现类
 * 
 * @author bank
 */
@Service
@Transactional
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private AppPermissionRepository appPermissionRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    // ==================== RBAC权限管理 ====================

    @Override
    public Permission createPermission(Permission permission) {
        // 检查权限编码是否已存在
        if (existsByPermCode(permission.getPermCode())) {
            throw new RuntimeException("权限编码已存在: " + permission.getPermCode());
        }
        
        // 设置默认状态
        if (permission.getStatus() == null) {
            permission.setStatus(1);
        }
        
        return permissionRepository.save(permission);
    }

    @Override
    public Permission updatePermission(Long id, Permission permission) {
        Optional<Permission> existingPermission = permissionRepository.findById(id);
        if (!existingPermission.isPresent()) {
            throw new RuntimeException("权限不存在: " + id);
        }
        
        Permission existing = existingPermission.get();
        
        // 检查权限编码是否与其他权限冲突
        if (!existing.getPermCode().equals(permission.getPermCode()) && 
            existsByPermCode(permission.getPermCode())) {
            throw new RuntimeException("权限编码已存在: " + permission.getPermCode());
        }
        
        // 更新字段
        existing.setPermCode(permission.getPermCode());
        existing.setPermName(permission.getPermName());
        existing.setPermDesc(permission.getPermDesc());
        existing.setResourceType(permission.getResourceType());
        existing.setResourceId(permission.getResourceId());
        
        return permissionRepository.save(existing);
    }

    @Override
    public void deletePermission(Long id) {
        if (!permissionRepository.existsById(id)) {
            throw new RuntimeException("权限不存在: " + id);
        }
        permissionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Permission> findById(Long id) {
        return permissionRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Permission> findByPermCode(String permCode) {
        return permissionRepository.findByPermCode(permCode);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Permission> findPermissions(String keyword, Integer status, Pageable pageable) {
        // 这里需要根据实际的Repository方法来实现
        // 暂时返回所有权限
        return permissionRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permission> findAllEnabled() {
        return permissionRepository.findByStatusOrderByCreatedAtDesc(1);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPermCode(String permCode) {
        return permissionRepository.existsByPermCode(permCode);
    }

    @Override
    public Permission updateStatus(Long id, Integer status) {
        Optional<Permission> permissionOpt = permissionRepository.findById(id);
        if (!permissionOpt.isPresent()) {
            throw new RuntimeException("权限不存在: " + id);
        }
        
        Permission permission = permissionOpt.get();
        permission.setStatus(status);
        return permissionRepository.save(permission);
    }

    @Override
    public void assignPermissionToRole(Long roleId, Long permissionId) {
        // 检查是否已经分配
        RolePermission existing = rolePermissionRepository.findByRoleIdAndPermissionId(roleId, permissionId);
        if (existing != null) {
            throw new RuntimeException("角色已拥有此权限");
        }
        
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(roleId);
        rolePermission.setPermissionId(permissionId);
        rolePermissionRepository.save(rolePermission);
    }

    @Override
    public void revokePermissionFromRole(Long roleId, Long permissionId) {
        RolePermission rolePermission = rolePermissionRepository.findByRoleIdAndPermissionId(roleId, permissionId);
        if (rolePermission != null) {
            rolePermissionRepository.delete(rolePermission);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permission> getRolePermissions(Long roleId) {
        return permissionRepository.findByRoleId(roleId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permission> getUserPermissions(Long userId) {
        return permissionRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, String permCode) {
        List<Permission> userPermissions = getUserPermissions(userId);
        return userPermissions.stream()
                .anyMatch(permission -> permission.getPermCode().equals(permCode) && permission.getStatus() == 1);
    }

    // ==================== 应用权限管理 ====================

    @Override
    @Transactional(readOnly = true)
    public boolean hasAppPermission(Long userId, Long appId) {
        return appPermissionRepository.hasPermission(userId, appId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAppPermissionType(Long userId, Long appId, String permissionType) {
        return appPermissionRepository.hasPermissionType(userId, appId, permissionType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppPermission> getUserAppPermissions(Long userId) {
        return appPermissionRepository.findByUserIdAndStatus(userId, 1);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppPermission> getAppUserPermissions(Long appId) {
        return appPermissionRepository.findByAppIdAndStatus(appId, 1);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppPermission> getAllAppPermissions() {
        return appPermissionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppPermissionDTO> getAllAppPermissionsWithDetails() {
        List<AppPermission> permissions = appPermissionRepository.findAll();
        return permissions.stream().map(permission -> {
            AppPermissionDTO dto = new AppPermissionDTO(permission);
            
            // 获取用户名
            Optional<User> user = userRepository.findById(permission.getUserId());
            dto.setUserName(user.map(User::getUsername).orElse("未知用户"));
            
            // 获取应用名称
            Optional<Application> app = applicationRepository.findById(permission.getAppId());
            dto.setAppName(app.map(Application::getAppName).orElse("未知应用"));
            
            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void assignAppPermission(Long userId, Long appId, String permissionType) {
        // 检查是否已存在权限
        AppPermission existingPermission = appPermissionRepository
            .findByUserIdAndAppIdAndStatus(userId, appId, 1)
            .orElse(null);

        if (existingPermission != null) {
            // 更新现有权限
            existingPermission.setPermissionType(permissionType);
            appPermissionRepository.save(existingPermission);
        } else {
            // 创建新权限
            AppPermission permission = new AppPermission();
            permission.setUserId(userId);
            permission.setAppId(appId);
            permission.setPermissionType(permissionType);
            permission.setStatus(1);
            appPermissionRepository.save(permission);
        }
    }

    @Override
    public void revokeAppPermission(Long userId, Long appId) {
        AppPermission permission = appPermissionRepository
            .findByUserIdAndAppIdAndStatus(userId, appId, 1)
            .orElse(null);

        if (permission != null) {
            permission.setStatus(0);
            appPermissionRepository.save(permission);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getUserAppIds(Long userId) {
        return appPermissionRepository.findUserAppIds(userId);
    }

    // ==================== 综合权限检查 ====================

    @Override
    @Transactional(readOnly = true)
    public boolean hasSystemPermission(Long userId, String permCode) {
        return hasPermission(userId, permCode);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAppDataPermission(Long userId, Long appId, String action) {
        // 首先检查系统权限
        if (!hasSystemPermission(userId, "app:" + action)) {
            return false;
        }
        
        // 然后检查应用权限
        switch (action) {
            case "read":
                return hasAppPermission(userId, appId);
            case "write":
                return hasAppPermissionType(userId, appId, "WRITE") || 
                       hasAppPermissionType(userId, appId, "ADMIN");
            case "admin":
                return hasAppPermissionType(userId, appId, "ADMIN");
            default:
                return hasAppPermission(userId, appId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserPermissions getUserAllPermissions(Long userId) {
        List<Permission> systemPermissions = getUserPermissions(userId);
        List<AppPermission> appPermissions = getUserAppPermissions(userId);
        List<Role> roles = roleService.getUserRoles(userId);
        
        return new UserPermissions(systemPermissions, appPermissions, roles);
    }
} 