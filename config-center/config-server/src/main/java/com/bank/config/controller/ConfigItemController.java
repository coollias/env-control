package com.bank.config.controller;

import com.bank.config.common.ApiResponse;
import com.bank.config.entity.ConfigItem;
import com.bank.config.service.ConfigItemService;
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
 * 配置项管理Controller
 * 
 * @author bank
 */
@RestController
@RequestMapping("/config-items")
@CrossOrigin(origins = "*")
public class ConfigItemController {

    @Autowired
    private ConfigItemService configItemService;

    /**
     * 创建配置项
     */
    @PostMapping
    public ApiResponse<ConfigItem> createConfigItem(@Valid @RequestBody ConfigItem configItem) {
        try {
            ConfigItem created = configItemService.createConfigItem(configItem);
            return ApiResponse.success("配置项创建成功", created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新配置项
     */
    @PutMapping("/{id}")
    public ApiResponse<ConfigItem> updateConfigItem(@PathVariable Long id, @Valid @RequestBody ConfigItem configItem) {
        try {
            ConfigItem updated = configItemService.updateConfigItem(id, configItem);
            return ApiResponse.success("配置项更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除配置项
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteConfigItem(@PathVariable Long id) {
        try {
            configItemService.deleteConfigItem(id);
            return ApiResponse.success("配置项删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取配置项
     */
    @GetMapping("/{id}")
    public ApiResponse<ConfigItem> getConfigItem(@PathVariable Long id) {
        Optional<ConfigItem> configItem = configItemService.findById(id);
        if (configItem.isPresent()) {
            return ApiResponse.success(configItem.get());
        } else {
            return ApiResponse.error(404, "配置项不存在");
        }
    }

    /**
     * 根据应用ID、环境ID和配置键获取配置项
     */
    @GetMapping("/app/{appId}/env/{envId}/key/{configKey}")
    public ApiResponse<ConfigItem> getConfigItemByKey(
            @PathVariable Long appId,
            @PathVariable Long envId,
            @PathVariable String configKey) {
        Optional<ConfigItem> configItem = configItemService.findByAppIdAndEnvIdAndConfigKey(appId, envId, configKey);
        if (configItem.isPresent()) {
            return ApiResponse.success(configItem.get());
        } else {
            return ApiResponse.error(404, "配置项不存在");
        }
    }

    /**
     * 根据应用ID和环境ID获取配置项列表
     */
    @GetMapping("/app/{appId}/env/{envId}")
    public ApiResponse<List<ConfigItem>> getConfigItemsByAppAndEnv(
            @PathVariable Long appId,
            @PathVariable Long envId) {
        try {
            List<ConfigItem> configItems = configItemService.findByAppIdAndEnvId(appId, envId);
            return ApiResponse.success(configItems);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 分页查询配置项
     */
    @GetMapping
    public ApiResponse<Page<ConfigItem>> getConfigItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long appId,
            @RequestParam(required = false) Long envId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer status) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "configKey"));
            Page<ConfigItem> configItems = configItemService.findConfigItems(appId, envId, keyword, status, pageable);
            return ApiResponse.success(configItems);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据配置组ID获取配置项
     */
    @GetMapping("/app/{appId}/env/{envId}/group/{groupId}")
    public ApiResponse<List<ConfigItem>> getConfigItemsByGroup(
            @PathVariable Long appId,
            @PathVariable Long envId,
            @PathVariable Long groupId) {
        try {
            List<ConfigItem> configItems = configItemService.findByAppIdAndEnvIdAndGroupId(appId, envId, groupId);
            return ApiResponse.success(configItems);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 批量创建配置项
     */
    @PostMapping("/batch")
    public ApiResponse<List<ConfigItem>> batchCreateConfigItems(@Valid @RequestBody List<ConfigItem> configItems) {
        try {
            List<ConfigItem> created = configItemService.batchCreateConfigItems(configItems);
            return ApiResponse.success("批量创建配置项成功", created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 批量更新配置项
     */
    @PutMapping("/batch")
    public ApiResponse<List<ConfigItem>> batchUpdateConfigItems(@Valid @RequestBody List<ConfigItem> configItems) {
        try {
            List<ConfigItem> updated = configItemService.batchUpdateConfigItems(configItems);
            return ApiResponse.success("批量更新配置项成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新配置项状态
     */
    @PutMapping("/{id}/status")
    public ApiResponse<ConfigItem> updateConfigItemStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            ConfigItem configItem = configItemService.updateStatus(id, status);
            return ApiResponse.success("配置项状态更新成功", configItem);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取配置项数量
     */
    @GetMapping("/count/app/{appId}/env/{envId}")
    public ApiResponse<Long> getConfigItemCount(@PathVariable Long appId, @PathVariable Long envId) {
        try {
            long count = configItemService.countByAppIdAndEnvId(appId, envId);
            return ApiResponse.success(count);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
} 