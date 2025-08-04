package com.bank.config.entity;

import javax.persistence.*;

/**
 * 权限实体类
 * 
 * @author bank
 */
@Entity
@Table(name = "permissions")
public class Permission extends BaseEntity {

    @Column(name = "perm_code", nullable = false, unique = true, length = 64)
    private String permCode;

    @Column(name = "perm_name", nullable = false, length = 128)
    private String permName;

    @Column(name = "perm_desc", columnDefinition = "TEXT")
    private String permDesc;

    @Column(name = "resource_type", length = 32)
    private String resourceType;

    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "status", nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    private Integer status = 1; // 1-启用，0-禁用

    public String getPermCode() {
        return permCode;
    }

    public void setPermCode(String permCode) {
        this.permCode = permCode;
    }

    public String getPermName() {
        return permName;
    }

    public void setPermName(String permName) {
        this.permName = permName;
    }

    public String getPermDesc() {
        return permDesc;
    }

    public void setPermDesc(String permDesc) {
        this.permDesc = permDesc;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
} 