package com.bank.config.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 应用实体类
 * 
 * @author bank
 */
@Entity
@Table(name = "applications")
public class Application extends BaseEntity {

    @Column(name = "app_code", nullable = false, unique = true, length = 64)
    private String appCode;

    @Column(name = "app_name", nullable = false, length = 128)
    private String appName;

    @Column(name = "app_desc", columnDefinition = "TEXT")
    private String appDesc;

    @Column(name = "owner", length = 64)
    private String owner;

    @Column(name = "contact_email", length = 128)
    private String contactEmail;

    @Column(name = "created_by", nullable = false)
    private Long createdBy; // 创建者用户ID

    @Column(name = "status", nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    private Integer status = 1; // 1-启用，0-禁用

    // 注意：数据库表中没有 last_login_at 字段，暂时注释掉
    // @Column(name = "last_login_at")
    // private LocalDateTime lastLoginAt;

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppDesc() {
        return appDesc;
    }

    public void setAppDesc(String appDesc) {
        this.appDesc = appDesc;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    // 注意：数据库表中没有 last_login_at 字段，暂时注释掉
    // public LocalDateTime getLastLoginAt() {
    //     return lastLoginAt;
    // }

    // public void setLastLoginAt(LocalDateTime lastLoginAt) {
    //     this.lastLoginAt = lastLoginAt;
    // }
} 