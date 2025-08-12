package com.bank.config.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类
 * 启用WebSocket消息代理和配置消息路由
 * 
 * @author bank
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单的消息代理，用于向客户端发送消息
        config.enableSimpleBroker("/topic", "/queue");
        
        // 设置应用程序前缀，用于客户端发送消息到服务器
        config.setApplicationDestinationPrefixes("/app");
        
        // 设置用户目标前缀，用于向特定用户发送消息
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册STOMP端点，客户端通过这个端点连接WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // 允许所有来源
                .withSockJS();  // 启用SockJS支持
    }
}
