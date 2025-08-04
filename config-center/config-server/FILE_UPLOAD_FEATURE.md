# 配置文件上传功能

## 功能概述

配置中心现在支持通过上传配置文件的方式批量导入配置项。支持的文件格式包括：

- **JSON** (.json)
- **YAML** (.yaml/.yml) 
- **XML** (.xml)
- **Properties** (.properties)

## 使用方法

### 1. 前端使用

1. 在配置管理页面点击"导入配置"按钮
2. 选择应用和环境
3. 拖拽或点击上传配置文件
4. 点击"开始导入"按钮

### 2. 后端API

#### 上传配置文件
```
POST /api/config-items/upload
Content-Type: multipart/form-data

参数:
- file: 配置文件
- appId: 应用ID
- envId: 环境ID
```

#### 获取支持的文件格式
```
GET /api/config-items/supported-formats
```

## 文件格式说明

### JSON格式
```json
{
  "database": {
    "url": "jdbc:mysql://localhost:3306/config_center",
    "username": "root",
    "password": "password"
  },
  "redis": {
    "host": "localhost",
    "port": 6379
  }
}
```

### YAML格式
```yaml
database:
  url: jdbc:mysql://localhost:3306/config_center
  username: root
  password: password
redis:
  host: localhost
  port: 6379
```

### Properties格式
```properties
database.url=jdbc:mysql://localhost:3306/config_center
database.username=root
database.password=password
redis.host=localhost
redis.port=6379
```

### XML格式
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <database>
    <url>jdbc:mysql://localhost:3306/config_center</url>
    <username>root</username>
    <password>password</password>
  </database>
  <redis>
    <host>localhost</host>
    <port>6379</port>
  </redis>
</configuration>
```

## 解析规则

1. **嵌套结构**: 系统会自动将嵌套的配置结构转换为扁平化的键值对
   - 例如: `database.pool.maxConnections` = `20`

2. **数组处理**: 数组类型会被转换为JSON字符串存储
   - 例如: `security.cors.allowedOrigins` = `["http://localhost:3000", "https://yourdomain.com"]`

3. **数据类型**: 
   - 基本类型(字符串、数字、布尔)存储为字符串
   - 复杂类型(对象、数组)存储为JSON字符串

## 示例文件

在 `src/main/resources/` 目录下提供了示例配置文件：

- `example-config.json` - JSON格式示例
- `example-config.yaml` - YAML格式示例  
- `example-config.properties` - Properties格式示例

## 注意事项

1. 文件大小限制：最大10MB
2. 支持的文件扩展名：.json, .yaml, .yml, .xml, .properties
3. 导入的配置项会自动设置描述为"从配置文件导入"
4. 如果配置键已存在，会创建新的配置项（不会覆盖现有配置）
5. 所有导入的配置项默认状态为"启用"

## 错误处理

- 文件格式不支持时会显示相应错误信息
- 文件解析失败时会显示具体的错误原因
- 网络错误或服务器错误时会显示通用错误信息 