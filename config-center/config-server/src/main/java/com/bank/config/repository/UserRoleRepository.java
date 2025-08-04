package com.bank.config.repository;

import com.bank.config.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户角色Repository
 * 
 * @author bank
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    /**
     * 根据用户ID查找用户角色关联
     */
    List<UserRole> findByUserId(Long userId);

    /**
     * 根据角色ID查找用户角色关联
     */
    List<UserRole> findByRoleId(Long roleId);

    /**
     * 根据用户ID和角色ID查找
     */
    UserRole findByUserIdAndRoleId(Long userId, Long roleId);

    /**
     * 根据用户ID查找角色ID列表
     */
    @Query("SELECT ur.roleId FROM UserRole ur WHERE ur.userId = :userId")
    List<Long> findRoleIdsByUserId(@Param("userId") Long userId);

    /**
     * 删除用户的所有角色
     */
    void deleteByUserId(Long userId);

    /**
     * 删除角色的所有用户
     */
    void deleteByRoleId(Long roleId);
} 