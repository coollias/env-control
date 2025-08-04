package com.bank.config.service.impl;

import com.bank.config.entity.Application;
import com.bank.config.repository.ApplicationRepository;
import com.bank.config.service.ApplicationService;
import com.bank.config.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 应用Service实现类
 * 
 * @author bank
 */
@Service
@Transactional
public class ApplicationServiceImpl implements ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private PermissionService permissionService;

    @Override
    public Application createApplication(Application application) {
        // 检查应用编码是否已存在
        if (existsByAppCode(application.getAppCode())) {
            throw new RuntimeException("应用编码已存在: " + application.getAppCode());
        }
        
        // 设置默认状态
        if (application.getStatus() == null) {
            application.setStatus(1);
        }
        
        return applicationRepository.save(application);
    }

    @Override
    public Application updateApplication(Long id, Application application) {
        Optional<Application> existingApp = applicationRepository.findById(id);
        if (!existingApp.isPresent()) {
            throw new RuntimeException("应用不存在: " + id);
        }
        
        Application existing = existingApp.get();
        
        // 检查应用编码是否与其他应用冲突
        if (!existing.getAppCode().equals(application.getAppCode()) && 
            existsByAppCode(application.getAppCode())) {
            throw new RuntimeException("应用编码已存在: " + application.getAppCode());
        }
        
        // 更新字段
        existing.setAppCode(application.getAppCode());
        existing.setAppName(application.getAppName());
        existing.setAppDesc(application.getAppDesc());
        existing.setOwner(application.getOwner());
        existing.setContactEmail(application.getContactEmail());
        
        return applicationRepository.save(existing);
    }

    @Override
    public void deleteApplication(Long id) {
        if (!applicationRepository.existsById(id)) {
            throw new RuntimeException("应用不存在: " + id);
        }
        applicationRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Application> findById(Long id) {
        return applicationRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Application> findByAppCode(String appCode) {
        return applicationRepository.findByAppCode(appCode);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Application> findApplications(String keyword, Integer status, Pageable pageable) {
        if (StringUtils.hasText(keyword)) {
            return applicationRepository.findByKeywordAndStatus(keyword, status, pageable);
        } else {
            return applicationRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Application> findAllEnabled() {
        return applicationRepository.findByStatusOrderByCreatedAtDesc(1);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByAppCode(String appCode) {
        return applicationRepository.existsByAppCode(appCode);
    }

    @Override
    public Application updateStatus(Long id, Integer status) {
        Optional<Application> optional = applicationRepository.findById(id);
        if (!optional.isPresent()) {
            throw new RuntimeException("应用不存在: " + id);
        }
        
        Application application = optional.get();
        application.setStatus(status);
        return applicationRepository.save(application);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAllEnabled() {
        return applicationRepository.countByStatus(1);
    }

    /**
     * 根据用户ID获取有权限的应用列表
     */
    @Transactional(readOnly = true)
    @Override
    public List<Application> findApplicationsByUserId(Long userId) {
        List<Long> userAppIds = permissionService.getUserAppIds(userId);
        if (userAppIds.isEmpty()) {
            return new ArrayList<>();
        }
        return applicationRepository.findByIdInAndStatusOrderByCreatedAtDesc(userAppIds, 1);
    }

    /**
     * 检查用户是否有应用的权限
     */
    public boolean hasApplicationPermission(Long userId, Long appId) {
        return permissionService.hasAppPermission(userId, appId);
    }

    /**
     * 检查用户是否有应用的写权限
     */
    public boolean hasApplicationWritePermission(Long userId, Long appId) {
        return permissionService.hasAppPermissionType(userId, appId, "WRITE") || 
               permissionService.hasAppPermissionType(userId, appId, "ADMIN");
    }

    /**
     * 检查用户是否有应用的管理权限
     */
    public boolean hasApplicationAdminPermission(Long userId, Long appId) {
        return permissionService.hasAppPermissionType(userId, appId, "ADMIN");
    }

    @Override
    public boolean isAppCreator(Long userId, Long appId) {
        Optional<Application> app = findById(appId);
        return app.isPresent() && app.get().getCreatedBy() != null && 
               app.get().getCreatedBy().equals(userId);
    }

    @Override
    public List<Application> findApplicationsByCreator(Long creatorId) {
        return applicationRepository.findByCreatedByAndStatusOrderByCreatedAtDesc(creatorId, 1);
    }
} 