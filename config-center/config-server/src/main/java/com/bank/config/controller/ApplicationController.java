package com.bank.config.controller;

import com.bank.config.common.ApiResponse;
import com.bank.config.entity.Application;
import com.bank.config.service.ApplicationService;
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
@RequestMapping("/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    /**
     * 创建应用
     */
    @PostMapping
    public ApiResponse<Application> createApplication(@Valid @RequestBody Application application) {
        try {
            Application created = applicationService.createApplication(application);
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
        Optional<Application> application = applicationService.findById(id);
        if (application.isPresent()) {
            return ApiResponse.success(application.get());
        } else {
            return ApiResponse.error(404, "应用不存在");
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
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Application> applications = applicationService.findApplications(keyword, status, pageable);
            return ApiResponse.success(applications);
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
            List<Application> applications = applicationService.findAllEnabled();
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