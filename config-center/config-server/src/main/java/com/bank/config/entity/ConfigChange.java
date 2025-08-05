package com.bank.config.entity;

import javax.persistence.*;

/**
 * 配置变更详情实体类
 * 
 * @author bank
 */
@Entity
@Table(name = "config_changes")
public class ConfigChange extends BaseEntity {

    @Column(name = "version_id", nullable = false)
    private Long versionId;

    @Column(name = "config_key", nullable = false, length = 128)
    private String configKey;

    @Column(name = "old_value", columnDefinition = "LONGTEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "LONGTEXT")
    private String newValue;

    @Column(name = "change_type", nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    private Integer changeType = 1; // 1-新增，2-修改，3-删除

    // 关联关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", insertable = false, updatable = false)
    private ConfigVersion configVersion;

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public Integer getChangeType() {
        return changeType;
    }

    public void setChangeType(Integer changeType) {
        this.changeType = changeType;
    }

    public ConfigVersion getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(ConfigVersion configVersion) {
        this.configVersion = configVersion;
    }
} 