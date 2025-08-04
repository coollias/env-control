package com.bank.config.controller;

import com.bank.config.common.ApiResponse;
import com.bank.config.entity.Environment;
import com.bank.config.service.EnvironmentService;
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
 * 环境管理Controller
 * 
 * @author bank
 */
@RestController
@RequestMapping("/api/environments")
//@CrossOrigin(origins = "*")
public class EnvironmentController {

    @Autowired
    private EnvironmentService environmentService;

    /**
     * 创建环境
     */
    @PostMapping
    public ApiResponse<Environment> createEnvironment(@Valid @RequestBody Environment environment) {
        try {
            Environment created = environmentService.createEnvironment(environment);
            return ApiResponse.success("环境创建成功", created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新环境
     */
    @PutMapping("/{id}")
    public ApiResponse<Environment> updateEnvironment(@PathVariable Long id, @Valid @RequestBody Environment environment) {
        try {
            Environment updated = environmentService.updateEnvironment(id, environment);
            return ApiResponse.success("环境更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除环境
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteEnvironment(@PathVariable Long id) {
        try {
            environmentService.deleteEnvironment(id);
            return ApiResponse.success("环境删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取环境
     */
    @GetMapping("/{id}")
    public ApiResponse<Environment> getEnvironment(@PathVariable Long id) {
        Optional<Environment> environment = environmentService.findById(id);
        if (environment.isPresent()) {
            return ApiResponse.success(environment.get());
        } else {
            return ApiResponse.error(404, "环境不存在");
        }
    }

    /**
     * 根据环境编码获取环境
     */
    @GetMapping("/code/{envCode}")
    public ApiResponse<Environment> getEnvironmentByCode(@PathVariable String envCode) {
        Optional<Environment> environment = environmentService.findByEnvCode(envCode);
        if (environment.isPresent()) {
            return ApiResponse.success(environment.get());
        } else {
            return ApiResponse.error(404, "环境不存在");
        }
    }

    /**
     * 分页查询环境
     */
    @GetMapping
    public ApiResponse<Page<Environment>> getEnvironments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "1") Integer status) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "sortOrder"));
            Page<Environment> environments = environmentService.findEnvironments(status, pageable);
            return ApiResponse.success(environments);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取所有启用的环境
     */
    @GetMapping("/enabled")
    public ApiResponse<List<Environment>> getEnabledEnvironments() {
        try {
            List<Environment> environments = environmentService.findAllEnabled();
            return ApiResponse.success(environments);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新环境状态
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Environment> updateEnvironmentStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            Environment environment = environmentService.updateStatus(id, status);
            return ApiResponse.success("环境状态更新成功", environment);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 检查环境编码是否存在
     */
    @GetMapping("/check-code/{envCode}")
    public ApiResponse<Boolean> checkEnvCodeExists(@PathVariable String envCode) {
        boolean exists = environmentService.existsByEnvCode(envCode);
        return ApiResponse.success(exists);
    }
} 