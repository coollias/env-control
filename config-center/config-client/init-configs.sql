-- 配置中心配置项初始化脚本
-- 用于创建Spring Boot热更新示例所需的配置项

-- 注意：请根据你的实际数据库结构调整以下SQL语句

-- 1. 确保应用存在（如果不存在则创建）
INSERT INTO applications (app_code, app_name, description, status, created_at, updated_at) 
VALUES ('1003', 'Spring Boot Hot Update Example', 'Spring Boot热更新示例应用', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE 
    app_name = VALUES(app_name), 
    description = VALUES(description), 
    updated_at = NOW();

-- 2. 确保环境存在（如果不存在则创建）
INSERT INTO environments (env_code, env_name, description, status, created_at, updated_at) 
VALUES ('dev', '开发环境', '开发环境配置', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE 
    env_name = VALUES(env_name), 
    description = VALUES(description), 
    updated_at = NOW();

-- 3. 获取应用ID和环境ID
SET @app_id = (SELECT id FROM applications WHERE app_code = '1003');
SET @env_id = (SELECT id FROM environments WHERE env_code = 'dev');

-- 4. 创建配置项（如果不存在则创建）
INSERT INTO config_items (app_id, env_id, config_key, config_value, config_type, is_encrypted, is_required, status, created_at, updated_at) VALUES
-- 数据库配置
(@app_id, @env_id, 'spring.database.url', 'jdbc:mysql://localhost:3306/spring_example', 1, 0, 0, 1, NOW(), NOW()),
(@app_id, @env_id, 'spring.database.username', 'spring_user', 1, 0, 0, 1, NOW(), NOW()),
(@app_id, @env_id, 'spring.database.pool.maxConnections', '20', 2, 0, 0, 1, NOW(), NOW()),

-- 日志配置
(@app_id, @env_id, 'spring.logging.level', 'INFO', 1, 0, 0, 1, NOW(), NOW()),

-- 功能开关配置
(@app_id, @env_id, 'spring.feature.enableCache', 'true', 3, 0, 0, 1, NOW(), NOW()),
(@app_id, @env_id, 'spring.feature.enableMetrics', 'true', 3, 0, 0, 1, NOW(), NOW()),
(@app_id, @env_id, 'spring.feature.enableHealthCheck', 'true', 3, 0, 0, 1, NOW(), NOW()),

-- 应用配置
(@app_id, @env_id, 'spring.app.timeout', '30000', 2, 0, 0, 1, NOW(), NOW()),
(@app_id, @env_id, 'spring.app.secret', 'spring-example-secret-key-2024', 1, 0, 0, 1, NOW(), NOW()),

-- Redis配置
(@app_id, @env_id, 'redis.host', 'localhost', 1, 0, 0, 1, NOW(), NOW()),
(@app_id, @env_id, 'redis.port', '6379', 2, 0, 0, 1, NOW(), NOW()),

-- 刷新和通知配置
(@app_id, @env_id, 'app.refresh.interval', '5000', 2, 0, 0, 1, NOW(), NOW()),
(@app_id, @env_id, 'app.notification.enabled', 'true', 3, 0, 0, 1, NOW(), NOW())

ON DUPLICATE KEY UPDATE 
    config_value = VALUES(config_value),
    updated_at = NOW();

-- 5. 显示创建的配置项
SELECT 
    ci.config_key,
    ci.config_value,
    ci.config_type,
    ci.is_encrypted,
    ci.is_required,
    ci.status
FROM config_items ci
JOIN applications a ON ci.app_id = a.id
JOIN environments e ON ci.env_id = e.id
WHERE a.app_code = '1003' AND e.env_code = 'dev'
ORDER BY ci.config_key;
