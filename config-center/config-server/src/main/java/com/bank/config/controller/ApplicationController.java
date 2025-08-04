package com.bank.config.controller;

import com.bank.config.common.ApiResponse;
import com.bank.config.common.UserContext;
import com.bank.config.entity.Application;
import com.bank.config.service.ApplicationService;
import com.bank.config.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * 应用管理Controller
 * 
 * @author bank
 */
@RestController
@RequestMapping("/api/applications")
//@CrossOrigin(origins = "*")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;
    
    @Autowired
    private PermissionService permissionService;

    /**
     * 创建应用
     */
    @PostMapping
    public ApiResponse<Application> createApplication(@Valid @RequestBody Application application) {
        try {
            Long currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                return ApiResponse.error(401, "用户未登录");
            }
            
            // 设置创建者
            application.setCreatedBy(currentUserId);
            
            Application created = applicationService.createApplication(application);
            
            // 为创建者自动分配应用管理员权限
            try {
                permissionService.assignAppPermission(currentUserId, created.getId(), "ADMIN");
            } catch (Exception e) {
                // 记录错误但不影响应用创建
                System.err.println("为应用创建者分配权限失败: " + e.getMessage());
            }
            
            return ApiResponse.success("应用创建成功", created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新应用
     */
    @PutMapping("/{id}")
    public ApiResponse<Application> updateApplication(@PathVariable Long id, @Valid @RequestBody Application application) {
        try {
            Long currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                return ApiResponse.error(401, "用户未登录");
            }
            
            // 检查用户是否有写权限
            if (!applicationService.hasApplicationWritePermission(currentUserId, id)) {
                return ApiResponse.error(403, "没有权限修改此应用");
            }
            
            Application updated = applicationService.updateApplication(id, application);
            return ApiResponse.success("应用更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除应用
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteApplication(@PathVariable Long id) {
        try {
            Long currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                return ApiResponse.error(401, "用户未登录");
            }
            
            // 检查用户是否有管理员权限
            if (!applicationService.hasApplicationAdminPermission(currentUserId, id)) {
                return ApiResponse.error(403, "没有权限删除此应用");
            }
            
            applicationService.deleteApplication(id);
            return ApiResponse.success("应用删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取应用
     */
    @GetMapping("/{id}")
    public ApiResponse<Application> getApplication(@PathVariable Long id) {
        try {
            Long currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                return ApiResponse.error(401, "用户未登录");
            }
            
            // 检查用户是否有权限访问此应用
            if (!applicationService.hasApplicationPermission(currentUserId, id)) {
                return ApiResponse.error(403, "没有权限访问此应用");
            }
            
            Optional<Application> application = applicationService.findById(id);
            if (application.isPresent()) {
                return ApiResponse.success(application.get());
            } else {
                return ApiResponse.error(404, "应用不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据应用编码获取应用
     */
    @GetMapping("/code/{appCode}")
    public ApiResponse<Application> getApplicationByCode(@PathVariable String appCode) {
        Optional<Application> application = applicationService.findByAppCode(appCode);
        if (application.isPresent()) {
            return ApiResponse.success(application.get());
        } else {
            return ApiResponse.error(404, "应用不存在");
        }
    }

    /**
     * 分页查询应用
     */
    @GetMapping
    public ApiResponse<Page<Application>> getApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer status) {
        try {
            Long currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                return ApiResponse.error(401, "用户未登录");
            }
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            
            // 获取用户有权限的应用
            List<Application> userApplications = applicationService.findApplicationsByUserId(currentUserId);
            
            // 获取所有应用
            Page<Application> allApplications = applicationService.findApplications(keyword, status, pageable);
            
            // 过滤：用户可以看到有权限的应用 + 自己创建的应用
            List<Application> filteredApplications = allApplications.getContent().stream()
                    .filter(app -> {
                        // 用户有权限的应用
                        boolean hasPermission = userApplications.stream()
                                .anyMatch(userApp -> userApp.getId().equals(app.getId()));
                        
                        // 或者用户创建的应用
                        boolean isCreator = app.getCreatedBy() != null && app.getCreatedBy().equals(currentUserId);
                        
                        return hasPermission || isCreator;
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            // 创建新的Page对象
            Page<Application> filteredPage = new org.springframework.data.domain.PageImpl<>(
                    filteredApplications, pageable, allApplications.getTotalElements());
            
            return ApiResponse.success(filteredPage);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取所有启用的应用
     */
    @GetMapping("/enabled")
    public ApiResponse<List<Application>> getEnabledApplications() {
        try {
            Long currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                return ApiResponse.error(401, "用户未登录");
            }
            
            // 只返回用户有权限的应用
            List<Application> userApplications = applicationService.findApplicationsByUserId(currentUserId);
            return ApiResponse.success(userApplications);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取所有应用（用于权限管理）
     */
    @GetMapping("/for-permissions")
    public ApiResponse<List<Application>> getApplicationsForPermissions() {
        try {
            Long currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                return ApiResponse.error(401, "用户未登录");
            }
            
            // 只返回用户有权限管理的应用
            // 1. 系统管理员可以看到所有应用
            // 2. 应用管理员可以看到自己管理的应用
            // 3. 应用创建者可以看到自己创建的应用
            boolean isSystemAdmin = permissionService.hasSystemPermission(currentUserId, "app:admin");
            
            List<Application> applications;
            if (isSystemAdmin) {
                // 系统管理员可以看到所有应用
                applications = applicationService.findAllEnabled();
            } else {
                // 普通用户只能看到有权限管理的应用
                List<Application> userApplications = applicationService.findApplicationsByUserId(currentUserId);
                List<Application> createdApplications = applicationService.findApplicationsByCreator(currentUserId);
                
                // 合并并去重
                applications = new java.util.ArrayList<>();
                applications.addAll(userApplications);
                applications.addAll(createdApplications);
                
                // 去重
                applications = applications.stream()
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
            }
            
            return ApiResponse.success(applications);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新应用状态
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Application> updateApplicationStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            Application application = applicationService.updateStatus(id, status);
            return ApiResponse.success("应用状态更新成功", application);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 检查应用编码是否存在
     */
    @GetMapping("/check-code/{appCode}")
    public ApiResponse<Boolean> checkAppCodeExists(@PathVariable String appCode) {
        boolean exists = applicationService.existsByAppCode(appCode);
        return ApiResponse.success(exists);
    }
} 