package com.bank.config.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户角色关联实体类
 * 
 * @author bank
 */
@Entity
@Table(name = "user_roles")
public class UserRole extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
} 