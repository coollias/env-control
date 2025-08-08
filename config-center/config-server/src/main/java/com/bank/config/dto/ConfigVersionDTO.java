package com.bank.config.dto;

import java.time.LocalDateTime;

/**
 * 配置版本DTO
 * 
 * @author bank
 */
public class ConfigVersionDTO {
    
    private Long id;
    private Long appId;
    private Long envId;
    private String versionNumber;
    private String versionName;
    private String versionDesc;
    private Integer changeType;
    private String changeSummary;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public ConfigVersionDTO() {}
    
    public ConfigVersionDTO(Long id, Long appId, Long envId, String versionNumber, 
                          String versionName, String versionDesc, Integer changeType,
                          String changeSummary, String createdBy, 
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.appId = appId;
        this.envId = envId;
        this.versionNumber = versionNumber;
        this.versionName = versionName;
        this.versionDesc = versionDesc;
        this.changeType = changeType;
        this.changeSummary = changeSummary;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getAppId() {
        return appId;
    }
    
    public void setAppId(Long appId) {
        this.appId = appId;
    }
    
    public Long getEnvId() {
        return envId;
    }
    
    public void setEnvId(Long envId) {
        this.envId = envId;
    }
    
    public String getVersionNumber() {
        return versionNumber;
    }
    
    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }
    
    public String getVersionName() {
        return versionName;
    }
    
    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
    
    public String getVersionDesc() {
        return versionDesc;
    }
    
    public void setVersionDesc(String versionDesc) {
        this.versionDesc = versionDesc;
    }
    
    public Integer getChangeType() {
        return changeType;
    }
    
    public void setChangeType(Integer changeType) {
        this.changeType = changeType;
    }
    
    public String getChangeSummary() {
        return changeSummary;
    }
    
    public void setChangeSummary(String changeSummary) {
        this.changeSummary = changeSummary;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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