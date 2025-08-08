# 配置客户端测试项目

这是一个用于测试配置客户端的Spring Boot项目，提供了完整的API接口来验证配置客户端的功能。

## 项目结构

```
config-client-test/
├── src/main/java/com/bank/config/test/
│   ├── ConfigClientTestApplication.java    # 主启动类
│   ├── config/
│   │   └── ConfigClientConfig.java        # 配置客户端配置
│   ├── controller/
│   │   ├── ConfigTestController.java      # 配置测试控制器
│   │   └── BusinessController.java        # 业务测试控制器
│   └── service/
│       └── ConfigTestService.java         # 配置测试服务
├── src/main/resources/
│   └── application.yml                     # 应用配置
├── pom.xml                                # Maven配置
└── README.md                              # 项目说明
```

## 快速开始

### 1. 编译项目

```bash
mvn clean compile
```

### 2. 运行项目

```bash
mvn spring-boot:run
```

项目将在 `http://localhost:8081` 启动。

### 3. 确保配置中心运行

确保你的配置中心服务器在 `http://localhost:8080` 运行，并且有以下配置：

- 应用代码: `1003`
- 环境代码: `dev`
- 有效的JWT Token

## API接口

### 配置客户端测试接口

#### 1. 获取单个配置项
```
GET /api/config/{key}
```

示例：
```bash
curl http://localhost:8081/api/config/database.url
```

#### 2. 获取所有配置项
```
GET /api/config/all
```

示例：
```bash
curl http://localhost:8081/api/config/all
```

#### 3. 手动刷新配置
```
POST /api/config/refresh
```

示例：
```bash
curl -X POST http://localhost:8081/api/config/refresh
```

#### 4. 获取客户端健康状态
```
GET /api/config/health
```

示例：
```bash
curl http://localhost:8081/api/config/health
```

#### 5. 启动客户端
```
POST /api/config/start
```

示例：
```bash
curl -X POST http://localhost:8081/api/config/start
```

#### 6. 停止客户端
```
POST /api/config/stop
```

示例：
```bash
curl -X POST http://localhost:8081/api/config/stop
```

#### 7. 获取缓存信息
```
GET /api/config/cache
```

示例：
```bash
curl http://localhost:8081/api/config/cache
```

### 业务测试接口

#### 1. 获取业务配置
```
GET /api/business/config
```

#### 2. 获取数据库连接信息
```
GET /api/business/database
```

#### 3. 获取Redis连接信息
```
GET /api/business/redis
```

#### 4. 获取调试信息
```
GET /api/business/debug
```

#### 5. 动态获取配置
```
GET /api/business/dynamic/{key}
```

## 测试流程

### 1. 启动测试

1. 确保配置中心服务器运行在 `http://localhost:8080`
2. 启动测试项目：`mvn spring-boot:run`
3. 访问 `http://localhost:8081/api/config/health` 检查客户端状态

### 2. 使用Web界面测试（推荐）

```bash
# 在config-client-test目录下
./start-frontend.sh
```

或者直接在浏览器中打开 `frontend/index.html` 文件

Web界面提供以下功能：
- **客户端状态监控**: 实时显示客户端运行状态
- **操作控制**: 启动/停止客户端，刷新配置
- **配置展示**: 显示所有配置和业务配置
- **监控指标**: 显示各种监控数据
- **缓存信息**: 显示本地缓存状态
- **实时日志**: 显示操作日志和系统日志

### 3. 使用API接口测试

1. **启动客户端**：
   ```bash
   curl -X POST http://localhost:8081/api/config/start
   ```

2. **获取所有配置**：
   ```bash
   curl http://localhost:8081/api/config/all
   ```

3. **获取单个配置**：
   ```bash
   curl http://localhost:8081/api/config/database.url
   ```

4. **测试业务功能**：
   ```bash
   curl http://localhost:8081/api/business/config
   curl http://localhost:8081/api/business/database
   curl http://localhost:8081/api/business/redis
   ```

### 4. 配置热更新测试

1. 在配置中心修改某个配置项
2. 等待30秒（默认轮询间隔）或手动刷新：
   ```bash
   curl -X POST http://localhost:8081/api/config/refresh
   ```
3. 检查配置是否已更新：
   ```bash
   curl http://localhost:8081/api/config/all
   ```

### 5. 监控指标测试

```bash
curl http://localhost:8081/api/config/health
```

查看返回的metrics信息，包括：
- 拉取次数
- 成功/失败次数
- 缓存命中率
- 平均延迟等

## 日志查看

项目启动后，可以在控制台看到详细的日志信息，包括：
- 配置客户端初始化过程
- 配置拉取和更新过程
- 配置变更监听器触发
- 业务配置更新过程

## 故障排除

### 1. 连接失败

如果看到连接错误，请检查：
- 配置中心服务器是否运行在 `http://localhost:8080`
- 网络连接是否正常
- 防火墙设置

### 2. 认证失败

如果看到403错误，请检查：
- JWT Token是否有效
- 应用代码和环境代码是否正确
- 配置中心中的权限设置

### 3. 配置未找到

如果配置项返回null，请检查：
- 配置中心中是否存在该配置项
- 配置键是否完全匹配
- 应用和环境是否正确

## 扩展测试

你可以基于这个测试项目进行更多测试：

1. **性能测试**：测试大量配置项的拉取性能
2. **压力测试**：测试高并发下的配置获取
3. **故障恢复测试**：测试网络中断后的恢复能力
4. **缓存测试**：测试缓存机制的有效性

这个测试项目提供了完整的API接口，可以帮助你全面验证配置客户端的功能和性能。 