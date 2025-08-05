package com.bank.config.controller;

import com.bank.config.common.ApiResponse;
import com.bank.config.entity.ConfigItem;
import com.bank.config.service.ConfigItemService;
import com.bank.config.service.FileParseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 配置项管理Controller
 * 
 * @author bank
 */
@RestController
@RequestMapping("/api/config-items")
//@CrossOrigin(origins = "*")
public class ConfigItemController {

    @Autowired
    private ConfigItemService configItemService;

    @Autowired
    private FileParseService fileParseService;

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
     * 获取应用在指定环境下的完整配置（包含继承的配置）
     */
    @GetMapping("/app/{appId}/env/{envId}/merged")
    public ApiResponse<List<ConfigItem>> getMergedConfigsForAppAndEnv(
            @PathVariable Long appId,
            @PathVariable Long envId) {
        try {
            List<ConfigItem> mergedConfigs = configItemService.getMergedConfigsForAppAndEnv(appId, envId);
            return ApiResponse.success(mergedConfigs);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取应用在指定环境下的配置映射（键值对形式）
     */
    @GetMapping("/app/{appId}/env/{envId}/merged-map")
    public ApiResponse<Map<String, ConfigItem>> getMergedConfigMapForAppAndEnv(
            @PathVariable Long appId,
            @PathVariable Long envId) {
        try {
            Map<String, ConfigItem> mergedConfigs = configItemService.getMergedConfigMapForAppAndEnv(appId, envId);
            return ApiResponse.success(mergedConfigs);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取指定配置键在环境继承链中的最终值
     */
    @GetMapping("/app/{appId}/env/{envId}/key/{configKey}/inherited")
    public ApiResponse<ConfigItem> getConfigWithInheritance(
            @PathVariable Long appId,
            @PathVariable Long envId,
            @PathVariable String configKey) {
        try {
            Optional<ConfigItem> config = configItemService.getConfigWithInheritance(appId, envId, configKey);
            if (config.isPresent()) {
                return ApiResponse.success(config.get());
            } else {
                return ApiResponse.error(404, "配置项不存在");
            }
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取环境继承链
     */
    @GetMapping("/app/{appId}/env/{envId}/inheritance-chain")
    public ApiResponse<List<Long>> getEnvironmentInheritanceChain(
            @PathVariable Long appId,
            @PathVariable Long envId) {
        try {
            List<Long> envChain = configItemService.getEnvironmentInheritanceChain(appId, envId);
            return ApiResponse.success(envChain);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取应用在所有环境下的配置差异
     */
    @GetMapping("/app/{appId}/config-differences")
    public ApiResponse<Map<String, Map<String, String>>> getConfigDifferencesAcrossEnvironments(
            @PathVariable Long appId) {
        try {
            Map<String, Map<String, String>> differences = configItemService.getConfigDifferencesAcrossEnvironments(appId);
            return ApiResponse.success(differences);
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
            System.out.println(configItems);
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

    /**
     * 上传配置文件并解析
     */
    @PostMapping("/upload")
    public ApiResponse<List<ConfigItem>> uploadConfigFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("appId") Long appId,
            @RequestParam("envId") Long envId) {
        try {
            // 验证文件格式
            if (!fileParseService.isSupportedFileFormat(file.getOriginalFilename())) {
                return ApiResponse.error("不支持的文件格式，请上传JSON、YAML、XML或Properties文件");
            }

            // 解析文件
            List<ConfigItem> configItems = fileParseService.parseConfigFile(file, appId, envId);
            
            if (configItems.isEmpty()) {
                return ApiResponse.error("文件中没有找到有效的配置项");
            }

            // 批量保存配置项
            List<ConfigItem> savedConfigItems = configItemService.batchCreateConfigItems(configItems);
            
            return ApiResponse.success("文件解析成功，共导入 " + savedConfigItems.size() + " 个配置项", savedConfigItems);
        } catch (IOException e) {
            return ApiResponse.error("文件解析失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取支持的文件格式
     */
    @GetMapping("/supported-formats")
    public ApiResponse<List<String>> getSupportedFormats() {
        try {
            List<String> formats = fileParseService.getSupportedFormats();
            return ApiResponse.success(formats);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
} 