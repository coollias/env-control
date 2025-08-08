# 配置快照系统使用指南

## 概述

配置快照系统是一个用于管理应用配置版本和推送的功能模块。它提供了暂存、发布、版本管理、WebSocket推送等核心功能。

## 核心功能

### 1. 暂存功能
- 前端点击暂存按钮时，将当前环境的全部配置信息保存到数据库
- 自动生成版本号（如：v1.0.0, v1.0.1）
- 支持配置数据的JSON格式存储

### 2. 发布功能
- 点击发布时通过WebSocket推送配置到客户端
- 支持全量推送和增量推送
- 实时通知客户端配置变更

### 3. 版本管理
- 支持版本比较和差异查看
- 支持版本回滚功能
- 提供版本历史记录

## 数据库设计

### 主要表结构

#### 1. config_snapshots（配置快照表）
```sql
CREATE TABLE config_snapshots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    app_id BIGINT NOT NULL,
    env_id BIGINT NOT NULL,
    snapshot_name VARCHAR(128) NOT NULL,
    snapshot_desc TEXT,
    version_number VARCHAR(32) NOT NULL,
    snapshot_type TINYINT DEFAULT 1, -- 1-暂存，2-发布
    status TINYINT DEFAULT 1,
    config_data LONGTEXT NOT NULL, -- JSON格式的完整配置数据
    config_count INT DEFAULT 0,
    created_by VARCHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 2. config_snapshot_items（配置快照详情表）
```sql
CREATE TABLE config_snapshot_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    snapshot_id BIGINT NOT NULL,
    config_key VARCHAR(128) NOT NULL,
    config_value LONGTEXT,
    config_type TINYINT DEFAULT 1,
    is_encrypted TINYINT DEFAULT 0,
    is_required TINYINT DEFAULT 0,
    default_value LONGTEXT,
    description TEXT,
    group_id BIGINT DEFAULT 0,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 3. config_publish_records（配置发布记录表）
```sql
CREATE TABLE config_publish_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    app_id BIGINT NOT NULL,
    env_id BIGINT NOT NULL,
    snapshot_id BIGINT NOT NULL,
    version_number VARCHAR(32) NOT NULL,
    publish_type TINYINT DEFAULT 1, -- 1-立即发布，2-定时发布，3-灰度发布
    publish_status TINYINT DEFAULT 0, -- 0-待发布，1-发布中，2-成功，3-失败
    target_instances TEXT, -- JSON格式的目标实例列表
    success_count INT DEFAULT 0,
    fail_count INT DEFAULT 0,
    error_message TEXT,
    published_by VARCHAR(64) NOT NULL,
    published_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## API接口

### 配置快照API

#### 1. 创建暂存快照
```http
POST /api/config-snapshots/staged
Content-Type: application/json

{
    "appId": 1,
    "envId": 1,
    "snapshotName": "开发环境配置暂存",
    "snapshotDesc": "开发环境的配置暂存版本",
    "configData": {
        "server.port": "8080",
        "database.url": "jdbc:mysql://localhost:3306/test",
        "redis.host": "localhost",
        "redis.port": "6379"
    },
    "createdBy": "admin"
}
```

#### 2. 发布快照
```http
POST /api/config-snapshots/{snapshotId}/publish
Content-Type: application/json

{
    "publishedBy": "admin"
}
```

#### 3. 获取快照列表
```http
GET /api/config-snapshots/app/{appId}/env/{envId}
```

#### 4. 比较快照差异
```http
GET /api/config-snapshots/compare?snapshotId1=1&snapshotId2=2
```

#### 5. 回滚到指定快照
```http
POST /api/config-snapshots/app/{appId}/env/{envId}/rollback/{snapshotId}
Content-Type: application/json

{
    "createdBy": "admin"
}
```

### 配置推送API

#### 1. 推送配置到应用
```http
POST /api/config-push/app/{appId}/env/{envId}/push
Content-Type: application/json

{
    "configData": {
        "server.port": "8080",
        "database.url": "jdbc:mysql://localhost:3306/test"
    }
}
```

#### 2. 推送快照配置
```http
POST /api/config-push/snapshot/{snapshotId}/push
Content-Type: application/json

{
    "targetInstances": ["instance1", "instance2"]
}
```

#### 3. 获取在线客户端
```http
GET /api/config-push/app/{appId}/clients
```

## WebSocket支持

### 连接端点
```
ws://localhost:8080/ws
```

### 订阅主题
- `/topic/app/{appId}/config` - 应用配置更新
- `/topic/app/{appId}/env/{envId}/config` - 环境配置更新
- `/topic/app/{appId}/notifications` - 配置变更通知
- `/topic/instance/{instanceId}/config` - 实例配置更新

### 消息格式
```json
{
    "type": "CONFIG_UPDATE",
    "appId": 1,
    "envId": 1,
    "configData": {
        "server.port": "8080",
        "database.url": "jdbc:mysql://localhost:3306/test"
    },
    "timestamp": 1640995200000
}
```

## 使用示例

### Java代码示例

```java
@Autowired
private ConfigSnapshotService configSnapshotService;

@Autowired
private ConfigPushService configPushService;

// 1. 创建暂存快照
Map<String, Object> configData = new HashMap<>();
configData.put("server.port", "8080");
configData.put("database.url", "jdbc:mysql://localhost:3306/test");

ConfigSnapshot snapshot = configSnapshotService.createSnapshot(
    1L, 1L, "开发环境配置", "开发环境配置描述", configData, "admin"
);

// 2. 发布快照
ConfigSnapshot publishedSnapshot = configSnapshotService.publishSnapshot(
    snapshot.getId(), "admin"
);

// 3. 推送配置
Map<String, Object> configData = configSnapshotService.getSnapshotConfigData(publishedSnapshot.getId());
configPushService.pushConfigToApp(1L, 1L, configData);
```

### 前端代码示例

```javascript
import { configSnapshotApi, configPushApi } from '@/api'

// 1. 创建暂存快照
const createStagedSnapshot = async () => {
    const data = {
        appId: 1,
        envId: 1,
        snapshotName: '开发环境配置暂存',
        snapshotDesc: '开发环境的配置暂存版本',
        configData: {
            'server.port': '8080',
            'database.url': 'jdbc:mysql://localhost:3306/test'
        },
        createdBy: 'admin'
    }
    
    const response = await configSnapshotApi.createStagedSnapshot(data)
    console.log('暂存成功:', response.data)
}

// 2. 发布快照
const publishSnapshot = async (snapshotId) => {
    const data = { publishedBy: 'admin' }
    const response = await configSnapshotApi.publishSnapshot(snapshotId, data)
    console.log('发布成功:', response.data)
}

// 3. 推送配置
const pushSnapshotConfig = async (snapshotId) => {
    const data = { targetInstances: ['instance1', 'instance2'] }
    const response = await configPushApi.pushSnapshotConfig(snapshotId, data)
    console.log('推送成功:', response.data)
}
```

## 工作流程

### 1. 暂存流程
1. 前端用户编辑配置
2. 点击暂存按钮
3. 调用创建暂存快照API
4. 系统生成版本号并保存配置
5. 返回暂存成功信息

### 2. 发布流程
1. 选择要发布的暂存快照
2. 点击发布按钮
3. 调用发布快照API
4. 系统创建发布快照
5. 通过WebSocket推送配置到客户端
6. 返回发布成功信息

### 3. 客户端接收流程
1. 客户端建立WebSocket连接
2. 订阅配置更新主题
3. 接收配置更新消息
4. 更新本地配置
5. 重启应用或热加载配置

## 注意事项

1. **版本号管理**: 系统自动生成版本号，格式为v1.0.0、v1.0.1等
2. **数据一致性**: 发布时会创建新的快照，确保数据一致性
3. **推送可靠性**: 支持重试机制和错误处理
4. **安全性**: 支持配置加密和权限控制
5. **性能**: 使用缓存优化查询性能

## 扩展功能

1. **灰度发布**: 支持按比例推送配置到部分客户端
2. **定时发布**: 支持设置定时发布任务
3. **配置验证**: 支持配置格式和内容验证
4. **回滚机制**: 支持快速回滚到历史版本
5. **监控告警**: 支持推送状态监控和异常告警
