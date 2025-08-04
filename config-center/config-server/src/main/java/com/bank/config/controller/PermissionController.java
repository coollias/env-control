package com.bank.config.controller;

import com.bank.config.common.ApiResponse;
import com.bank.config.common.UserContext;
import com.bank.config.entity.AppPermission;
import com.bank.config.entity.Permission;
import com.bank.config.service.ApplicationService;
import com.bank.config.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * 权限管理Controller
 * 
 * @author bank
 */
@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;
    
    @Autowired
    private ApplicationService applicationService;

    // ==================== RBAC权限管理 ====================

    /**
     * 创建权限
     */
    @PostMapping
    public ApiResponse<Permission> createPermission(@Valid @RequestBody Permission permission) {
        try {
            Permission created = permissionService.createPermission(permission);
            return ApiResponse.success("权限创建成功", created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新权限
     */
    @PutMapping("/{id}")
    public ApiResponse<Permission> updatePermission(@PathVariable Long id, @Valid @RequestBody Permission permission) {
        try {
            Permission updated = permissionService.updatePermission(id, permission);
            return ApiResponse.success("权限更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePermission(@PathVariable Long id) {
        try {
            permissionService.deletePermission(id);
            return ApiResponse.success("权限删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取权限
     */
    @GetMapping("/{id}")
    public ApiResponse<Permission> getPermission(@PathVariable Long id) {
        Optional<Permission> permission = permissionService.findById(id);
        if (permission.isPresent()) {
            return ApiResponse.success(permission.get());
        } else {
            return ApiResponse.error(404, "权限不存在");
        }
    }

    /**
     * 根据权限编码获取权限
     */
    @GetMapping("/code/{permCode}")
    public ApiResponse<Permission> getPermissionByCode(@PathVariable String permCode) {
        Optional<Permission> permission = permissionService.findByPermCode(permCode);
        if (permission.isPresent()) {
            return ApiResponse.success(permission.get());
        } else {
            return ApiResponse.error(404, "权限不存在");
        }
    }

    /**
     * 分页查询权限
     */
    @GetMapping
    public ApiResponse<Page<Permission>> getPermissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer status) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Permission> permissions = permissionService.findPermissions(keyword, status, pageable);
            return ApiResponse.success(permissions);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取所有启用的权限
     */
    @GetMapping("/enabled")
    public ApiResponse<List<Permission>> getEnabledPermissions() {
        try {
            List<Permission> permissions = permissionService.findAllEnabled();
            return ApiResponse.success(permissions);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新权限状态
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Permission> updatePermissionStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            Permission permission = permissionService.updateStatus(id, status);
            return ApiResponse.success("权限状态更新成功", permission);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 检查权限编码是否存在
     */
    @GetMapping("/check-code/{permCode}")
    public ApiResponse<Boolean> checkPermissionCodeExists(@PathVariable String permCode) {
        boolean exists = permissionService.existsByPermCode(permCode);
        return ApiResponse.success(exists);
    }

    /**
     * 为角色分配权限
     */
    @PostMapping("/assign-to-role")
    public ApiResponse<Void> assignPermissionToRole(@RequestParam Long roleId, @RequestParam Long permissionId) {
        try {
            permissionService.assignPermissionToRole(roleId, permissionId);
            return ApiResponse.success("权限分配成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 撤销角色的权限
     */
    @DeleteMapping("/revoke-from-role")
    public ApiResponse<Void> revokePermissionFromRole(@RequestParam Long roleId, @RequestParam Long permissionId) {
        try {
            permissionService.revokePermissionFromRole(roleId, permissionId);
            return ApiResponse.success("权限撤销成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取角色的所有权限
     */
    @GetMapping("/role/{roleId}")
    public ApiResponse<List<Permission>> getRolePermissions(@PathVariable Long roleId) {
        try {
            List<Permission> permissions = permissionService.getRolePermissions(roleId);
            return ApiResponse.success(permissions);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取用户的所有权限（通过角色）
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<Permission>> getUserPermissions(@PathVariable Long userId) {
        try {
            List<Permission> permissions = permissionService.getUserPermissions(userId);
            return ApiResponse.success(permissions);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 检查用户是否有指定权限
     */
    @GetMapping("/check")
    public ApiResponse<Boolean> checkUserPermission(@RequestParam Long userId, @RequestParam String permCode) {
        try {
            boolean hasPermission = permissionService.hasPermission(userId, permCode);
            return ApiResponse.success(hasPermission);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // ==================== 应用权限管理 ====================

    /**
     * 为用户分配应用权限
     */
    @PostMapping("/assign-app")
    public ApiResponse<Void> assignAppPermission(@RequestParam Long userId, 
                                               @RequestParam Long appId, 
                                               @RequestParam String permissionType) {
        try {
            Long currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                return ApiResponse.error(401, "用户未登录");
            }
            
            // 检查当前用户是否有权限分配该应用的权限
            // 1. 系统管理员可以分配任何应用的权限
            // 2. 应用管理员可以分配自己管理的应用权限
            boolean isSystemAdmin = permissionService.hasSystemPermission(currentUserId, "app:admin");
            boolean isAppAdmin = permissionService.hasAppPermissionType(currentUserId, appId, "ADMIN");
            boolean isAppCreator = applicationService.isAppCreator(currentUserId, appId);
            
            if (!isSystemAdmin && !isAppAdmin && !isAppCreator) {
                return ApiResponse.error(403, "没有权限分配此应用的权限");
            }
            
            permissionService.assignAppPermission(userId, appId, permissionType);
            return ApiResponse.success("应用权限分配成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 撤销用户的应用权限
     */
    @DeleteMapping("/revoke-app")
    public ApiResponse<Void> revokeAppPermission(@RequestParam Long userId, @RequestParam Long appId) {
        try {
            Long currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                return ApiResponse.error(401, "用户未登录");
            }
            
            // 检查当前用户是否有权限撤销该应用的权限
            // 1. 系统管理员可以撤销任何应用的权限
            // 2. 应用管理员可以撤销自己管理的应用权限
            boolean isSystemAdmin = permissionService.hasSystemPermission(currentUserId, "app:admin");
            boolean isAppAdmin = permissionService.hasAppPermissionType(currentUserId, appId, "ADMIN");
            boolean isAppCreator = applicationService.isAppCreator(currentUserId, appId);
            
            if (!isSystemAdmin && !isAppAdmin && !isAppCreator) {
                return ApiResponse.error(403, "没有权限撤销此应用的权限");
            }
            
            permissionService.revokeAppPermission(userId, appId);
            return ApiResponse.success("应用权限撤销成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取用户的应用权限列表
     */
    @GetMapping("/app/user/{userId}")
    public ApiResponse<List<AppPermission>> getUserAppPermissions(@PathVariable Long userId) {
        try {
            List<AppPermission> permissions = permissionService.getUserAppPermissions(userId);
            return ApiResponse.success(permissions);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取应用的用户权限列表
     */
    @GetMapping("/app/{appId}")
    public ApiResponse<List<AppPermission>> getAppUserPermissions(@PathVariable Long appId) {
        try {
            List<AppPermission> permissions = permissionService.getAppUserPermissions(appId);
            return ApiResponse.success(permissions);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 检查用户是否有应用权限
     */
    @GetMapping("/app/check")
    public ApiResponse<Boolean> checkAppPermission(@RequestParam Long userId, 
                                                 @RequestParam Long appId, 
                                                 @RequestParam(required = false) String permissionType) {
        try {
            boolean hasPermission;
            if (permissionType != null) {
                hasPermission = permissionService.hasAppPermissionType(userId, appId, permissionType);
            } else {
                hasPermission = permissionService.hasAppPermission(userId, appId);
            }
            return ApiResponse.success(hasPermission);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取用户的所有权限信息
     */
    @GetMapping("/user/{userId}/all")
    public ApiResponse<PermissionService.UserPermissions> getUserAllPermissions(@PathVariable Long userId) {
        try {
            PermissionService.UserPermissions permissions = permissionService.getUserAllPermissions(userId);
            return ApiResponse.success(permissions);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取所有应用权限列表（用于权限管理页面）
     */
    @GetMapping("/app/all")
    public ApiResponse<List<com.bank.config.dto.AppPermissionDTO>> getAllAppPermissions() {
        try {
            List<com.bank.config.dto.AppPermissionDTO> permissions = permissionService.getAllAppPermissionsWithDetails();
            return ApiResponse.success(permissions);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
} 