package com.bank.config.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 角色实体类
 * 
 * @author bank
 */
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    @Column(name = "role_code", nullable = false, unique = true, length = 64)
    private String roleCode;

    @Column(name = "role_name", nullable = false, length = 128)
    private String roleName;

    @Column(name = "role_desc", columnDefinition = "TEXT")
    private String roleDesc;

    @Column(name = "status", nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    private Integer status = 1; // 1-启用，0-禁用

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleDesc() {
        return roleDesc;
    }

    public void setRoleDesc(String roleDesc) {
        this.roleDesc = roleDesc;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
} 