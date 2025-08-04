# 用户权限隔离系统

## 系统概述

为了确保配置中心的安全性，我们实现了完整的用户权限隔离系统。每个用户只能看到和操作自己有权限的应用和配置。

## 权限模型

### 1. 用户角色
- **ADMIN**: 管理员，可以管理所有应用和用户
- **USER**: 普通用户，只能访问被授权的应用

### 2. 权限类型
- **READ**: 只读权限，可以查看配置但不能修改
- **WRITE**: 读写权限，可以查看和修改配置
- **ADMIN**: 管理权限，可以管理应用和配置

### 3. 权限隔离规则

#### 应用隔离
- 用户只能看到自己有权限的应用
- 创建应用时自动为创建者分配ADMIN权限
- 管理员可以为用户分配应用权限

#### 配置隔离
- 用户只能看到自己有权限应用的配置
- 配置项与应用绑定，通过应用权限控制访问

#### 环境隔离
- 环境是全局的，但配置按应用隔离
- 不同应用在同一环境下的配置相互隔离

## 数据库表结构

### users 表
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    email VARCHAR(128),
    real_name VARCHAR(64),
    phone VARCHAR(32),
    role VARCHAR(32) NOT NULL DEFAULT 'USER',
    status TINYINT NOT NULL DEFAULT 1,
    last_login_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### user_permissions 表
```sql
CREATE TABLE user_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    app_id BIGINT NOT NULL,
    permission_type VARCHAR(32) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_app (user_id, app_id)
);
```

### applications 表（新增字段）
```sql
ALTER TABLE applications ADD COLUMN created_by BIGINT NOT NULL COMMENT '创建者用户ID';
```

## API权限控制

### 1. 应用管理API
- `GET /api/applications` - 只返回用户有权限的应用
- `POST /api/applications` - 需要ADMIN权限或创建者权限
- `PUT /api/applications/{id}` - 需要ADMIN权限或创建者权限
- `DELETE /api/applications/{id}` - 需要ADMIN权限或创建者权限

### 2. 配置管理API
- `GET /api/config-items` - 只返回用户有权限应用的配置
- `POST /api/config-items` - 需要对应应用的WRITE或ADMIN权限
- `PUT /api/config-items/{id}` - 需要对应应用的WRITE或ADMIN权限
- `DELETE /api/config-items/{id}` - 需要对应应用的ADMIN权限

### 3. 权限管理API
- `GET /api/permissions` - 获取用户权限列表
- `POST /api/permissions` - 分配权限（需要ADMIN权限）
- `DELETE /api/permissions/{id}` - 撤销权限（需要ADMIN权限）

## 使用示例

### 1. 创建用户
```bash
POST /api/users
{
    "username": "developer1",
    "password": "password123",
    "email": "dev1@example.com",
    "realName": "张三",
    "role": "USER"
}
```

### 2. 分配应用权限
```bash
POST /api/permissions
{
    "userId": 1,
    "appId": 1,
    "permissionType": "WRITE"
}
```

### 3. 查看用户权限
```bash
GET /api/permissions?userId=1
```

## 安全建议

1. **密码加密**: 使用BCrypt等算法加密存储密码
2. **JWT认证**: 实现基于JWT的用户认证
3. **API限流**: 对敏感API实现访问频率限制
4. **审计日志**: 记录所有权限变更操作
5. **定期审查**: 定期审查用户权限，及时清理无效权限

## 后续扩展

1. **角色管理**: 支持自定义角色和权限组合
2. **组织架构**: 支持部门级别的权限管理
3. **权限继承**: 支持权限的继承和传递
4. **临时权限**: 支持临时权限的分配和过期
5. **权限审计**: 详细的权限使用审计日志 