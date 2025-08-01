package com.bank.config.service;

import com.bank.config.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 应用Service接口
 * 
 * @author bank
 */
public interface ApplicationService {

    /**
     * 创建应用
     */
    Application createApplication(Application application);

    /**
     * 更新应用
     */
    Application updateApplication(Long id, Application application);

    /**
     * 删除应用
     */
    void deleteApplication(Long id);

    /**
     * 根据ID查找应用
     */
    Optional<Application> findById(Long id);

    /**
     * 根据应用编码查找应用
     */
    Optional<Application> findByAppCode(String appCode);

    /**
     * 分页查询应用
     */
    Page<Application> findApplications(String keyword, Integer status, Pageable pageable);

    /**
     * 获取所有启用的应用
     */
    List<Application> findAllEnabled();

    /**
     * 检查应用编码是否存在
     */
    boolean existsByAppCode(String appCode);

    /**
     * 启用/禁用应用
     */
    Application updateStatus(Long id, Integer status);
} 