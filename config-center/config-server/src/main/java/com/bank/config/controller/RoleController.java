package com.bank.config.controller;

import com.bank.config.common.ApiResponse;
import com.bank.config.entity.Role;
import com.bank.config.service.RoleService;
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
 * 角色管理Controller
 * 
 * @author bank
 */
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 创建角色
     */
    @PostMapping
    public ApiResponse<Role> createRole(@Valid @RequestBody Role role) {
        try {
            Role created = roleService.createRole(role);
            return ApiResponse.success("角色创建成功", created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    public ApiResponse<Role> updateRole(@PathVariable Long id, @Valid @RequestBody Role role) {
        try {
            Role updated = roleService.updateRole(id, role);
            return ApiResponse.success("角色更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        try {
            roleService.deleteRole(id);
            return ApiResponse.success("角色删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取角色
     */
    @GetMapping("/{id}")
    public ApiResponse<Role> getRole(@PathVariable Long id) {
        Optional<Role> role = roleService.findById(id);
        if (role.isPresent()) {
            return ApiResponse.success(role.get());
        } else {
            return ApiResponse.error(404, "角色不存在");
        }
    }

    /**
     * 根据角色编码获取角色
     */
    @GetMapping("/code/{roleCode}")
    public ApiResponse<Role> getRoleByCode(@PathVariable String roleCode) {
        Optional<Role> role = roleService.findByRoleCode(roleCode);
        if (role.isPresent()) {
            return ApiResponse.success(role.get());
        } else {
            return ApiResponse.error(404, "角色不存在");
        }
    }

    /**
     * 分页查询角色
     */
    @GetMapping
    public ApiResponse<Page<Role>> getRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer status) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Role> roles = roleService.findRoles(keyword, status, pageable);
            return ApiResponse.success(roles);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取所有启用的角色
     */
    @GetMapping("/enabled")
    public ApiResponse<List<Role>> getEnabledRoles() {
        try {
            List<Role> roles = roleService.findAllEnabled();
            return ApiResponse.success(roles);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新角色状态
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Role> updateRoleStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            Role role = roleService.updateStatus(id, status);
            return ApiResponse.success("角色状态更新成功", role);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 检查角色编码是否存在
     */
    @GetMapping("/check-code/{roleCode}")
    public ApiResponse<Boolean> checkRoleCodeExists(@PathVariable String roleCode) {
        boolean exists = roleService.existsByRoleCode(roleCode);
        return ApiResponse.success(exists);
    }

    /**
     * 为用户分配角色
     */
    @PostMapping("/assign")
    public ApiResponse<Void> assignRoleToUser(@RequestParam Long userId, @RequestParam Long roleId) {
        try {
            roleService.assignRoleToUser(userId, roleId);
            return ApiResponse.success("角色分配成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 撤销用户的角色
     */
    @DeleteMapping("/revoke")
    public ApiResponse<Void> revokeRoleFromUser(@RequestParam Long userId, @RequestParam Long roleId) {
        try {
            roleService.revokeRoleFromUser(userId, roleId);
            return ApiResponse.success("角色撤销成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取用户的所有角色
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<Role>> getUserRoles(@PathVariable Long userId) {
        try {
            List<Role> roles = roleService.getUserRoles(userId);
            return ApiResponse.success(roles);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 检查用户是否有指定角色
     */
    @GetMapping("/check")
    public ApiResponse<Boolean> checkUserRole(@RequestParam Long userId, @RequestParam String roleCode) {
        try {
            boolean hasRole = roleService.hasRole(userId, roleCode);
            return ApiResponse.success(hasRole);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
} 