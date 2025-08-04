package com.bank.config.service.impl;

import com.bank.config.entity.Role;
import com.bank.config.entity.UserRole;
import com.bank.config.repository.RoleRepository;
import com.bank.config.repository.UserRoleRepository;
import com.bank.config.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * 角色Service实现类
 * 
 * @author bank
 */
@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Override
    public Role createRole(Role role) {
        // 检查角色编码是否已存在
        if (existsByRoleCode(role.getRoleCode())) {
            throw new RuntimeException("角色编码已存在: " + role.getRoleCode());
        }
        
        // 设置默认状态
        if (role.getStatus() == null) {
            role.setStatus(1);
        }
        
        return roleRepository.save(role);
    }

    @Override
    public Role updateRole(Long id, Role role) {
        Optional<Role> existingRole = roleRepository.findById(id);
        if (!existingRole.isPresent()) {
            throw new RuntimeException("角色不存在: " + id);
        }
        
        Role existing = existingRole.get();
        
        // 检查角色编码是否与其他角色冲突
        if (!existing.getRoleCode().equals(role.getRoleCode()) && 
            existsByRoleCode(role.getRoleCode())) {
            throw new RuntimeException("角色编码已存在: " + role.getRoleCode());
        }
        
        // 更新字段
        existing.setRoleCode(role.getRoleCode());
        existing.setRoleName(role.getRoleName());
        existing.setRoleDesc(role.getRoleDesc());
        
        return roleRepository.save(existing);
    }

    @Override
    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new RuntimeException("角色不存在: " + id);
        }
        roleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findById(Long id) {
        return roleRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findByRoleCode(String roleCode) {
        return roleRepository.findByRoleCode(roleCode);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Role> findRoles(String keyword, Integer status, Pageable pageable) {
        // 这里需要根据实际的Repository方法来实现
        // 暂时返回所有角色
        return roleRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> findAllEnabled() {
        return roleRepository.findByStatusOrderByCreatedAtDesc(1);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByRoleCode(String roleCode) {
        return roleRepository.existsByRoleCode(roleCode);
    }

    @Override
    public Role updateStatus(Long id, Integer status) {
        Optional<Role> roleOpt = roleRepository.findById(id);
        if (!roleOpt.isPresent()) {
            throw new RuntimeException("角色不存在: " + id);
        }
        
        Role role = roleOpt.get();
        role.setStatus(status);
        return roleRepository.save(role);
    }

    @Override
    public void assignRoleToUser(Long userId, Long roleId) {
        // 检查是否已经分配
        UserRole existing = userRoleRepository.findByUserIdAndRoleId(userId, roleId);
        if (existing != null) {
            throw new RuntimeException("用户已拥有此角色");
        }
        
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRoleRepository.save(userRole);
    }

    @Override
    public void revokeRoleFromUser(Long userId, Long roleId) {
        UserRole userRole = userRoleRepository.findByUserIdAndRoleId(userId, roleId);
        if (userRole != null) {
            userRoleRepository.delete(userRole);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getUserRoles(Long userId) {
        List<Long> roleIds = userRoleRepository.findRoleIdsByUserId(userId);
        return roleRepository.findAllById(roleIds);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasRole(Long userId, String roleCode) {
        List<Role> userRoles = getUserRoles(userId);
        return userRoles.stream()
                .anyMatch(role -> role.getRoleCode().equals(roleCode) && role.getStatus() == 1);
    }
} 