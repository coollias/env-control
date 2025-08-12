# Config Client - 配置客户端

## 概述

配置客户端是一个轻量级的Java库，用于从配置中心拉取配置，并支持配置热更新功能。

## 功能特性

- **配置拉取**：支持HTTP和WebSocket两种方式获取配置
- **本地缓存**：支持本地文件缓存，提高性能
- **热更新**：配置变更时自动更新对象属性
- **注解支持**：使用`@ConfigValue`注解标记需要热更新的字段
- **类型转换**：自动类型转换和验证
- **故障恢复**：网络异常时自动重试和降级

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.bank.config</groupId>
    <artifactId>config-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 基本使用

```java
// 创建配置客户端
ConfigClient configClient = new ConfigClient.ConfigClientBuilder()
    .serverUrl("http://localhost:8080")
    .appCode("your-app")
    .envCode("dev")
    .token("your-token")
    .enableWebSocket(true)
    .appId(1L)
    .instanceId("instance-001")
    .build();

// 启动客户端
configClient.start();

// 获取配置
String value = configClient.getConfig("database.url");
```

### 3. 启用热更新

```java
public class MyService {
    
    @ConfigValue("database.url")
    private String databaseUrl;
    
    @ConfigValue("database.pool.maxConnections")
    private Integer maxConnections;
    
    @PostConstruct
    public void init() {
        // 启用热更新
        configClient.enableHotUpdate(this);
    }
}
```

## 运行示例

### 1. 普通Java应用示例

```bash
mvn exec:java -Dexec.mainClass="com.bank.config.client.example.HotUpdateExample"
```

### 2. Spring Boot集成示例

```bash
# 使用端口8081，避免与配置中心服务端冲突
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=8081"
```

### 3. 运行测试

```bash
mvn test
```

### 4. 使用演示脚本

```bash
chmod +x demo-hot-update.sh
./demo-hot-update.sh
```

## 配置说明

### 环境变量配置

可以通过环境变量或系统属性配置客户端：

```bash
# 配置中心服务地址
export config.server.url=http://localhost:8080

# 应用信息
export config.app.code=1003
export config.env.code=dev
export config.app.id=3

# 实例信息
export config.instance.id=my-instance
export config.instance.ip=127.0.0.1

# 认证令牌
export config.token=your-jwt-token
```

### 配置文件配置

Spring Boot应用可以通过`application.properties`配置：

```properties
# 服务器端口
server.port=8081

# 配置中心连接配置
config.server.url=http://localhost:8080
config.app.code=1003
config.env.code=dev
config.app.id=3
config.instance.id=spring-instance-001
config.instance.ip=127.0.0.1
```

## 热更新功能

### @ConfigValue注解

```java
@ConfigValue("database.url")                    // 直接指定配置键
private String databaseUrl;

@ConfigValue                                    // 使用字段名作为配置键
private String databaseUsername;

@ConfigValue(prefix = "redis.")                 // 使用前缀
private String redisHost;

@ConfigValue(value = "app.timeout", defaultValue = "30000")  // 带默认值
private Long timeout;

@ConfigValue(value = "app.secret", required = true)          // 必填配置
private String secret;
```

### 手动绑定

```java
// 绑定配置字段
configClient.bindConfigField("database.url", this, "databaseUrl");

// 绑定配置方法
configClient.bindConfigMethod("app.timeout", this, "setTimeout", Long.class);
```

## 注意事项

1. **端口冲突**：Spring Boot示例使用端口8081，避免与配置中心服务端（8080）冲突
2. **依赖版本**：确保Spring Framework版本与Spring Boot版本兼容
3. **网络连接**：确保能够连接到配置中心服务
4. **认证令牌**：使用有效的JWT令牌进行认证

## 故障排除

### 常见问题

1. **端口被占用**
   - 修改`application.properties`中的`server.port`
   - 或使用`-Dserver.port=8082`启动参数

2. **Spring Boot版本不兼容**
   - 检查Spring Framework版本（需要5.3.x）
   - 重新编译项目：`mvn clean compile`

3. **配置中心连接失败**
   - 检查配置中心服务是否启动
   - 验证网络连接和防火墙设置
   - 确认认证令牌是否有效

## 更多信息

- [热更新功能指南](HOT_UPDATE_GUIDE.md)
- [功能实现总结](HOT_UPDATE_SUMMARY.md)
- [配置中心服务端](../config-server/README.md) 