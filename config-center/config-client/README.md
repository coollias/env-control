# Config Client

轻量级配置客户端，用于从配置中心拉取配置。

## 特性

- **轻量级**: 不依赖Spring Cloud，纯Java实现
- **多格式支持**: 支持Properties、YAML、JSON、XML等格式
- **本地缓存**: 支持本地文件缓存，提高性能
- **定时拉取**: 支持定时从配置中心拉取最新配置
- **安全认证**: 支持JWT Token认证
- **配置加密**: 支持敏感配置的加密解密
- **降级策略**: 支持配置获取失败时的降级处理
- **重试机制**: 支持网络异常时的重试
- **监控指标**: 提供详细的监控指标
- **健康检查**: 提供健康检查功能

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.bank.config</groupId>
    <artifactId>config-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 创建客户端

```java
// 使用构建器创建客户端
ConfigClient client = new ConfigClient.ConfigClientBuilder()
    .serverUrl("http://localhost:8080")
    .appCode("my-app")
    .envCode("prod")
    .token("your-token")
    .pollInterval(60000) // 1分钟拉取间隔
    .cacheFile("/tmp/config-cache.properties")
    .build();

// 初始化客户端
client.initialize();
```

### 3. 获取配置

```java
// 获取单个配置
String dbUrl = client.getConfig("database.url");
String apiKey = client.getConfig("api.key");

// 获取配置并指定默认值
String timeout = client.getConfig("timeout", "5000");

// 获取所有配置
Map<String, String> allConfigs = client.getAllConfigs();
```

### 4. 监听配置变更

```java
// 添加配置变更监听器
client.addConfigChangeListener(new ConfigChangeListener() {
    @Override
    public void onConfigChange(String key, String oldValue, String newValue) {
        System.out.println("配置变更: " + key + " = " + oldValue + " -> " + newValue);
    }
    
    @Override
    public void onConfigRefresh(Map<String, String> newConfigs) {
        System.out.println("配置刷新，共" + newConfigs.size() + "个配置项");
    }
});
```

## 配置选项

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| serverUrl | String | - | 配置中心服务器地址 |
| appCode | String | - | 应用代码 |
| envCode | String | - | 环境代码 |
| token | String | - | 认证Token |
| pollInterval | long | 30000 | 拉取间隔（毫秒） |
| cacheFile | String | - | 缓存文件路径 |
| enablePolling | boolean | true | 是否启用定时拉取 |
| enableCache | boolean | true | 是否启用缓存 |
| cacheExpireTime | long | 300000 | 缓存过期时间（毫秒） |

## 使用示例

### Spring Boot集成

```java
@Configuration
public class ConfigClientConfig {
    
    @Bean
    public ConfigClient configClient() {
        return new ConfigClient.ConfigClientBuilder()
            .serverUrl("http://config-server:8080")
            .appCode("my-app")
            .envCode("prod")
            .token("your-token")
            .pollInterval(60000)
            .cacheFile("/tmp/config-cache.properties")
            .build();
    }
}

@Component
public class MyService {
    private final ConfigClient configClient;
    
    public MyService(ConfigClient configClient) {
        this.configClient = configClient;
    }
    
    public void doSomething() {
        String apiUrl = configClient.getConfig("api.url");
        String apiKey = configClient.getConfig("api.key");
        // 使用配置...
    }
}
```

### 独立应用使用

```java
public class MyApplication {
    private static ConfigClient configClient;
    
    public static void main(String[] args) {
        // 初始化配置客户端
        configClient = new ConfigClient.ConfigClientBuilder()
            .serverUrl("http://config-server:8080")
            .appCode("my-app")
            .envCode("prod")
            .token("your-token")
            .build();
        
        configClient.initialize();
        
        // 启动应用
        startApplication();
    }
    
    public static String getConfig(String key) {
        return configClient.getConfig(key);
    }
}
```

## 监控和健康检查

### 获取监控指标

```java
Map<String, Object> metrics = client.getMetrics();
System.out.println("拉取成功率: " + metrics.get("pull.successRate"));
System.out.println("缓存命中率: " + metrics.get("cache.hitRate"));
```

### 健康检查

```java
boolean isHealthy = client.isHealthy();
if (!isHealthy) {
    System.out.println("客户端不健康");
}

// 获取详细健康状态
Map<String, Object> health = client.getHealthStatus();
```

## 配置格式支持

### Properties格式

```properties
database.url=jdbc:mysql://localhost:3306/test
database.username=root
database.password=123456
```

### YAML格式

```yaml
database:
  url: jdbc:mysql://localhost:3306/test
  username: root
  password: 123456
```

### JSON格式

```json
{
  "database": {
    "url": "jdbc:mysql://localhost:3306/test",
    "username": "root",
    "password": "123456"
  }
}
```

## 安全功能

### 配置加密

```java
// 加密配置值
String encrypted = security.encrypt("sensitive-value");

// 解密配置值
String decrypted = security.decrypt(encrypted);
```

### 认证头

客户端会自动添加以下认证头：
- `Authorization: Bearer <token>`
- `X-App-Code: <appCode>`
- `X-Env-Code: <envCode>`
- `X-Timestamp: <timestamp>`

## 错误处理

### 重试机制

客户端内置重试机制，默认配置：
- 最大重试次数：3次
- 重试延迟：1秒
- 延迟倍数：2.0
- 最大延迟：10秒

### 降级策略

当配置获取失败时，会按以下顺序尝试：
1. 从本地缓存获取
2. 从服务器重新拉取
3. 使用默认值

## 性能优化

### 缓存策略

- 支持本地文件缓存
- 支持内存缓存
- 可配置缓存过期时间
- 支持缓存预热

### 拉取策略

- 支持定时拉取
- 支持按需拉取
- 支持版本检查
- 支持增量更新

## 日志配置

客户端使用SLF4J进行日志记录，可以通过配置日志级别来控制输出：

```properties
# 设置日志级别
logging.level.com.bank.config.client=INFO
```

## 故障排除

### 常见问题

1. **连接失败**
   - 检查服务器地址是否正确
   - 检查网络连接
   - 检查认证Token是否有效

2. **配置获取失败**
   - 检查应用代码和环境代码是否正确
   - 检查配置项是否存在
   - 检查权限设置

3. **缓存问题**
   - 检查缓存文件路径是否有写权限
   - 检查缓存文件格式是否正确
   - 尝试清除缓存文件

### 调试模式

启用调试日志：

```properties
logging.level.com.bank.config.client=DEBUG
```

## 版本历史

- **1.0.0**: 初始版本，支持基本的配置拉取和缓存功能

## 许可证

MIT License 