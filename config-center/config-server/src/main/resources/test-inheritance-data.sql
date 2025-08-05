-- 环境级配置覆盖功能测试数据
-- 这个脚本演示了环境继承和配置覆盖的工作原理

-- 确保环境数据存在
INSERT INTO environments (env_code, env_name, env_desc, sort_order, status) VALUES
('dev', '开发环境', '开发测试环境', 1, 1),
('test', '测试环境', '功能测试环境', 2, 1),
('prod', '生产环境', '生产运行环境', 3, 1)
ON DUPLICATE KEY UPDATE 
env_name = VALUES(env_name),
env_desc = VALUES(env_desc),
sort_order = VALUES(sort_order),
status = VALUES(status);

-- 确保应用数据存在
INSERT INTO applications (app_code, app_name, app_desc, owner, contact_email, status) VALUES
('demo-service', '演示服务', '用于演示环境继承功能的示例服务', '管理员', 'admin@example.com', 1)
ON DUPLICATE KEY UPDATE 
app_name = VALUES(app_name),
app_desc = VALUES(app_desc),
owner = VALUES(owner),
contact_email = VALUES(contact_email),
status = VALUES(status);

-- 获取应用ID和环境ID
SET @app_id = (SELECT id FROM applications WHERE app_code = 'demo-service');
SET @dev_env_id = (SELECT id FROM environments WHERE env_code = 'dev');
SET @test_env_id = (SELECT id FROM environments WHERE env_code = 'test');
SET @prod_env_id = (SELECT id FROM environments WHERE env_code = 'prod');

-- 开发环境配置（基础配置）
INSERT INTO config_items (app_id, env_id, config_key, config_value, config_type, description, status) VALUES
(@app_id, @dev_env_id, 'server.port', '8080', 2, '服务端口', 1),
(@app_id, @dev_env_id, 'server.host', 'localhost', 1, '服务主机', 1),
(@app_id, @dev_env_id, 'database.url', 'jdbc:mysql://localhost:3306/demo_dev', 1, '数据库连接URL', 1),
(@app_id, @dev_env_id, 'database.username', 'dev_user', 1, '数据库用户名', 1),
(@app_id, @dev_env_id, 'database.password', 'dev_password', 1, '数据库密码', 1),
(@app_id, @dev_env_id, 'redis.host', 'localhost', 1, 'Redis主机', 1),
(@app_id, @dev_env_id, 'redis.port', '6379', 2, 'Redis端口', 1),
(@app_id, @dev_env_id, 'logging.level', 'DEBUG', 1, '日志级别', 1),
(@app_id, @dev_env_id, 'logging.file', '/var/log/demo-dev.log', 1, '日志文件路径', 1),
(@app_id, @dev_env_id, 'feature.enableCache', 'true', 3, '是否启用缓存', 1),
(@app_id, @dev_env_id, 'feature.enableMetrics', 'true', 3, '是否启用监控', 1)
ON DUPLICATE KEY UPDATE 
config_value = VALUES(config_value),
description = VALUES(description),
status = VALUES(status);

-- 测试环境配置（覆盖部分配置）
INSERT INTO config_items (app_id, env_id, config_key, config_value, config_type, description, status) VALUES
(@app_id, @test_env_id, 'database.url', 'jdbc:mysql://test-server:3306/demo_test', 1, '数据库连接URL', 1),
(@app_id, @test_env_id, 'database.username', 'test_user', 1, '数据库用户名', 1),
(@app_id, @test_env_id, 'database.password', 'test_password', 1, '数据库密码', 1),
(@app_id, @test_env_id, 'redis.host', 'test-redis', 1, 'Redis主机', 1),
(@app_id, @test_env_id, 'logging.level', 'INFO', 1, '日志级别', 1),
(@app_id, @test_env_id, 'logging.file', '/var/log/demo-test.log', 1, '日志文件路径', 1),
(@app_id, @test_env_id, 'feature.enableCache', 'true', 3, '是否启用缓存', 1),
(@app_id, @test_env_id, 'feature.enableMetrics', 'false', 3, '是否启用监控', 1),
(@app_id, @test_env_id, 'test.mode', 'true', 3, '测试模式', 1)
ON DUPLICATE KEY UPDATE 
config_value = VALUES(config_value),
description = VALUES(description),
status = VALUES(status);

-- 生产环境配置（覆盖和新增配置）
INSERT INTO config_items (app_id, env_id, config_key, config_value, config_type, description, status) VALUES
(@app_id, @prod_env_id, 'server.port', '80', 2, '服务端口', 1),
(@app_id, @prod_env_id, 'server.host', '0.0.0.0', 1, '服务主机', 1),
(@app_id, @prod_env_id, 'database.url', 'jdbc:mysql://prod-server:3306/demo_prod', 1, '数据库连接URL', 1),
(@app_id, @prod_env_id, 'database.username', 'prod_user', 1, '数据库用户名', 1),
(@app_id, @prod_env_id, 'database.password', 'prod_password', 1, '数据库密码', 1),
(@app_id, @prod_env_id, 'redis.host', 'prod-redis', 1, 'Redis主机', 1),
(@app_id, @prod_env_id, 'redis.port', '6379', 2, 'Redis端口', 1),
(@app_id, @prod_env_id, 'logging.level', 'WARN', 1, '日志级别', 1),
(@app_id, @prod_env_id, 'logging.file', '/var/log/demo-prod.log', 1, '日志文件路径', 1),
(@app_id, @prod_env_id, 'feature.enableCache', 'true', 3, '是否启用缓存', 1),
(@app_id, @prod_env_id, 'feature.enableMetrics', 'true', 3, '是否启用监控', 1),
(@app_id, @prod_env_id, 'security.jwt.secret', 'prod-jwt-secret-key', 1, 'JWT密钥', 1),
(@app_id, @prod_env_id, 'security.jwt.expiration', '86400', 2, 'JWT过期时间', 1),
(@app_id, @prod_env_id, 'monitoring.health.check', 'true', 3, '健康检查', 1),
(@app_id, @prod_env_id, 'monitoring.metrics.port', '9090', 2, '监控端口', 1)
ON DUPLICATE KEY UPDATE 
config_value = VALUES(config_value),
description = VALUES(description),
status = VALUES(status);

-- 验证数据插入结果
SELECT 
    '开发环境配置数量' as description,
    COUNT(*) as count
FROM config_items 
WHERE app_id = @app_id AND env_id = @dev_env_id AND status = 1
UNION ALL
SELECT 
    '测试环境配置数量' as description,
    COUNT(*) as count
FROM config_items 
WHERE app_id = @app_id AND env_id = @test_env_id AND status = 1
UNION ALL
SELECT 
    '生产环境配置数量' as description,
    COUNT(*) as count
FROM config_items 
WHERE app_id = @app_id AND env_id = @prod_env_id AND status = 1;

-- 显示各环境的配置示例
SELECT 
    '开发环境配置示例' as env_name,
    config_key,
    config_value,
    CASE config_type 
        WHEN 1 THEN '字符串'
        WHEN 2 THEN '数字'
        WHEN 3 THEN '布尔'
        ELSE '其他'
    END as config_type
FROM config_items 
WHERE app_id = @app_id AND env_id = @dev_env_id AND status = 1
ORDER BY config_key
LIMIT 5; 