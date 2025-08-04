package com.bank.config.service;

import com.bank.config.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 角色Service接口
 * 
 * @author bank
 */
public interface RoleService {

    /**
     * 创建角色
     */
    Role createRole(Role role);

    /**
     * 更新角色
     */
    Role updateRole(Long id, Role role);

    /**
     * 删除角色
     */
    void deleteRole(Long id);

    /**
     * 根据ID查找角色
     */
    Optional<Role> findById(Long id);

    /**
     * 根据角色编码查找角色
     */
    Optional<Role> findByRoleCode(String roleCode);

    /**
     * 分页查询角色
     */
    Page<Role> findRoles(String keyword, Integer status, Pageable pageable);

    /**
     * 查找所有启用的角色
     */
    List<Role> findAllEnabled();

    /**
     * 检查角色编码是否存在
     */
    boolean existsByRoleCode(String roleCode);

    /**
     * 更新角色状态
     */
    Role updateStatus(Long id, Integer status);

    /**
     * 为用户分配角色
     */
    void assignRoleToUser(Long userId, Long roleId);

    /**
     * 撤销用户的角色
     */
    void revokeRoleFromUser(Long userId, Long roleId);

    /**
     * 获取用户的所有角色
     */
    List<Role> getUserRoles(Long userId);

    /**
     * 检查用户是否有指定角色
     */
    boolean hasRole(Long userId, String roleCode);
} 