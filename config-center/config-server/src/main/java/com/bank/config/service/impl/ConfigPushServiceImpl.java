package com.bank.config.service.impl;

import com.bank.config.service.ConfigPushService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置推送Service实现类
 * 
 * @author bank
 */
@Service
public class ConfigPushServiceImpl implements ConfigPushService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // 存储客户端连接信息
    private final Map<String, ClientConnection> clientConnections = new ConcurrentHashMap<>();

    @Override
    public void pushConfigToApp(Long appId, Long envId, Map<String, Object> configData) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "CONFIG_UPDATE");
            message.put("appId", appId);
            message.put("envId", envId);
            message.put("configData", configData);
            message.put("timestamp", System.currentTimeMillis());

            String messageJson = objectMapper.writeValueAsString(message);
            
            // 推送到应用的所有客户端
            messagingTemplate.convertAndSend("/topic/app/" + appId + "/config", messageJson);
            
            // 同时推送到环境特定的频道
            messagingTemplate.convertAndSend("/topic/app/" + appId + "/env/" + envId + "/config", messageJson);
            
        } catch (Exception e) {
            throw new RuntimeException("推送配置失败", e);
        }
    }

    @Override
    public void pushConfigToInstances(Long appId, Long envId, Map<String, Object> configData, List<String> instanceIds) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "CONFIG_UPDATE");
            message.put("appId", appId);
            message.put("envId", envId);
            message.put("configData", configData);
            message.put("timestamp", System.currentTimeMillis());

            String messageJson = objectMapper.writeValueAsString(message);
            
            // 推送到指定的客户端实例
            for (String instanceId : instanceIds) {
                messagingTemplate.convertAndSend("/topic/instance/" + instanceId + "/config", messageJson);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("推送配置失败", e);
        }
    }

    @Override
    public void pushConfigChangeNotification(Long appId, Long envId, String versionNumber, String changeType) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "CONFIG_CHANGE_NOTIFICATION");
            message.put("appId", appId);
            message.put("envId", envId);
            message.put("versionNumber", versionNumber);
            message.put("changeType", changeType);
            message.put("timestamp", System.currentTimeMillis());

            String messageJson = objectMapper.writeValueAsString(message);
            
            // 推送配置变更通知
            messagingTemplate.convertAndSend("/topic/app/" + appId + "/notifications", messageJson);
            
        } catch (Exception e) {
            throw new RuntimeException("推送配置变更通知失败", e);
        }
    }

    @Override
    public List<Map<String, Object>> getOnlineClients(Long appId) {
        List<Map<String, Object>> clients = new ArrayList<>();
        
        for (Map.Entry<String, ClientConnection> entry : clientConnections.entrySet()) {
            ClientConnection connection = entry.getValue();
            if (connection.getAppId().equals(appId) && connection.isOnline()) {
                Map<String, Object> clientInfo = new HashMap<>();
                clientInfo.put("connectionId", connection.getConnectionId());
                clientInfo.put("instanceId", connection.getInstanceId());
                clientInfo.put("instanceIp", connection.getInstanceIp());
                clientInfo.put("clientVersion", connection.getClientVersion());
                clientInfo.put("lastHeartbeat", connection.getLastHeartbeat());
                clientInfo.put("connectedAt", connection.getConnectedAt());
                clients.add(clientInfo);
            }
        }
        
        return clients;
    }

    @Override
    public Map<String, Object> getClientConnectionStats(Long appId) {
        Map<String, Object> stats = new HashMap<>();
        
        int totalClients = 0;
        int onlineClients = 0;
        int offlineClients = 0;
        
        for (ClientConnection connection : clientConnections.values()) {
            if (connection.getAppId().equals(appId)) {
                totalClients++;
                if (connection.isOnline()) {
                    onlineClients++;
                } else {
                    offlineClients++;
                }
            }
        }
        
        stats.put("totalClients", totalClients);
        stats.put("onlineClients", onlineClients);
        stats.put("offlineClients", offlineClients);
        
        return stats;
    }

    @Override
    public void disconnectClient(String connectionId) {
        ClientConnection connection = clientConnections.get(connectionId);
        if (connection != null) {
            connection.setOnline(false);
            // 这里可以发送断开连接的消息
            messagingTemplate.convertAndSend("/topic/instance/" + connectionId + "/disconnect", 
                "{\"type\":\"DISCONNECT\",\"message\":\"Server initiated disconnect\"}");
        }
    }

    @Override
    public void broadcastMessage(String message) {
        messagingTemplate.convertAndSend("/topic/broadcast", message);
    }

    @Override
    public void sendMessageToApp(Long appId, String message) {
        messagingTemplate.convertAndSend("/topic/app/" + appId + "/message", message);
    }

    @Override
    public void sendMessageToInstances(List<String> instanceIds, String message) {
        for (String instanceId : instanceIds) {
            messagingTemplate.convertAndSend("/topic/instance/" + instanceId + "/message", message);
        }
    }

    /**
     * 注册客户端连接
     */
    public void registerClient(String connectionId, Long appId, String instanceId, String instanceIp, String clientVersion) {
        ClientConnection connection = new ClientConnection();
        connection.setConnectionId(connectionId);
        connection.setAppId(appId);
        connection.setInstanceId(instanceId);
        connection.setInstanceIp(instanceIp);
        connection.setClientVersion(clientVersion);
        connection.setOnline(true);
        connection.setConnectedAt(new Date());
        connection.setLastHeartbeat(new Date());
        
        clientConnections.put(connectionId, connection);
    }

    /**
     * 更新客户端心跳
     */
    public void updateClientHeartbeat(String connectionId) {
        ClientConnection connection = clientConnections.get(connectionId);
        if (connection != null) {
            connection.setLastHeartbeat(new Date());
        }
    }

    /**
     * 移除客户端连接
     */
    public void removeClient(String connectionId) {
        clientConnections.remove(connectionId);
    }

    /**
     * 客户端连接信息内部类
     */
    private static class ClientConnection {
        private String connectionId;
        private Long appId;
        private String instanceId;
        private String instanceIp;
        private String clientVersion;
        private boolean online;
        private Date connectedAt;
        private Date lastHeartbeat;

        // Getters and Setters
        public String getConnectionId() { return connectionId; }
        public void setConnectionId(String connectionId) { this.connectionId = connectionId; }
        
        public Long getAppId() { return appId; }
        public void setAppId(Long appId) { this.appId = appId; }
        
        public String getInstanceId() { return instanceId; }
        public void setInstanceId(String instanceId) { this.instanceId = instanceId; }
        
        public String getInstanceIp() { return instanceIp; }
        public void setInstanceIp(String instanceIp) { this.instanceIp = instanceIp; }
        
        public String getClientVersion() { return clientVersion; }
        public void setClientVersion(String clientVersion) { this.clientVersion = clientVersion; }
        
        public boolean isOnline() { return online; }
        public void setOnline(boolean online) { this.online = online; }
        
        public Date getConnectedAt() { return connectedAt; }
        public void setConnectedAt(Date connectedAt) { this.connectedAt = connectedAt; }
        
        public Date getLastHeartbeat() { return lastHeartbeat; }
        public void setLastHeartbeat(Date lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
    }
}
