# 配置中心服务端 (Config Server)

## 项目简介

这是微服务配置管理平台的服务端，提供配置的增删改查、版本控制、实时推送等功能。

## 技术栈

- **Spring Boot 2.7.18**: 主框架
- **Spring Data JPA**: 数据访问层
- **MySQL 8.0**: 数据库
- **Redis**: 缓存
- **Spring Security**: 安全认证
- **WebSocket**: 实时推送
- **JWT**: 令牌认证

## 项目结构

```
config-server/
├── src/main/java/com/bank/config/
│   ├── ConfigServerApplication.java          # 启动类
│   ├── common/                              # 通用类
│   │   ├── ApiResponse.java                 # API响应类
│   │   └── GlobalExceptionHandler.java      # 全局异常处理器
│   ├── config/                              # 配置类
│   │   ├── JpaConfig.java                   # JPA配置
│   │   └── WebConfig.java                   # Web配置
│   ├── controller/                          # 控制器层
│   │   ├── ApplicationController.java       # 应用管理
│   │   ├── EnvironmentController.java       # 环境管理
│   │   ├── ConfigItemController.java        # 配置项管理
│   │   ├── ConfigClientController.java      # 客户端API
│   │   └── HealthController.java            # 健康检查
│   ├── entity/                              # 实体类
│   │   ├── BaseEntity.java                  # 基础实体
│   │   ├── Application.java                 # 应用实体
│   │   ├── Environment.java                 # 环境实体
│   │   └── ConfigItem.java                  # 配置项实体
│   ├── repository/                          # 数据访问层
│   │   ├── ApplicationRepository.java       # 应用Repository
│   │   ├── EnvironmentRepository.java       # 环境Repository
│   │   └── ConfigItemRepository.java        # 配置项Repository
│   └── service/                             # 业务逻辑层
│       ├── ApplicationService.java          # 应用服务接口
│       ├── EnvironmentService.java          # 环境服务接口
│       ├── ConfigItemService.java           # 配置项服务接口
│       └── impl/                           # 服务实现
│           ├── ApplicationServiceImpl.java   # 应用服务实现
│           ├── EnvironmentServiceImpl.java   # 环境服务实现
│           └── ConfigItemServiceImpl.java    # 配置项服务实现
├── src/main/resources/
│   └── application.yml                      # 应用配置文件
└── pom.xml                                 # Maven配置文件
```

## 功能特性

### 1. 应用管理
- 创建、更新、删除应用
- 应用状态管理
- 应用编码唯一性校验
- 分页查询和模糊搜索

### 2. 环境管理
- 创建、更新、删除环境
- 环境状态管理
- 环境排序
- 环境编码唯一性校验

### 3. 配置项管理
- 创建、更新、删除配置项
- 配置项状态管理
- 配置类型支持（字符串、数字、布尔、JSON、YAML、Properties）
- 配置加密标记
- 批量操作支持
- 按应用和环境分组查询

### 4. 客户端API
- 获取单个配置项
- 获取应用所有配置
- 配置项详情查询
- 配置项存在性检查
- 配置项数量统计

## API接口

### 应用管理接口

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/applications` | 创建应用 |
| PUT | `/api/applications/{id}` | 更新应用 |
| DELETE | `/api/applications/{id}` | 删除应用 |
| GET | `/api/applications/{id}` | 获取应用详情 |
| GET | `/api/applications` | 分页查询应用 |
| GET | `/api/applications/enabled` | 获取所有启用的应用 |
| PUT | `/api/applications/{id}/status` | 更新应用状态 |

### 环境管理接口

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/environments` | 创建环境 |
| PUT | `/api/environments/{id}` | 更新环境 |
| DELETE | `/api/environments/{id}` | 删除环境 |
| GET | `/api/environments/{id}` | 获取环境详情 |
| GET | `/api/environments` | 分页查询环境 |
| GET | `/api/environments/enabled` | 获取所有启用的环境 |
| PUT | `/api/environments/{id}/status` | 更新环境状态 |

### 配置项管理接口

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/config-items` | 创建配置项 |
| PUT | `/api/config-items/{id}` | 更新配置项 |
| DELETE | `/api/config-items/{id}` | 删除配置项 |
| GET | `/api/config-items/{id}` | 获取配置项详情 |
| GET | `/api/config-items` | 分页查询配置项 |
| POST | `/api/config-items/batch` | 批量创建配置项 |
| PUT | `/api/config-items/batch` | 批量更新配置项 |

### 客户端API接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/client/config/{appCode}/{envCode}/{configKey}` | 获取单个配置项 |
| GET | `/api/client/configs/{appCode}/{envCode}` | 获取应用所有配置 |
| GET | `/api/client/config-detail/{appCode}/{envCode}/{configKey}` | 获取配置项详情 |
| GET | `/api/client/config-exists/{appCode}/{envCode}/{configKey}` | 检查配置项是否存在 |
| GET | `/api/client/config-count/{appCode}/{envCode}` | 获取配置项数量 |

### 健康检查接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/health` | 健康检查 |
| GET | `/api/health/test` | 简单测试 |

## 快速开始

### 1. 环境准备

- JDK 8+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 2. 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE config_center CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行数据库脚本：`database_design.sql`

### 3. 配置文件

修改 `src/main/resources/application.yml` 中的数据库和Redis配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/config_center
    username: your_username
    password: your_password
  redis:
    host: localhost
    port: 6379
```

### 4. 启动应用

```bash
# 编译项目
mvn clean compile

# 运行项目
mvn spring-boot:run
```

### 5. 验证启动

访问 `http://localhost:8080/api/health` 查看服务状态。

## 测试示例

### 创建应用

```bash
curl -X POST http://localhost:8080/api/applications \
  -H "Content-Type: application/json" \
  -d '{
    "appCode": "user-service",
    "appName": "用户服务",
    "appDesc": "用户管理微服务",
    "owner": "张三",
    "contactEmail": "zhangsan@example.com"
  }'
```

### 创建环境

```bash
curl -X POST http://localhost:8080/api/environments \
  -H "Content-Type: application/json" \
  -d '{
    "envCode": "dev",
    "envName": "开发环境",
    "envDesc": "开发测试环境",
    "sortOrder": 1
  }'
```

### 创建配置项

```bash
curl -X POST http://localhost:8080/api/config-items \
  -H "Content-Type: application/json" \
  -d '{
    "appId": 1,
    "envId": 1,
    "configKey": "server.port",
    "configValue": "8080",
    "configType": 2,
    "description": "服务端口"
  }'
```

### 获取配置

```bash
curl http://localhost:8080/api/client/config/user-service/dev/server.port
```

## 开发计划

### 已完成功能
- [x] 基础CRUD操作
- [x] 应用管理
- [x] 环境管理
- [x] 配置项管理
- [x] 客户端API
- [x] 异常处理
- [x] 参数校验

### 待开发功能
- [ ] 用户认证和权限控制
- [ ] 配置版本控制
- [ ] WebSocket实时推送
- [ ] 配置加密功能
- [ ] 配置模板功能
- [ ] 灰度发布功能
- [ ] 监控和日志
- [ ] 缓存优化

## 注意事项

1. 确保数据库连接配置正确
2. 确保Redis服务正常运行
3. 生产环境需要配置适当的安全策略
4. 建议使用HTTPS协议
5. 定期备份数据库数据 