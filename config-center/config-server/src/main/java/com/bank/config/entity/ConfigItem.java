package com.bank.config.entity;

import javax.persistence.*;

/**
 * 配置项实体类
 * 
 * @author bank
 */
@Entity
@Table(name = "config_items")
public class ConfigItem extends BaseEntity {

    @Column(name = "app_id", nullable = false)
    private Long appId;

    @Column(name = "env_id", nullable = false)
    private Long envId;

    @Column(name = "group_id")
    private Long groupId = 0L;

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

    @Column(name = "status", nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    private Integer status = 1; // 1-启用，0-禁用

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

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
} 