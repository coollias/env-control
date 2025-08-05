package com.bank.config.service.impl;

import com.bank.config.entity.Application;
import com.bank.config.repository.ApplicationRepository;
import com.bank.config.service.ApplicationService;
import com.bank.config.service.PermissionService;
import com.bank.config.service.RedisCacheService;
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

    @Autowired
    private RedisCacheService redisCacheService;

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
        
        Application saved = applicationRepository.save(application);
        
        // 清理相关缓存
        System.out.println("=== 清理应用缓存 ===");
        redisCacheService.deleteByPattern("app*");
        
        return saved;
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
        
        Application saved = applicationRepository.save(existing);
        
        // 清理相关缓存
        redisCacheService.deleteByPattern("app*");
        
        return saved;
    }

    @Override
    public void deleteApplication(Long id) {
        if (!applicationRepository.existsById(id)) {
            throw new RuntimeException("应用不存在: " + id);
        }
        applicationRepository.deleteById(id);
        
        // 清理相关缓存
        redisCacheService.deleteByPattern("app*");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Application> findById(Long id) {
        // 尝试从缓存获取
        String cacheKey = "app:" + id;
        Optional<Application> cached = redisCacheService.get(cacheKey, Application.class);
        if (cached.isPresent()) {
            System.out.println("=== 缓存命中: " + cacheKey + " ===");
            return cached;
        }
        
        System.out.println("=== 缓存未命中: " + cacheKey + " ===");
        
        // 从数据库获取
        Optional<Application> app = applicationRepository.findById(id);
        if (app.isPresent()) {
            // 存入缓存
            redisCacheService.set(cacheKey, app.get());
            System.out.println("=== 已缓存: " + cacheKey + " ===");
        }
        
        return app;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Application> findByAppCode(String appCode) {
        // 尝试从缓存获取
        String cacheKey = "app:code:" + appCode;
        Optional<Application> cached = redisCacheService.get(cacheKey, Application.class);
        if (cached.isPresent()) {
            return cached;
        }
        
        // 从数据库获取
        Optional<Application> app = applicationRepository.findByAppCode(appCode);
        if (app.isPresent()) {
            // 存入缓存
            redisCacheService.set(cacheKey, app.get());
        }
        
        return app;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Application> findApplications(String keyword, Integer status, Pageable pageable) {
        // 对于分页查询，我们只缓存基础数据，不缓存分页结果
        // 因为分页查询变化频繁，缓存效果不明显
        if (StringUtils.hasText(keyword)) {
            return applicationRepository.findByKeywordAndStatus(keyword, status, pageable);
        } else {
            return applicationRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Application> findAllEnabled() {
        // 尝试从缓存获取
        String cacheKey = "app:enabled";
        Optional<List<Application>> cached = redisCacheService.get(cacheKey, new com.fasterxml.jackson.core.type.TypeReference<List<Application>>() {});
        if (cached.isPresent()) {
            return cached.get();
        }
        
        // 从数据库获取
        List<Application> apps = applicationRepository.findByStatusOrderByCreatedAtDesc(1);
        
        // 存入缓存
        redisCacheService.set(cacheKey, apps);
        
        return apps;
    }

    @Override
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
        // 尝试从缓存获取
        String cacheKey = "app:user:" + userId;
        Optional<List<Application>> cached = redisCacheService.get(cacheKey, new com.fasterxml.jackson.core.type.TypeReference<List<Application>>() {});
        if (cached.isPresent()) {
            System.out.println("=== 从缓存获取用户应用列表: " + userId + " ===");
            return cached.get();
        }
        
        System.out.println("=== 缓存未命中用户应用列表: " + userId + " ===");
        
        List<Long> userAppIds = permissionService.getUserAppIds(userId);
        if (userAppIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Application> apps = applicationRepository.findByIdInAndStatusOrderByCreatedAtDesc(userAppIds, 1);
        
        // 存入缓存
        redisCacheService.set(cacheKey, apps);
        System.out.println("=== 已缓存用户应用列表: " + userId + " ===");
        
        return apps;
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

    /**
     * 测试缓存方法
     */
    public String testCache(Long id) {
        // 尝试从缓存获取
        String cacheKey = "test:app:" + id;
        Optional<String> cached = redisCacheService.get(cacheKey, String.class);
        if (cached.isPresent()) {
            System.out.println("从缓存获取测试值: " + id);
            return cached.get();
        }
        
        // 生成新值
        String value = "test-value-" + id;
        System.out.println("=== 调用testCache方法，参数: " + id + " ===");
        
        // 存入缓存
        redisCacheService.set(cacheKey, value);
        System.out.println("测试值已缓存: " + id);
        
        return value;
    }
} 