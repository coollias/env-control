# 配置热更新功能使用指南

## 概述

配置热更新功能允许应用程序在运行时自动更新配置值，无需重启应用。当配置中心发布配置变更时，客户端会自动接收更新并更新相关的对象属性。

## 核心组件

### 1. ConfigHotUpdateManager
配置热更新管理器，负责检测配置变更并自动更新相关的对象属性。

### 2. ConfigHotUpdateProcessor
配置热更新处理器，用于处理@ConfigValue注解标记的字段和方法。

### 3. @ConfigValue注解
用于标记需要热更新的字段和方法。

## 使用方法

### 方法1：使用@ConfigValue注解（推荐）

#### 1.1 标记字段
```java
public class MyService {
    
    @ConfigValue("database.url")
    private String databaseUrl;
    
    @ConfigValue("database.pool.maxConnections")
    private Integer maxConnections;
    
    @ConfigValue("logging.level")
    private String logLevel;
    
    @ConfigValue(value = "app.timeout", defaultValue = "30000")
    private Long timeout;
    
    @ConfigValue(value = "app.secret", required = true)
    private String secret;
    
    // 使用前缀的配置
    @ConfigValue(prefix = "redis.")
    private String host;
    
    @ConfigValue(prefix = "redis.")
    private Integer port;
}
```

#### 1.2 标记方法
```java
public class MyService {
    
    @ConfigValue("app.refresh.interval")
    public void setRefreshInterval(Integer interval) {
        // 当配置变更时，此方法会被自动调用
        System.out.println("设置刷新间隔: " + interval);
        // 执行具体的业务逻辑
    }
    
    @ConfigValue("app.notification.enabled")
    public void setNotificationEnabled(Boolean enabled) {
        // 当配置变更时，此方法会被自动调用
        System.out.println("设置通知启用状态: " + enabled);
    }
}
```

#### 1.3 启用热更新
```java
@Autowired
private ConfigClient configClient;

@PostConstruct
public void init() {
    // 启用热更新，处理@ConfigValue注解
    configClient.enableHotUpdate(this);
}
```

### 方法2：手动绑定配置

#### 2.1 绑定字段
```java
public class MyService {
    
    private String databaseUrl;
    private Integer maxConnections;
    
    @PostConstruct
    public void init() {
        // 手动绑定配置字段
        configClient.bindConfigField("database.url", this, "databaseUrl");
        configClient.bindConfigField("database.pool.maxConnections", this, "maxConnections");
    }
}
```

#### 2.2 绑定方法
```java
public class MyService {
    
    @PostConstruct
    public void init() {
        // 手动绑定配置方法
        configClient.bindConfigMethod("app.timeout", this, "setTimeout", Long.class);
        configClient.bindConfigMethod("app.enabled", this, "setEnabled", Boolean.class);
    }
    
    public void setTimeout(Long timeout) {
        // 当配置变更时，此方法会被自动调用
        System.out.println("设置超时时间: " + timeout);
    }
    
    public void setEnabled(Boolean enabled) {
        // 当配置变更时，此方法会被自动调用
        System.out.println("设置启用状态: " + enabled);
    }
}
```

## 完整示例

### 1. 普通Java应用示例

```java
public class HotUpdateExample {
    
    @ConfigValue("database.url")
    private String databaseUrl;
    
    @ConfigValue("database.username")
    private String databaseUsername;
    
    @ConfigValue("app.feature.enableCache")
    private Boolean enableCache;
    
    public static void main(String[] args) {
        // 创建配置客户端
        ConfigClient configClient = new ConfigClient.ConfigClientBuilder()
            .serverUrl("http://localhost:8080")
            .appCode("example-app")
            .envCode("dev")
            .enableWebSocket(true)
            .appId(1L)
            .instanceId("instance-001")
            .build();
        
        // 启动客户端
        configClient.start();
        
        // 创建示例对象
        HotUpdateExample example = new HotUpdateExample();
        
        // 启用热更新
        configClient.enableHotUpdate(example);
        
        // 打印初始配置
        example.printCurrentConfigs();
        
        // 保持程序运行，观察配置变更
        Thread.sleep(60000);
        
        // 再次打印配置，查看是否有变更
        example.printCurrentConfigs();
        
        // 停止客户端
        configClient.stop();
    }
}
```

### 2. Spring Boot应用示例

```java
@SpringBootApplication
public class SpringHotUpdateExample {
    
    public static void main(String[] args) {
        SpringApplication.run(SpringHotUpdateExample.class, args);
    }
    
    @Bean
    public ConfigClient configClient() {
        ConfigClient configClient = new ConfigClient.ConfigClientBuilder()
            .serverUrl("http://localhost:8080")
            .appCode("spring-example-app")
            .envCode("dev")
            .enableWebSocket(true)
            .appId(1L)
            .instanceId("spring-instance-001")
            .build();
        
        configClient.start();
        return configClient;
    }
}

@Component
public class ConfigHotUpdateService implements CommandLineRunner {
    
    @Autowired
    private ConfigClient configClient;
    
    @ConfigValue("spring.database.url")
    private String databaseUrl;
    
    @ConfigValue("spring.feature.enableCache")
    private Boolean enableCache;
    
    @Override
    public void run(String... args) throws Exception {
        // 启用热更新
        configClient.enableHotUpdate(this);
        
        // 打印初始配置
        printCurrentConfigs();
    }
    
    public void printCurrentConfigs() {
        System.out.println("数据库URL: " + databaseUrl);
        System.out.println("启用缓存: " + enableCache);
    }
}
```

## 配置注解参数说明

### @ConfigValue注解参数

| 参数 | 说明 | 默认值 |
|------|------|--------|
| value | 配置键 | 字段名 |
| prefix | 配置键前缀 | "" |
| defaultValue | 默认值 | "" |
| required | 是否必填 | false |
| description | 配置描述 | "" |

### 配置键规则

1. **直接指定配置键**：`@ConfigValue("database.url")`
2. **使用字段名作为配置键**：`@ConfigValue` 或 `@ConfigValue()`
3. **使用前缀**：`@ConfigValue(prefix = "redis.")` 会生成 `redis.字段名` 的配置键

## 工作原理

### 1. 配置变更检测
- 通过WebSocket接收配置更新通知
- 定时检测配置变更（每秒检测一次）
- 比较新旧配置值，确定是否需要更新

### 2. 自动更新机制
- 使用反射机制访问和修改字段值
- 自动类型转换（String、Integer、Long、Double、Float、Boolean）
- 支持字段和方法的自动更新

### 3. 更新触发时机
- WebSocket配置更新时
- 定时检测发现配置变更时
- 手动调用refreshConfig()时

## 注意事项

### 1. 字段访问权限
- 使用@ConfigValue注解的字段必须是可访问的（public或package-private）
- 私有字段会自动设置setAccessible(true)

### 2. 类型转换
- 支持基本数据类型和包装类型的自动转换
- 不支持复杂对象类型的自动转换
- 转换失败时会记录错误日志

### 3. 性能考虑
- 热更新检测每秒执行一次，对性能影响很小
- 建议只对真正需要热更新的配置项使用注解
- 大量配置项时，考虑使用手动绑定方式

### 4. 线程安全
- 热更新操作是线程安全的
- 但业务代码需要自行保证线程安全

## 故障排除

### 1. 配置未更新
- 检查@ConfigValue注解是否正确
- 确认配置键是否与配置中心一致
- 查看日志中是否有错误信息

### 2. 类型转换失败
- 检查配置值是否可以转换为目标类型
- 确认配置值的格式是否正确
- 查看日志中的类型转换错误

### 3. WebSocket连接问题
- 确认配置中心WebSocket服务是否正常
- 检查网络连接和防火墙设置
- 查看WebSocket连接日志

## 最佳实践

### 1. 配置分组
- 使用有意义的前缀组织配置
- 例如：`database.`、`redis.`、`app.feature.`

### 2. 默认值设置
- 为重要配置项设置合理的默认值
- 避免因配置缺失导致应用启动失败

### 3. 必填配置
- 只对真正必需的配置项设置required=true
- 其他配置项使用默认值或允许为空

### 4. 监控和日志
- 启用配置变更日志，便于问题排查
- 定期检查配置更新状态
- 监控热更新功能的运行状态

## 总结

配置热更新功能为应用程序提供了灵活的配置管理能力，通过简单的注解和API调用，就可以实现配置的自动更新。合理使用此功能，可以显著提高应用的灵活性和可维护性。
