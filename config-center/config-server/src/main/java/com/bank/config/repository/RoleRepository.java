package com.bank.config.repository;

import com.bank.config.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色Repository
 * 
 * @author bank
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * 根据角色编码查找角色
     */
    Optional<Role> findByRoleCode(String roleCode);

    /**
     * 根据状态查找角色列表
     */
    List<Role> findByStatusOrderByCreatedAtDesc(Integer status);

    /**
     * 检查角色编码是否存在
     */
    boolean existsByRoleCode(String roleCode);
} 