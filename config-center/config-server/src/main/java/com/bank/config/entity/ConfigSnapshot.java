package com.bank.config.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;

/**
 * 配置快照实体类
 * 
 * @author bank
 */
@Entity
@Table(name = "config_snapshots")
public class ConfigSnapshot extends BaseEntity {

    @Column(name = "app_id", nullable = false)
    private Long appId;

    @Column(name = "env_id", nullable = false)
    private Long envId;

    @Column(name = "snapshot_name", nullable = false, length = 128)
    private String snapshotName;

    @Column(name = "snapshot_desc", columnDefinition = "TEXT")
    private String snapshotDesc;

    @Column(name = "version_number", nullable = false, length = 32)
    private String versionNumber;

    @Column(name = "snapshot_type", nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    private Integer snapshotType = 1; // 1-暂存，2-发布

    @Column(name = "status", nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    private Integer status = 1; // 1-有效，0-无效

    @Column(name = "config_data", nullable = false, columnDefinition = "LONGTEXT")
    private String configData; // JSON格式的完整配置数据

    @Column(name = "config_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer configCount = 0;

    @Column(name = "created_by", nullable = false, length = 64)
    private String createdBy;

    // 关联关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id", insertable = false, updatable = false)
    @JsonIgnore
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "env_id", insertable = false, updatable = false)
    @JsonIgnore
    private Environment environment;

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

    public String getSnapshotName() {
        return snapshotName;
    }

    public void setSnapshotName(String snapshotName) {
        this.snapshotName = snapshotName;
    }

    public String getSnapshotDesc() {
        return snapshotDesc;
    }

    public void setSnapshotDesc(String snapshotDesc) {
        this.snapshotDesc = snapshotDesc;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public Integer getSnapshotType() {
        return snapshotType;
    }

    public void setSnapshotType(Integer snapshotType) {
        this.snapshotType = snapshotType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getConfigData() {
        return configData;
    }

    public void setConfigData(String configData) {
        this.configData = configData;
    }

    public Integer getConfigCount() {
        return configCount;
    }

    public void setConfigCount(Integer configCount) {
        this.configCount = configCount;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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
}
