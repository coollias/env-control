# 环境级配置覆盖功能说明

## 功能概述

环境级配置覆盖功能允许配置按照环境的排序顺序进行继承和覆盖。排序越大的环境可以继承前面环境的配置，并且可以更新或增加自己环境下的配置。

## 工作原理

1. **环境排序**：环境按照 `sort_order` 字段进行排序
2. **配置继承**：高排序的环境会继承低排序环境的所有配置
3. **配置覆盖**：如果高排序环境中存在相同的配置键，会覆盖低排序环境的值
4. **配置合并**：最终配置是多个环境配置的合并结果

## 示例场景

假设有以下环境配置：

### 开发环境 (sort_order = 1)
```yaml
server:
  port: 8080
database:
  url: jdbc:mysql://localhost:3306/dev
  username: dev_user
logging:
  level: DEBUG
```

### 测试环境 (sort_order = 2)
```yaml
database:
  url: jdbc:mysql://test-server:3306/test
  username: test_user
logging:
  level: INFO
```

### 生产环境 (sort_order = 3)
```yaml
server:
  port: 80
database:
  url: jdbc:mysql://prod-server:3306/prod
  username: prod_user
  password: prod_password
logging:
  level: WARN
```

### 最终配置结果

**开发环境最终配置**：
```yaml
server:
  port: 8080
database:
  url: jdbc:mysql://localhost:3306/dev
  username: dev_user
logging:
  level: DEBUG
```

**测试环境最终配置**：
```yaml
server:
  port: 8080  # 继承自开发环境
database:
  url: jdbc:mysql://test-server:3306/test  # 覆盖开发环境
  username: test_user  # 覆盖开发环境
logging:
  level: INFO  # 覆盖开发环境
```

**生产环境最终配置**：
```yaml
server:
  port: 80  # 覆盖开发环境
database:
  url: jdbc:mysql://prod-server:3306/prod  # 覆盖测试环境
  username: prod_user  # 覆盖测试环境
  password: prod_password  # 新增配置
logging:
  level: WARN  # 覆盖测试环境
```

## API 接口

### 1. 获取合并后的配置列表
```
GET /api/config-items/app/{appId}/env/{envId}/merged
```

### 2. 获取合并后的配置映射
```
GET /api/config-items/app/{appId}/env/{envId}/merged-map
```

### 3. 获取指定配置键的继承值
```
GET /api/config-items/app/{appId}/env/{envId}/key/{configKey}/inherited
```

### 4. 获取环境继承链
```
GET /api/config-items/app/{appId}/env/{envId}/inheritance-chain
```

### 5. 获取跨环境配置差异
```
GET /api/config-items/app/{appId}/config-differences
```

## 前端使用

在前端界面中，用户可以通过以下方式使用此功能：

1. **应用环境页面**：查看应用在各个环境下的完整配置
2. **配置差异对比**：比较不同环境间的配置差异
3. **继承链显示**：查看当前环境的配置继承路径

## 配置建议

1. **环境排序**：建议按照环境的重要性和稳定性进行排序
   - 开发环境：sort_order = 1
   - 测试环境：sort_order = 2
   - 预生产环境：sort_order = 3
   - 生产环境：sort_order = 4

2. **配置管理**：
   - 在低排序环境中配置通用设置
   - 在高排序环境中配置环境特定设置
   - 避免在低排序环境中配置敏感信息

3. **最佳实践**：
   - 保持配置键的一致性
   - 使用有意义的配置键命名
   - 定期检查和清理无用配置

## 注意事项

1. **性能考虑**：继承链越长，查询性能可能受到影响
2. **数据一致性**：确保环境排序的正确性
3. **配置冲突**：注意避免配置键的命名冲突
4. **权限控制**：确保用户有相应环境的配置权限 