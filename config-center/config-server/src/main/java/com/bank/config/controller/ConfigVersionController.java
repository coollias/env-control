package com.bank.config.controller;

import com.bank.config.common.ApiResponse;
import com.bank.config.dto.ConfigVersionDTO;
import com.bank.config.entity.ConfigVersion;
import com.bank.config.service.ConfigVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 配置版本管理Controller
 * 
 * @author bank
 */
@RestController
@RequestMapping("/api/config-versions")
public class ConfigVersionController {

    @Autowired
    private ConfigVersionService configVersionService;

    /**
     * 创建配置版本
     */
    @PostMapping
    public ApiResponse<ConfigVersion> createVersion(@RequestBody ConfigVersion version) {
        try {
            ConfigVersion created = configVersionService.createVersion(version);
            return ApiResponse.success("版本创建成功", created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取版本
     */
    @GetMapping("/{id}")
    public ApiResponse<ConfigVersion> getVersion(@PathVariable Long id) {
        Optional<ConfigVersion> version = configVersionService.findById(id);
        if (version.isPresent()) {
            return ApiResponse.success(version.get());
        } else {
            return ApiResponse.error(404, "版本不存在");
        }
    }

    /**
     * 根据应用ID和环境ID获取版本列表
     */
    @GetMapping("/app/{appId}/env/{envId}")
    public ApiResponse<List<ConfigVersionDTO>> getVersionsByAppAndEnv(
            @PathVariable Long appId,
            @PathVariable Long envId) {
        try {
            List<ConfigVersionDTO> versions = configVersionService.findDTOsByAppIdAndEnvId(appId, envId);
            return ApiResponse.success(versions);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 分页获取版本列表
     */
    @GetMapping("/app/{appId}/env/{envId}/page")
    public ApiResponse<Page<ConfigVersion>> getVersionsByAppAndEnvPage(
            @PathVariable Long appId,
            @PathVariable Long envId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ConfigVersion> versions = configVersionService.findByAppIdAndEnvId(appId, envId, pageable);
            return ApiResponse.success(versions);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除版本
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteVersion(@PathVariable Long id) {
        try {
            configVersionService.deleteVersion(id);
            return ApiResponse.success("版本删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取最新版本号
     */
    @GetMapping("/app/{appId}/env/{envId}/latest")
    public ApiResponse<String> getLatestVersionNumber(
            @PathVariable Long appId,
            @PathVariable Long envId) {
        try {
            String versionNumber = configVersionService.getLatestVersionNumber(appId, envId);
            return ApiResponse.success(versionNumber);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 生成新版本号
     */
    @GetMapping("/app/{appId}/env/{envId}/generate")
    public ApiResponse<String> generateVersionNumber(
            @PathVariable Long appId,
            @PathVariable Long envId) {
        try {
            String versionNumber = configVersionService.generateVersionNumber(appId, envId);
            return ApiResponse.success(versionNumber);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 创建版本并记录变更
     */
    @PostMapping("/app/{appId}/env/{envId}/create-with-changes")
    public ApiResponse<ConfigVersion> createVersionWithChanges(
            @PathVariable Long appId,
            @PathVariable Long envId,
            @RequestBody Map<String, Object> request) {
        try {
            String versionName = (String) request.get("versionName");
            String versionDesc = (String) request.get("versionDesc");
            String createdBy = (String) request.get("createdBy");
            @SuppressWarnings("unchecked")
            Map<String, String> changes = (Map<String, String>) request.get("changes");
            Integer changeType = (Integer) request.get("changeType");
            
            ConfigVersion version = configVersionService.createVersionWithChanges(
                    appId, envId, versionName, versionDesc, createdBy, changes, changeType);
            return ApiResponse.success("版本创建成功", version);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 回滚到指定版本
     */
    @PostMapping("/app/{appId}/env/{envId}/rollback/{versionNumber}")
    public ApiResponse<ConfigVersion> rollbackToVersion(
            @PathVariable Long appId,
            @PathVariable Long envId,
            @PathVariable String versionNumber,
            @RequestBody Map<String, String> request) {
        try {
            String createdBy = request.get("createdBy");
            ConfigVersion version = configVersionService.rollbackToVersion(appId, envId, versionNumber, createdBy);
            return ApiResponse.success("回滚成功", version);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 比较两个版本的差异
     */
    @GetMapping("/app/{appId}/env/{envId}/compare")
    public ApiResponse<Map<String, Object>> compareVersions(
            @PathVariable Long appId,
            @PathVariable Long envId,
            @RequestParam String version1,
            @RequestParam String version2) {
        try {
            Map<String, Object> differences = configVersionService.compareVersions(appId, envId, version1, version2);
            return ApiResponse.success(differences);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取版本的变更详情
     */
    @GetMapping("/{id}/changes")
    public ApiResponse<List<Map<String, Object>>> getVersionChanges(@PathVariable Long id) {
        try {
            List<Map<String, Object>> changes = configVersionService.getVersionChanges(id);
            return ApiResponse.success(changes);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取配置项的变更历史
     */
    @GetMapping("/app/{appId}/env/{envId}/config/{configKey}/history")
    public ApiResponse<List<Map<String, Object>>> getConfigChangeHistory(
            @PathVariable Long appId,
            @PathVariable Long envId,
            @PathVariable String configKey) {
        try {
            List<Map<String, Object>> history = configVersionService.getConfigChangeHistory(appId, envId, configKey);
            return ApiResponse.success(history);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
} 