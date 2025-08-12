package com.bank.config.service;

import java.util.List;
import java.util.Map;

/**
 * 配置推送Service接口
 * 
 * @author bank
 */
public interface ConfigPushService {

    /**
     * 推送配置到指定应用的所有客户端
     */
    void pushConfigToApp(Long appId, Long envId, Map<String, Object> configData);

    /**
     * 推送配置到指定的客户端实例
     */
    void pushConfigToInstances(Long appId, Long envId, Map<String, Object> configData, List<String> instanceIds);

    /**
     * 推送配置变更通知
     */
    void pushConfigChangeNotification(Long appId, Long envId, String versionNumber, String changeType);

    /**
     * 获取在线客户端列表
     */
    List<Map<String, Object>> getOnlineClients(Long appId);

    /**
     * 获取客户端连接统计
     */
    Map<String, Object> getClientConnectionStats(Long appId);

    /**
     * 断开指定客户端连接
     */
    void disconnectClient(String connectionId);

    /**
     * 广播消息到所有客户端
     */
    void broadcastMessage(String message);

    /**
     * 发送消息到指定应用的所有客户端
     */
    void sendMessageToApp(Long appId, String message);

    /**
     * 发送消息到指定的客户端实例
     */
    void sendMessageToInstances(List<String> instanceIds, String message);
    
    /**
     * 注册客户端连接
     */
    void registerClient(String connectionId, Long appId, String instanceId, String instanceIp, String clientVersion);
    
    /**
     * 更新客户端心跳时间
     */
    void updateClientHeartbeat(String connectionId);
    
    /**
     * 移除客户端连接
     */
    void removeClient(String connectionId);
}
