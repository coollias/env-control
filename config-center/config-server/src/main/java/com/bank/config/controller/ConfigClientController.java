package com.bank.config.controller;

import com.bank.config.common.ApiResponse;
import com.bank.config.entity.ConfigItem;
import com.bank.config.service.ConfigItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 配置客户端API Controller
 * 提供客户端获取配置的接口
 * 
 * @author bank
 */
@RestController
@RequestMapping("/client")
@CrossOrigin(origins = "*")
public class ConfigClientController {

    @Autowired
    private ConfigItemService configItemService;

    /**
     * 获取单个配置项
     */
    @GetMapping("/config/{appCode}/{envCode}/{configKey}")
    public ApiResponse<String> getConfig(
            @PathVariable String appCode,
            @PathVariable String envCode,
            @PathVariable String configKey) {
        try {
            // 这里需要根据appCode和envCode获取对应的ID
            // 简化处理，实际应该通过Service查询
            Optional<ConfigItem> configItem = configItemService.findByAppIdAndEnvIdAndConfigKey(1L, 1L, configKey);
            if (configItem.isPresent()) {
                return ApiResponse.success(configItem.get().getConfigValue());
            } else {
                return ApiResponse.error(404, "配置项不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取应用在指定环境下的所有配置
     */
    @GetMapping("/configs/{appCode}/{envCode}")
    public ApiResponse<Map<String, String>> getConfigs(
            @PathVariable String appCode,
            @PathVariable String envCode) {
        try {
            // 简化处理，实际应该通过Service查询
            List<ConfigItem> configItems = configItemService.findByAppIdAndEnvId(1L, 1L);
            Map<String, String> configMap = new HashMap<>();
            for (ConfigItem configItem : configItems) {
                configMap.put(configItem.getConfigKey(), configItem.getConfigValue());
            }
            return ApiResponse.success(configMap);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取配置项详情
     */
    @GetMapping("/config-detail/{appCode}/{envCode}/{configKey}")
    public ApiResponse<ConfigItem> getConfigDetail(
            @PathVariable String appCode,
            @PathVariable String envCode,
            @PathVariable String configKey) {
        try {
            // 简化处理，实际应该通过Service查询
            Optional<ConfigItem> configItem = configItemService.findByAppIdAndEnvIdAndConfigKey(1L, 1L, configKey);
            if (configItem.isPresent()) {
                return ApiResponse.success(configItem.get());
            } else {
                return ApiResponse.error(404, "配置项不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 检查配置项是否存在
     */
    @GetMapping("/config-exists/{appCode}/{envCode}/{configKey}")
    public ApiResponse<Boolean> configExists(
            @PathVariable String appCode,
            @PathVariable String envCode,
            @PathVariable String configKey) {
        try {
            // 简化处理，实际应该通过Service查询
            Optional<ConfigItem> configItem = configItemService.findByAppIdAndEnvIdAndConfigKey(1L, 1L, configKey);
            return ApiResponse.success(configItem.isPresent());
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取配置项数量
     */
    @GetMapping("/config-count/{appCode}/{envCode}")
    public ApiResponse<Long> getConfigCount(
            @PathVariable String appCode,
            @PathVariable String envCode) {
        try {
            // 简化处理，实际应该通过Service查询
            long count = configItemService.countByAppIdAndEnvId(1L, 1L);
            return ApiResponse.success(count);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
} 