package com.bank.config.client.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.Map.*;

/**
 * WebSocket配置客户端
 * 用于接收服务器推送的配置更新
 * 
 * @author bank
 */
public class WebSocketConfigClient {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfigClient.class);
    
    private final String serverUrl;
    private final Long appId;
    private final String instanceId;
    private final String instanceIp;
    private final String clientVersion;
    
    private WebSocketStompClient stompClient;
    private StompSession session;
    private final ObjectMapper objectMapper;
    
    private ConfigUpdateListener configUpdateListener;
    private ConfigChangeNotificationListener notificationListener;
    
    public WebSocketConfigClient(String serverUrl, Long appId, String instanceId, String instanceIp, String clientVersion) {
        this.serverUrl = serverUrl;
        this.appId = appId;
        this.instanceId = instanceId;
        this.instanceIp = instanceIp;
        this.clientVersion = clientVersion;
        this.objectMapper = new ObjectMapper();
        // 注册Java 8日期时间模块
        this.objectMapper.registerModule(new JavaTimeModule());
        // 禁用将日期写为时间戳
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    
    /**
     * 连接到WebSocket服务器
     */
    public void connect() {
        try {
            // 创建WebSocket客户端
            List<Transport> transports = new ArrayList<>();
            transports.add(new WebSocketTransport(new StandardWebSocketClient()));
            
            SockJsClient sockJsClient = new SockJsClient(transports);
            stompClient = new WebSocketStompClient(sockJsClient);
            
            // 设置字符串消息转换器
            StringMessageConverter messageConverter = new StringMessageConverter();
            stompClient.setMessageConverter(messageConverter);
            
            // 连接到服务器
            String wsUrl = serverUrl.replace("http", "ws") + "/ws";
            logger.info("正在连接到WebSocket服务器: {}", wsUrl);
            
            session = stompClient.connect(wsUrl, new StompSessionHandler() {
                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    logger.info("WebSocket连接成功");
                    
                    // 订阅配置更新频道
                    subscribeToConfigUpdates(session);
                    
                    // 订阅配置变更通知频道
                    subscribeToNotifications(session);
                    
                    // 发送客户端注册信息
                    sendClientRegistration(session);
                }
                
                @Override
                public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                    logger.error("WebSocket异常: {}", exception.getMessage(), exception);
                }
                
                @Override
                public void handleTransportError(StompSession session, Throwable exception) {
                    logger.error("WebSocket传输错误: {}", exception.getMessage(), exception);
                }
                
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return String.class;
                }
                
                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    // 处理接收到的消息
                    try {
                        if (payload instanceof String) {
                            handleMessage((String) payload);
                        } else if (payload instanceof byte[]) {
                            String message = new String((byte[]) payload, StandardCharsets.UTF_8);
                            handleMessage(message);
                        } else {
                            // 尝试将payload转换为字符串
                            String message = objectMapper.writeValueAsString(payload);
                            handleMessage(message);
                        }
                    } catch (Exception e) {
                        logger.error("处理消息失败: {}", e.getMessage(), e);
                    }
                }
            }).get(10, TimeUnit.SECONDS);
            
            logger.info("WebSocket客户端启动成功");
            
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("WebSocket连接失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 订阅配置更新频道
     */
    private void subscribeToConfigUpdates(StompSession session) {
        // 订阅应用配置更新
        session.subscribe("/topic/app/" + appId + "/config", new org.springframework.messaging.simp.stomp.StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }
            
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                try {
                    if (payload instanceof String) {
                        handleConfigUpdate((String) payload);
                    } else if (payload instanceof byte[]) {
                        String message = new String((byte[]) payload, StandardCharsets.UTF_8);
                        handleConfigUpdate(message);
                    } else {
                        // 尝试将payload转换为字符串
                        String message = objectMapper.writeValueAsString(payload);
                        handleConfigUpdate(message);
                    }
                } catch (Exception e) {
                    logger.error("处理配置更新消息失败: {}", e.getMessage(), e);
                }
            }
        });
        
        // 订阅环境特定配置更新
        session.subscribe("/topic/app/" + appId + "/env/*/config", new org.springframework.messaging.simp.stomp.StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }
            
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                try {
                    if (payload instanceof String) {
                        handleConfigUpdate((String) payload);
                    } else if (payload instanceof byte[]) {
                        String message = new String((byte[]) payload, StandardCharsets.UTF_8);
                        handleConfigUpdate(message);
                    } else {
                        // 尝试将payload转换为字符串
                        String message = objectMapper.writeValueAsString(payload);
                        handleConfigUpdate(message);
                    }
                } catch (Exception e) {
                    logger.error("处理配置更新消息失败: {}", e.getMessage(), e);
                }
            }
        });
        
        logger.info("已订阅配置更新频道");
    }
    
    /**
     * 订阅配置变更通知频道
     */
    private void subscribeToNotifications(StompSession session) {
        session.subscribe("/topic/app/" + appId + "/notifications", new org.springframework.messaging.simp.stomp.StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }
            
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                try {
                    if (payload instanceof String) {
                        handleNotification((String) payload);
                    } else if (payload instanceof byte[]) {
                        String message = new String((byte[]) payload, StandardCharsets.UTF_8);
                        handleNotification(message);
                    } else {
                        // 尝试将payload转换为字符串
                        String message = objectMapper.writeValueAsString(payload);
                        handleNotification(message);
                    }
                } catch (Exception e) {
                    logger.error("处理通知消息失败: {}", e.getMessage(), e);
                }
            }
        });
        
        logger.info("已订阅配置变更通知频道");
    }
    
    /**
     * 发送客户端注册信息
     */
    private void sendClientRegistration(StompSession session) {
        try {
            Map<String, Object> registration = new HashMap<>();
            registration.put("type", "CLIENT_REGISTRATION");
            registration.put("appId", appId);
            registration.put("instanceId", instanceId);
            registration.put("instanceIp", instanceIp);
            registration.put("clientVersion", clientVersion);
            registration.put("timestamp", System.currentTimeMillis());
            
            String message = objectMapper.writeValueAsString(registration);
            // 转换为byte[]再发送
            session.send("/app/client/register", message.getBytes(StandardCharsets.UTF_8));
            
            logger.info("客户端注册信息已发送");
            
        } catch (Exception e) {
            logger.error("发送客户端注册信息失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理配置更新消息
     */
    private void handleConfigUpdate(String payload) {
        try {
            Map<String, Object> message = objectMapper.readValue(payload, Map.class);
            String type = (String) message.get("type");
            
            if ("CONFIG_UPDATE".equals(type)) {
                Long messageAppId = ((Number) message.get("appId")).longValue();
                Long messageEnvId = ((Number) message.get("envId")).longValue();
                Map<String, Object> configData = (Map<String, Object>) message.get("configData");
                
                logger.info("收到配置更新: appId={}, envId={}", messageAppId, messageEnvId);
                
                if (configUpdateListener != null) {
                    configUpdateListener.onConfigUpdate(messageAppId, messageEnvId, configData);
                }
            }
            
        } catch (Exception e) {
            logger.error("处理配置更新消息失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理配置变更通知消息
     */
    private void handleNotification(String payload) {
        try {
            Map<String, Object> message = objectMapper.readValue(payload, Map.class);
            String type = (String) message.get("type");
            
            if ("CONFIG_CHANGE_NOTIFICATION".equals(type)) {
                Long messageAppId = ((Number) message.get("appId")).longValue();
                Long messageEnvId = ((Number) message.get("envId")).longValue();
                String versionNumber = (String) message.get("versionNumber");
                String changeType = (String) message.get("changeType");
                
                logger.info("收到配置变更通知: appId={}, envId={}, version={}, type={}", 
                    messageAppId, messageEnvId, versionNumber, changeType);
                
                if (notificationListener != null) {
                    notificationListener.onConfigChangeNotification(messageAppId, messageEnvId, versionNumber, changeType);
                }
            }
            
        } catch (Exception e) {
            logger.error("处理配置变更通知失败: {}", e.getMessage(), e);
        }
    }
    

    /**
     * 处理接收到的消息
     */
    private void handleMessage(String payload) {
        try {
            Map<String, Object> message = objectMapper.readValue(payload, Map.class);
            String type = (String) message.get("type");
            
            switch (type) {
                case "CONFIG_UPDATE":
                    handleConfigUpdate(payload);
                    break;
                case "CONFIG_CHANGE_NOTIFICATION":
                    handleNotification(payload);
                    break;
                default:
                    logger.debug("收到未知类型消息: {}", type);
            }
            
        } catch (Exception e) {
            logger.error("处理消息失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 断开连接
     */
    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        if (stompClient != null) {
            stompClient.stop();
        }
        logger.info("WebSocket连接已断开");
    }
    
    /**
     * 发送心跳
     */
    public void sendHeartbeat() {
        if (session != null && session.isConnected()) {
            try {
                Map<String, Object> heartbeat = new HashMap<>();
                heartbeat.put("type", "HEARTBEAT");
                heartbeat.put("appId", appId);
                heartbeat.put("instanceId", instanceId);
                heartbeat.put("timestamp", System.currentTimeMillis());
                
                String message = objectMapper.writeValueAsString(heartbeat);
                // 转换为byte[]再发送
                session.send("/app/client/heartbeat", message.getBytes(StandardCharsets.UTF_8));
                
                logger.debug("心跳已发送");
                
            } catch (Exception e) {
                logger.error("发送心跳失败: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * 设置配置更新监听器
     */
    public void setConfigUpdateListener(ConfigUpdateListener listener) {
        this.configUpdateListener = listener;
    }
    
    /**
     * 设置配置变更通知监听器
     */
    public void setNotificationListener(ConfigChangeNotificationListener listener) {
        this.notificationListener = listener;
    }
    
    /**
     * 配置更新监听器接口
     */
    public interface ConfigUpdateListener {
        void onConfigUpdate(Long appId, Long envId, Map<String, Object> configData);
    }
    
    /**
     * 配置变更通知监听器接口
     */
    public interface ConfigChangeNotificationListener {
        void onConfigChangeNotification(Long appId, Long envId, String versionNumber, String changeType);
    }
}
