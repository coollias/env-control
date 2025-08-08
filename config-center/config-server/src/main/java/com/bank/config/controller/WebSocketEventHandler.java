package com.bank.config.controller;

import com.bank.config.service.impl.ConfigPushServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket事件处理器
 * 
 * @author bank
 */
@Controller
public class WebSocketEventHandler {

    @Autowired
    private ConfigPushServiceImpl configPushService;

    /**
     * 处理客户端连接
     */
    @MessageMapping("/connect")
    @SendToUser("/queue/connect-response")
    public Map<String, Object> handleConnect(Map<String, Object> message, SimpMessageHeaderAccessor headerAccessor) {
        String connectionId = headerAccessor.getSessionId();
        
        // 注册客户端连接
        Long appId = Long.valueOf(message.get("appId").toString());
        String instanceId = (String) message.get("instanceId");
        String instanceIp = (String) message.get("instanceIp");
        String clientVersion = (String) message.get("clientVersion");
        
        configPushService.registerClient(connectionId, appId, instanceId, instanceIp, clientVersion);
        
        // 返回连接成功响应
        Map<String, Object> response = new HashMap<>();
        response.put("type", "CONNECT_SUCCESS");
        response.put("connectionId", connectionId);
        response.put("message", "连接成功");
        
        return response;
    }

    /**
     * 处理客户端心跳
     */
    @MessageMapping("/heartbeat")
    @SendToUser("/queue/heartbeat-response")
    public Map<String, Object> handleHeartbeat(SimpMessageHeaderAccessor headerAccessor) {
        String connectionId = headerAccessor.getSessionId();
        
        // 更新客户端心跳
        configPushService.updateClientHeartbeat(connectionId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("type", "HEARTBEAT_SUCCESS");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 处理客户端断开连接
     */
    @MessageMapping("/disconnect")
    @SendTo("/topic/disconnect")
    public Map<String, Object> handleDisconnect(SimpMessageHeaderAccessor headerAccessor) {
        String connectionId = headerAccessor.getSessionId();
        
        // 移除客户端连接
        configPushService.removeClient(connectionId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("type", "DISCONNECT");
        response.put("connectionId", connectionId);
        response.put("message", "客户端断开连接");
        return response;
    }

    /**
     * 处理客户端消息
     */
    @MessageMapping("/message")
    @SendTo("/topic/messages")
    public Map<String, Object> handleMessage(Map<String, Object> message, SimpMessageHeaderAccessor headerAccessor) {
        String connectionId = headerAccessor.getSessionId();
        
        Map<String, Object> response = new HashMap<>();
        response.put("type", "MESSAGE");
        response.put("connectionId", connectionId);
        response.put("message", message.get("content"));
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
