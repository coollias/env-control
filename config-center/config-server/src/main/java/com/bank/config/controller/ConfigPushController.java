package com.bank.config.controller;

import com.bank.config.common.ApiResponse;
import com.bank.config.service.ConfigPushService;
import com.bank.config.service.ConfigSnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 配置推送Controller
 * 
 * @author bank
 */
@RestController
@RequestMapping("/api/config-push")
public class ConfigPushController {

    @Autowired
    private ConfigPushService configPushService;

    @Autowired
    private ConfigSnapshotService configSnapshotService;

    /**
     * 推送配置到应用的所有客户端
     */
    @PostMapping("/app/{appId}/env/{envId}/push")
    public ApiResponse<Void> pushConfigToApp(@PathVariable Long appId, 
                                           @PathVariable Long envId,
                                           @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> configData = (Map<String, Object>) request.get("configData");
            
            configPushService.pushConfigToApp(appId, envId, configData);
            
            return ApiResponse.success("配置推送成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 推送配置到指定的客户端实例
     */
    @PostMapping("/app/{appId}/env/{envId}/push-to-instances")
    public ApiResponse<Void> pushConfigToInstances(@PathVariable Long appId, 
                                                 @PathVariable Long envId,
                                                 @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> configData = (Map<String, Object>) request.get("configData");
            @SuppressWarnings("unchecked")
            List<String> instanceIds = (List<String>) request.get("instanceIds");
            
            configPushService.pushConfigToInstances(appId, envId, configData, instanceIds);
            
            return ApiResponse.success("配置推送成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 推送快照配置
     */
    @PostMapping("/snapshot/{snapshotId}/push")
    public ApiResponse<Void> pushSnapshotConfig(@PathVariable Long snapshotId,
                                              @RequestBody Map<String, Object> request) {
        try {
            // 获取快照配置数据
            Map<String, Object> configData = configSnapshotService.getSnapshotConfigData(snapshotId);
            
            // 获取快照信息
            java.util.Optional<com.bank.config.entity.ConfigSnapshot> snapshotOpt = configSnapshotService.findById(snapshotId);
            if (!snapshotOpt.isPresent()) {
                return ApiResponse.error("快照不存在");
            }
            
            com.bank.config.entity.ConfigSnapshot snapshot = snapshotOpt.get();
            
            // 推送配置
            configPushService.pushConfigToApp(snapshot.getAppId(), snapshot.getEnvId(), configData);
            
            // 推送配置变更通知
            configPushService.pushConfigChangeNotification(snapshot.getAppId(), snapshot.getEnvId(), 
                snapshot.getVersionNumber(), "PUBLISH");
            
            return ApiResponse.success("快照配置推送成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 推送配置变更通知
     */
    @PostMapping("/app/{appId}/env/{envId}/notification")
    public ApiResponse<Void> pushConfigChangeNotification(@PathVariable Long appId, 
                                                       @PathVariable Long envId,
                                                       @RequestBody Map<String, String> request) {
        try {
            String versionNumber = request.get("versionNumber");
            String changeType = request.get("changeType");
            
            configPushService.pushConfigChangeNotification(appId, envId, versionNumber, changeType);
            
            return ApiResponse.success("配置变更通知推送成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取在线客户端列表
     */
    @GetMapping("/app/{appId}/clients")
    public ApiResponse<List<Map<String, Object>>> getOnlineClients(@PathVariable Long appId) {
        try {
            List<Map<String, Object>> clients = configPushService.getOnlineClients(appId);
            return ApiResponse.success(clients);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取客户端连接统计
     */
    @GetMapping("/app/{appId}/stats")
    public ApiResponse<Map<String, Object>> getClientConnectionStats(@PathVariable Long appId) {
        try {
            Map<String, Object> stats = configPushService.getClientConnectionStats(appId);
            return ApiResponse.success(stats);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 断开指定客户端连接
     */
    @PostMapping("/client/{connectionId}/disconnect")
    public ApiResponse<Void> disconnectClient(@PathVariable String connectionId) {
        try {
            configPushService.disconnectClient(connectionId);
            return ApiResponse.success("客户端断开成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 广播消息到所有客户端
     */
    @PostMapping("/broadcast")
    public ApiResponse<Void> broadcastMessage(@RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            configPushService.broadcastMessage(message);
            return ApiResponse.success("广播消息发送成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 发送消息到指定应用的所有客户端
     */
    @PostMapping("/app/{appId}/message")
    public ApiResponse<Void> sendMessageToApp(@PathVariable Long appId,
                                            @RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            configPushService.sendMessageToApp(appId, message);
            return ApiResponse.success("消息发送成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 发送消息到指定的客户端实例
     */
    @PostMapping("/instances/message")
    public ApiResponse<Void> sendMessageToInstances(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<String> instanceIds = (List<String>) request.get("instanceIds");
            String message = (String) request.get("message");
            
            configPushService.sendMessageToInstances(instanceIds, message);
            return ApiResponse.success("消息发送成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
