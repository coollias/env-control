package com.bank.config.service;

import com.bank.config.entity.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 环境Service接口
 * 
 * @author bank
 */
public interface EnvironmentService {

    /**
     * 创建环境
     */
    Environment createEnvironment(Environment environment);

    /**
     * 更新环境
     */
    Environment updateEnvironment(Long id, Environment environment);

    /**
     * 删除环境
     */
    void deleteEnvironment(Long id);

    /**
     * 根据ID查找环境
     */
    Optional<Environment> findById(Long id);

    /**
     * 根据环境编码查找环境
     */
    Optional<Environment> findByEnvCode(String envCode);

    /**
     * 分页查询环境
     */
    Page<Environment> findEnvironments(Integer status, Pageable pageable);

    /**
     * 获取所有启用的环境
     */
    List<Environment> findAllEnabled();

    /**
     * 检查环境编码是否存在
     */
    boolean existsByEnvCode(String envCode);

    /**
     * 启用/禁用环境
     */
    Environment updateStatus(Long id, Integer status);

    /**
     * 统计启用的环境数量
     */
    long countAllEnabled();
} 