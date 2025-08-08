package com.bank.config.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;

/**
 * 配置发布记录实体类
 * 
 * @author bank
 */
@Entity
@Table(name = "config_publish_records")
public class ConfigPublishRecord extends BaseEntity {

    @Column(name = "app_id", nullable = false)
    private Long appId;

    @Column(name = "env_id", nullable = false)
    private Long envId;

    @Column(name = "snapshot_id", nullable = false)
    private Long snapshotId;

    @Column(name = "version_number", nullable = false, length = 32)
    private String versionNumber;

    @Column(name = "publish_type", nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    private Integer publishType = 1; // 1-立即发布，2-定时发布，3-灰度发布

    @Column(name = "publish_status", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Integer publishStatus = 0; // 0-待发布，1-发布中，2-成功，3-失败

    @Column(name = "target_instances", columnDefinition = "TEXT")
    private String targetInstances; // JSON格式的目标实例列表

    @Column(name = "success_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer successCount = 0;

    @Column(name = "fail_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer failCount = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "published_by", nullable = false, length = 64)
    private String publishedBy;

    @Column(name = "published_at")
    private java.sql.Timestamp publishedAt;

    // 关联关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id", insertable = false, updatable = false)
    @JsonIgnore
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "env_id", insertable = false, updatable = false)
    @JsonIgnore
    private Environment environment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_id", insertable = false, updatable = false)
    @JsonIgnore
    private ConfigSnapshot configSnapshot;

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

    public Long getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(Long snapshotId) {
        this.snapshotId = snapshotId;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public Integer getPublishType() {
        return publishType;
    }

    public void setPublishType(Integer publishType) {
        this.publishType = publishType;
    }

    public Integer getPublishStatus() {
        return publishStatus;
    }

    public void setPublishStatus(Integer publishStatus) {
        this.publishStatus = publishStatus;
    }

    public String getTargetInstances() {
        return targetInstances;
    }

    public void setTargetInstances(String targetInstances) {
        this.targetInstances = targetInstances;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailCount() {
        return failCount;
    }

    public void setFailCount(Integer failCount) {
        this.failCount = failCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getPublishedBy() {
        return publishedBy;
    }

    public void setPublishedBy(String publishedBy) {
        this.publishedBy = publishedBy;
    }

    public java.sql.Timestamp getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(java.sql.Timestamp publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public ConfigSnapshot getConfigSnapshot() {
        return configSnapshot;
    }

    public void setConfigSnapshot(ConfigSnapshot configSnapshot) {
        this.configSnapshot = configSnapshot;
    }
}
