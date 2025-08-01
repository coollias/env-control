package com.bank.config.entity;

import javax.persistence.*;

/**
 * 环境实体类
 * 
 * @author bank
 */
@Entity
@Table(name = "environments")
public class Environment extends BaseEntity {

    @Column(name = "env_code", nullable = false, unique = true, length = 32)
    private String envCode;

    @Column(name = "env_name", nullable = false, length = 64)
    private String envName;

    @Column(name = "env_desc", columnDefinition = "TEXT")
    private String envDesc;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "status", nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    private Integer status = 1; // 1-启用，0-禁用

    public String getEnvCode() {
        return envCode;
    }

    public void setEnvCode(String envCode) {
        this.envCode = envCode;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public String getEnvDesc() {
        return envDesc;
    }

    public void setEnvDesc(String envDesc) {
        this.envDesc = envDesc;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
} 