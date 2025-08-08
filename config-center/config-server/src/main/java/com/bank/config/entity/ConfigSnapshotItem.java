package com.bank.config.entity;

import javax.persistence.*;

/**
 * 配置快照详情实体类
 * 
 * @author bank
 */
@Entity
@Table(name = "config_snapshot_items")
public class ConfigSnapshotItem extends BaseEntity {

    @Column(name = "snapshot_id", nullable = false)
    private Long snapshotId;

    @Column(name = "config_key", nullable = false, length = 128)
    private String configKey;

    @Column(name = "config_value", columnDefinition = "LONGTEXT")
    private String configValue;

    @Column(name = "config_type", nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    private Integer configType = 1; // 1-字符串，2-数字，3-布尔，4-JSON，5-YAML，6-Properties

    @Column(name = "is_encrypted", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Integer isEncrypted = 0; // 1-是，0-否

    @Column(name = "is_required", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Integer isRequired = 0; // 1-是，0-否

    @Column(name = "default_value", columnDefinition = "LONGTEXT")
    private String defaultValue;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "group_id", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long groupId = 0L;

    @Column(name = "sort_order", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer sortOrder = 0;

    // 关联关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_id", insertable = false, updatable = false)
    private ConfigSnapshot configSnapshot;

    public Long getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(Long snapshotId) {
        this.snapshotId = snapshotId;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public Integer getConfigType() {
        return configType;
    }

    public void setConfigType(Integer configType) {
        this.configType = configType;
    }

    public Integer getIsEncrypted() {
        return isEncrypted;
    }

    public void setIsEncrypted(Integer isEncrypted) {
        this.isEncrypted = isEncrypted;
    }

    public Integer getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Integer isRequired) {
        this.isRequired = isRequired;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public ConfigSnapshot getConfigSnapshot() {
        return configSnapshot;
    }

    public void setConfigSnapshot(ConfigSnapshot configSnapshot) {
        this.configSnapshot = configSnapshot;
    }
}
