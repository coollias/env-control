-- 测试配置项版本管理功能
-- 这个脚本演示了配置项的版本管理功能

USE config_center;

-- 确保环境数据存在
INSERT INTO environments (env_code, env_name, env_desc, sort_order, status) VALUES
('dev', '开发环境', '开发测试环境', 1, 1),
('test', '测试环境', '功能测试环境', 2, 1)
ON DUPLICATE KEY UPDATE 
env_name = VALUES(env_name),
env_desc = VALUES(env_desc),
sort_order = VALUES(sort_order),
status = VALUES(status);

-- 确保应用数据存在
INSERT INTO applications (app_code, app_name, app_desc, owner, contact_email, status) VALUES
('test-app', '测试应用', '用于测试版本管理功能的应用', '管理员', 'admin@example.com', 1)
ON DUPLICATE KEY UPDATE 
app_name = VALUES(app_name),
app_desc = VALUES(app_desc),
owner = VALUES(owner),
contact_email = VALUES(contact_email),
status = VALUES(status);

-- 获取应用ID和环境ID
SET @app_id = (SELECT id FROM applications WHERE app_code = 'test-app');
SET @dev_env_id = (SELECT id FROM environments WHERE env_code = 'dev');
SET @test_env_id = (SELECT id FROM environments WHERE env_code = 'test');

-- 清理测试数据
DELETE FROM config_changes WHERE version_id IN (SELECT id FROM config_versions WHERE app_id = @app_id);
DELETE FROM config_versions WHERE app_id = @app_id;
DELETE FROM config_items WHERE app_id = @app_id;

-- 测试1: 创建配置项（应该自动生成版本）
INSERT INTO config_items (app_id, env_id, config_key, config_value, config_type, description, status) VALUES
(@app_id, @dev_env_id, 'server.port', '8080', 2, '服务端口', 1);

-- 手动创建对应的版本记录
INSERT INTO config_versions (app_id, env_id, version_number, version_name, version_desc, change_type, change_summary, created_by) VALUES
(@app_id, @dev_env_id, 'v1.0.0', '新增配置项: server.port', '新增配置项 server.port', 1, '新增了 1 个配置项', 'admin');

SET @version_id = LAST_INSERT_ID();

INSERT INTO config_changes (version_id, config_key, old_value, new_value, change_type) VALUES
(@version_id, 'server.port', NULL, '8080', 1);

-- 测试2: 修改配置项（应该自动生成版本）
UPDATE config_items SET config_value = '9090' WHERE app_id = @app_id AND env_id = @dev_env_id AND config_key = 'server.port';

-- 手动创建对应的版本记录
INSERT INTO config_versions (app_id, env_id, version_number, version_name, version_desc, change_type, change_summary, created_by) VALUES
(@app_id, @dev_env_id, 'v1.0.1', '修改配置项: server.port', '修改配置项 server.port，原值: 8080，新值: 9090', 2, '修改了 1 个配置项', 'admin');

SET @version_id = LAST_INSERT_ID();

INSERT INTO config_changes (version_id, config_key, old_value, new_value, change_type) VALUES
(@version_id, 'server.port', '8080', '9090', 2);

-- 测试3: 再次修改配置项
UPDATE config_items SET config_value = '7070' WHERE app_id = @app_id AND env_id = @dev_env_id AND config_key = 'server.port';

-- 手动创建对应的版本记录
INSERT INTO config_versions (app_id, env_id, version_number, version_name, version_desc, change_type, change_summary, created_by) VALUES
(@app_id, @dev_env_id, 'v1.0.2', '修改配置项: server.port', '修改配置项 server.port，原值: 9090，新值: 7070', 2, '修改了 1 个配置项', 'admin');

SET @version_id = LAST_INSERT_ID();

INSERT INTO config_changes (version_id, config_key, old_value, new_value, change_type) VALUES
(@version_id, 'server.port', '9090', '7070', 2);

-- 验证版本历史
SELECT 
    '版本历史验证' as test_name,
    cv.version_number,
    cv.version_name,
    cv.change_type,
    cc.config_key,
    cc.old_value,
    cc.new_value,
    cv.created_at
FROM config_versions cv
JOIN config_changes cc ON cv.id = cc.version_id
WHERE cv.app_id = @app_id AND cv.env_id = @dev_env_id
ORDER BY cv.created_at;

-- 验证当前配置
SELECT 
    '当前配置验证' as test_name,
    config_key,
    config_value,
    updated_at
FROM config_items 
WHERE app_id = @app_id AND env_id = @dev_env_id;

-- 清理测试数据
-- DELETE FROM config_changes WHERE version_id IN (SELECT id FROM config_versions WHERE app_id = @app_id);
-- DELETE FROM config_versions WHERE app_id = @app_id;
-- DELETE FROM config_items WHERE app_id = @app_id; 