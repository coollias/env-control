package com.bank.config.controller;

import com.bank.config.common.ApiResponse;
import com.bank.config.common.UserContext;
import com.bank.config.entity.ConfigSnapshot;
import com.bank.config.entity.ConfigSnapshotItem;
import com.bank.config.service.ConfigSnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 配置快照管理Controller
 * 
 * @author bank
 */
@RestController
@RequestMapping("/api/config-snapshots")
public class ConfigSnapshotController {

    @Autowired
    private ConfigSnapshotService configSnapshotService;

    /**
     * 创建配置快照（暂存功能）
     */
    @PostMapping("/staged")
    public ApiResponse<ConfigSnapshot> createStagedSnapshot(@RequestBody Map<String, Object> request) {
        try {
            Long appId = Long.valueOf(request.get("appId").toString());
            Long envId = Long.valueOf(request.get("envId").toString());
            String snapshotName = (String) request.get("snapshotName");
            String snapshotDesc = (String) request.get("snapshotDesc");
            @SuppressWarnings("unchecked")
            Map<String, Object> configData = (Map<String, Object>) request.get("configData");
            // 获取当前用户名
            String createdBy = UserContext.getCurrentUsername();
            if (createdBy == null) {
                createdBy = "admin"; // 默认值
            }

            ConfigSnapshot snapshot = configSnapshotService.createSnapshot(appId, envId, snapshotName, 
                snapshotDesc, configData, createdBy);
            
            return ApiResponse.success("快照创建成功", snapshot);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 发布配置快照
     */
    @PostMapping("/{snapshotId}/publish")
    public ApiResponse<ConfigSnapshot> publishSnapshot(@PathVariable Long snapshotId, 
                                                     @RequestBody Map<String, String> request) {
        try {
            // 获取当前用户名
            String publishedBy = UserContext.getCurrentUsername();
            if (publishedBy == null) {
                publishedBy = "admin"; // 默认值
            }
            
            ConfigSnapshot snapshot = configSnapshotService.publishSnapshot(snapshotId, publishedBy);
            return ApiResponse.success("快照发布成功", snapshot);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取快照
     */
    @GetMapping("/{id}")
    public ApiResponse<ConfigSnapshot> getSnapshot(@PathVariable Long id) {
        Optional<ConfigSnapshot> snapshot = configSnapshotService.findById(id);
        if (snapshot.isPresent()) {
            return ApiResponse.success(snapshot.get());
        } else {
            return ApiResponse.error(404, "快照不存在");
        }
    }

    /**
     * 根据应用ID和环境ID获取快照列表
     */
    @GetMapping("/app/{appId}/env/{envId}")
    public ApiResponse<List<ConfigSnapshot>> getSnapshotsByAppAndEnv(@PathVariable Long appId, 
                                                                    @PathVariable Long envId) {
        try {
            List<ConfigSnapshot> snapshots = configSnapshotService.findByAppIdAndEnvId(appId, envId);
            return ApiResponse.success(snapshots);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 分页获取快照列表
     */
    @GetMapping("/app/{appId}/env/{envId}/page")
    public ApiResponse<Page<ConfigSnapshot>> getSnapshotsByAppAndEnvPage(@PathVariable Long appId, 
                                                                        @PathVariable Long envId,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ConfigSnapshot> snapshots = configSnapshotService.findByAppIdAndEnvId(appId, envId, pageable);
            return ApiResponse.success(snapshots);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除快照
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSnapshot(@PathVariable Long id) {
        try {
            configSnapshotService.deleteSnapshot(id);
            return ApiResponse.success("快照删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取最新版本号
     */
    @GetMapping("/app/{appId}/env/{envId}/latest-version")
    public ApiResponse<String> getLatestVersionNumber(@PathVariable Long appId, @PathVariable Long envId) {
        try {
            String versionNumber = configSnapshotService.getLatestVersionNumber(appId, envId);
            return ApiResponse.success(versionNumber);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 生成新版本号
     */
    @GetMapping("/app/{appId}/env/{envId}/generate-version")
    public ApiResponse<String> generateVersionNumber(@PathVariable Long appId, @PathVariable Long envId) {
        try {
            String versionNumber = configSnapshotService.generateVersionNumber(appId, envId);
            return ApiResponse.success(versionNumber);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取快照的配置项列表
     */
    @GetMapping("/{snapshotId}/items")
    public ApiResponse<List<ConfigSnapshotItem>> getSnapshotItems(@PathVariable Long snapshotId) {
        try {
            List<ConfigSnapshotItem> items = configSnapshotService.getSnapshotItems(snapshotId);
            return ApiResponse.success(items);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取快照的配置数据
     */
    @GetMapping("/{snapshotId}/config-data")
    public ApiResponse<Map<String, Object>> getSnapshotConfigData(@PathVariable Long snapshotId) {
        try {
            Map<String, Object> configData = configSnapshotService.getSnapshotConfigData(snapshotId);
            return ApiResponse.success(configData);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 比较两个快照的差异
     */
    @GetMapping("/compare")
    public ApiResponse<Map<String, Object>> compareSnapshots(@RequestParam Long snapshotId1, 
                                                            @RequestParam Long snapshotId2) {
        try {
            Map<String, Object> differences = configSnapshotService.compareSnapshots(snapshotId1, snapshotId2);
            return ApiResponse.success(differences);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 回滚到指定快照
     */
    @PostMapping("/app/{appId}/env/{envId}/rollback/{snapshotId}")
    public ApiResponse<ConfigSnapshot> rollbackToSnapshot(@PathVariable Long appId, 
                                                        @PathVariable Long envId,
                                                        @PathVariable Long snapshotId,
                                                        @RequestBody Map<String, String> request) {
        try {
            // 获取当前用户名
            String createdBy = UserContext.getCurrentUsername();
            if (createdBy == null) {
                createdBy = "admin"; // 默认值
            }
            
            ConfigSnapshot snapshot = configSnapshotService.rollbackToSnapshot(appId, envId, snapshotId, createdBy);
            return ApiResponse.success("回滚成功", snapshot);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取应用环境的最新发布快照
     */
    @GetMapping("/app/{appId}/env/{envId}/latest-published")
    public ApiResponse<ConfigSnapshot> getLatestPublishedSnapshot(@PathVariable Long appId, @PathVariable Long envId) {
        try {
            Optional<ConfigSnapshot> snapshot = configSnapshotService.getLatestPublishedSnapshot(appId, envId);
            if (snapshot.isPresent()) {
                return ApiResponse.success(snapshot.get());
            } else {
                return ApiResponse.error(404, "没有找到发布快照");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取应用环境的最新暂存快照
     */
    @GetMapping("/app/{appId}/env/{envId}/latest-staged")
    public ApiResponse<ConfigSnapshot> getLatestStagedSnapshot(@PathVariable Long appId, @PathVariable Long envId) {
        try {
            Optional<ConfigSnapshot> snapshot = configSnapshotService.getLatestStagedSnapshot(appId, envId);
            if (snapshot.isPresent()) {
                return ApiResponse.success(snapshot.get());
            } else {
                return ApiResponse.error(404, "没有找到暂存快照");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 应用快照到环境
     */
    @PostMapping("/{snapshotId}/apply")
    public ApiResponse<Void> applySnapshotToEnvironment(@PathVariable Long snapshotId,
                                                      @RequestBody Map<String, String> request) {
        try {
            String appliedBy = request.get("appliedBy");
            configSnapshotService.applySnapshotToEnvironment(snapshotId, appliedBy);
            return ApiResponse.success("快照应用成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 验证快照配置
     */
    @PostMapping("/{snapshotId}/validate")
    public ApiResponse<Boolean> validateSnapshotConfig(@PathVariable Long snapshotId) {
        try {
            boolean isValid = configSnapshotService.validateSnapshotConfig(snapshotId);
            return ApiResponse.success(isValid);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取快照统计信息
     */
    @GetMapping("/app/{appId}/env/{envId}/statistics")
    public ApiResponse<Map<String, Object>> getSnapshotStatistics(@PathVariable Long appId, @PathVariable Long envId) {
        try {
            Map<String, Object> statistics = configSnapshotService.getSnapshotStatistics(appId, envId);
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
