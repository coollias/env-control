package com.bank.config.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.bank.config.service.ConfigPushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * WebSocket消息控制器
 * 处理客户端发送的WebSocket消息
 * 
 * @author bank
 */
@Controller
public class WebSocketMessageController {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketMessageController.class);
    
    @Autowired
    private ConfigPushService configPushService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 处理客户端注册消息
     * 客户端发送消息到 /app/client/register
     */
    @MessageMapping("/client/register")
    public void handleClientRegistration(@Payload String message, SimpMessageHeaderAccessor headerAccessor) {
        try {
            logger.info("收到客户端注册消息: {}", message);
            
            // 解析注册消息
            Map<String, Object> registration = objectMapper.readValue(message, Map.class);
            String type = (String) registration.get("type");
            
            if ("CLIENT_REGISTRATION".equals(type)) {
                // 获取连接ID
                String connectionId = headerAccessor.getSessionId();
                
                // 获取注册信息
                Long appId = ((Number) registration.get("appId")).longValue();
                String instanceId = (String) registration.get("instanceId");
                String instanceIp = (String) registration.get("instanceIp");
                String clientVersion = (String) registration.get("clientVersion");
                
                logger.info("注册客户端: connectionId={}, appId={}, instanceId={}, instanceIp={}, version={}", 
                    connectionId, appId, instanceId, instanceIp, clientVersion);
                
                // 注册客户端到推送服务
                configPushService.registerClient(connectionId, appId, instanceId, instanceIp, clientVersion);
                
                logger.info("客户端注册成功: {}", instanceId);
            }
            
        } catch (Exception e) {
            logger.error("处理客户端注册消息失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理客户端心跳消息
     * 客户端发送消息到 /app/client/heartbeat
     */
    @MessageMapping("/client/heartbeat")
    public void handleClientHeartbeat(@Payload String message, SimpMessageHeaderAccessor headerAccessor) {
        try {
            String connectionId = headerAccessor.getSessionId();
            logger.debug("收到客户端心跳: connectionId={}", connectionId);
            
            // 更新客户端心跳时间
            configPushService.updateClientHeartbeat(connectionId);
            
        } catch (Exception e) {
            logger.error("处理客户端心跳消息失败: {}", e.getMessage(), e);
        }
    }
}
