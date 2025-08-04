package com.bank.config.dto;

import com.bank.config.entity.AppPermission;
import java.time.LocalDateTime;

/**
 * 应用权限DTO
 * 包含用户名和应用名称信息
 */
public class AppPermissionDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long appId;
    private String appName;
    private String permissionType;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AppPermissionDTO() {}

    public AppPermissionDTO(AppPermission permission) {
        this.id = permission.getId();
        this.userId = permission.getUserId();
        this.appId = permission.getAppId();
        this.permissionType = permission.getPermissionType();
        this.status = permission.getStatus();
        this.createdAt = permission.getCreatedAt();
        this.updatedAt = permission.getUpdatedAt();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(String permissionType) {
        this.permissionType = permissionType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
} 