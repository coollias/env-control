# 配置客户端 WebSocket 使用指南

## 概述

配置客户端现在支持WebSocket连接，可以实时接收服务器推送的配置更新，无需轮询。

## 功能特性

- **实时配置更新**: 通过WebSocket接收配置变更通知
- **自动重连**: 连接断开时自动重连
- **配置缓存**: 本地缓存配置数据，提高性能
- **监听器模式**: 支持配置变更监听器
- **心跳机制**: 定期发送心跳保持连接

## 使用方法

### 1. 创建WebSocket配置客户端

```java
ConfigClient client = new ConfigClient.ConfigClientBuilder()
    .serverUrl("http://localhost:8080")
    .appCode("myapp")
    .envCode("dev")
    .token("your-token-here")
    .enableWebSocket(true)           // 启用WebSocket
    .appId(1L)                      // 应用ID
    .instanceId("instance-001")     // 实例ID
    .instanceIp("192.168.1.100")    // 实例IP
    .clientVersion("1.0.0")         // 客户端版本
    .enablePolling(false)           // 禁用轮询，只使用WebSocket
    .enableCache(true)
    .cacheFile("config-cache.json")
    .build();
```

### 2. 添加配置变更监听器

```java
client.addConfigChangeListener(new ConfigChangeListener() {
    @Override
    public void onConfigChange(String key, String oldValue, String newValue) {
        System.out.println("配置变更: " + key + " = " + oldValue + " -> " + newValue);
    }
    
    @Override
    public void onConfigRefresh(Map<String, String> newConfigs) {
        System.out.println("配置刷新，共" + newConfigs.size() + "个配置项");
        newConfigs.forEach((key, value) -> {
            System.out.println("  " + key + " = " + value);
        });
    }
});
```

### 3. 启动客户端

```java
client.start();
```

### 4. 停止客户端

```java
client.stop();
```

## 配置选项

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `enableWebSocket` | boolean | 否 | false | 是否启用WebSocket |
| `appId` | Long | WebSocket启用时必填 | null | 应用ID |
| `instanceId` | String | WebSocket启用时必填 | null | 实例ID |
| `instanceIp` | String | 否 | null | 实例IP地址 |
| `clientVersion` | String | 否 | "1.0.0" | 客户端版本号 |

## WebSocket消息格式

### 配置更新消息

```json
{
  "type": "CONFIG_UPDATE",
  "appId": 1,
  "envId": 2,
  "configData": {
    "database": {
      "url": "jdbc:mysql://localhost:3306/myapp",
      "username": "root"
    },
    "redis": {
      "host": "localhost",
      "port": "6379"
    }
  },
  "timestamp": 1754619822886
}
```

### 配置变更通知

```json
{
  "type": "CONFIG_CHANGE_NOTIFICATION",
  "appId": 1,
  "envId": 2,
  "versionNumber": "v1.0.0",
  "changeType": "PUBLISH",
  "timestamp": 1754619822886
}
```

## 服务器端配置

确保服务器端已正确配置WebSocket：

1. **WebSocket配置类**: `WebSocketConfig.java`
2. **消息处理器**: `WebSocketEventHandler.java`
3. **推送服务**: `ConfigPushService.java`

## 示例代码

完整示例请参考: `WebSocketConfigClientExample.java`

## 注意事项

1. **网络连接**: 确保客户端能够访问服务器的WebSocket端口
2. **认证**: 客户端需要有效的认证token
3. **重连**: 网络异常时会自动重连，无需手动处理
4. **性能**: WebSocket连接比轮询更高效，减少服务器压力
5. **兼容性**: 可以同时启用WebSocket和轮询，作为双重保障

## 故障排除

### 连接失败
- 检查服务器地址和端口
- 确认网络连接正常
- 验证认证token是否有效

### 消息接收异常
- 检查监听器是否正确设置
- 确认消息格式是否符合预期
- 查看日志中的错误信息

### 性能问题
- 调整心跳间隔
- 检查网络延迟
- 监控内存使用情况 