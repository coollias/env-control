-- 微服务配置管理平台数据库设计
-- 数据库名称: config_center
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_unicode_ci

-- 创建数据库
CREATE DATABASE IF NOT EXISTS config_center 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE config_center;

-- ==================== 应用管理相关表 ====================

-- 应用表
CREATE TABLE applications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '应用ID',
    app_code VARCHAR(64) NOT NULL UNIQUE COMMENT '应用编码',
    app_name VARCHAR(128) NOT NULL COMMENT '应用名称',
    app_desc TEXT COMMENT '应用描述',
    owner VARCHAR(64) COMMENT '负责人',
    contact_email VARCHAR(128) COMMENT '联系邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_app_code (app_code),
    INDEX idx_status (status)
) COMMENT '应用信息表';

-- 应用密钥表
CREATE TABLE app_secrets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '密钥ID',
    app_id BIGINT NOT NULL COMMENT '应用ID',
    secret_key VARCHAR(128) NOT NULL COMMENT '密钥',
    secret_type TINYINT DEFAULT 1 COMMENT '密钥类型：1-API密钥，2-JWT密钥',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    expires_at TIMESTAMP NULL COMMENT '过期时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (app_id) REFERENCES applications(id) ON DELETE CASCADE,
    INDEX idx_app_id (app_id),
    INDEX idx_secret_key (secret_key),
    INDEX idx_status (status)
) COMMENT '应用密钥表';

-- ==================== 环境管理相关表 ====================

-- 环境表
CREATE TABLE environments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '环境ID',
    env_code VARCHAR(32) NOT NULL UNIQUE COMMENT '环境编码',
    env_name VARCHAR(64) NOT NULL COMMENT '环境名称',
    env_desc TEXT COMMENT '环境描述',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_env_code (env_code),
    INDEX idx_status (status)
) COMMENT '环境表';

-- ==================== 配置管理相关表 ====================

-- 配置组表
CREATE TABLE config_groups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置组ID',
    app_id BIGINT NOT NULL COMMENT '应用ID',
    group_code VARCHAR(64) NOT NULL COMMENT '配置组编码',
    group_name VARCHAR(128) NOT NULL COMMENT '配置组名称',
    group_desc TEXT COMMENT '配置组描述',
    parent_id BIGINT DEFAULT 0 COMMENT '父级配置组ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (app_id) REFERENCES applications(id) ON DELETE CASCADE,
    INDEX idx_app_id (app_id),
    INDEX idx_group_code (group_code),
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status)
) COMMENT '配置组表';

-- 配置项表
CREATE TABLE config_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置项ID',
    app_id BIGINT NOT NULL COMMENT '应用ID',
    env_id BIGINT NOT NULL COMMENT '环境ID',
    group_id BIGINT DEFAULT 0 COMMENT '配置组ID',
    config_key VARCHAR(128) NOT NULL COMMENT '配置键',
    config_value LONGTEXT COMMENT '配置值',
    config_type TINYINT DEFAULT 1 COMMENT '配置类型：1-字符串，2-数字，3-布尔，4-JSON，5-YAML，6-Properties',
    is_encrypted TINYINT DEFAULT 0 COMMENT '是否加密：1-是，0-否',
    is_required TINYINT DEFAULT 0 COMMENT '是否必填：1-是，0-否',
    default_value LONGTEXT COMMENT '默认值',
    description TEXT COMMENT '配置描述',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (app_id) REFERENCES applications(id) ON DELETE CASCADE,
    FOREIGN KEY (env_id) REFERENCES environments(id) ON DELETE CASCADE,
    UNIQUE KEY uk_app_env_key (app_id, env_id, config_key),
    INDEX idx_app_id (app_id),
    INDEX idx_env_id (env_id),
    INDEX idx_group_id (group_id),
    INDEX idx_config_key (config_key),
    INDEX idx_status (status)
) COMMENT '配置项表';

-- ==================== 版本控制相关表 ====================

-- 配置版本表
CREATE TABLE config_versions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '版本ID',
    app_id BIGINT NOT NULL COMMENT '应用ID',
    env_id BIGINT NOT NULL COMMENT '环境ID',
    version_number VARCHAR(32) NOT NULL COMMENT '版本号',
    version_name VARCHAR(128) COMMENT '版本名称',
    version_desc TEXT COMMENT '版本描述',
    change_type TINYINT DEFAULT 1 COMMENT '变更类型：1-新增，2-修改，3-删除',
    change_summary TEXT COMMENT '变更摘要',
    created_by VARCHAR(64) NOT NULL COMMENT '创建人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (app_id) REFERENCES applications(id) ON DELETE CASCADE,
    FOREIGN KEY (env_id) REFERENCES environments(id) ON DELETE CASCADE,
    UNIQUE KEY uk_app_env_version (app_id, env_id, version_number),
    INDEX idx_app_id (app_id),
    INDEX idx_env_id (env_id),
    INDEX idx_version_number (version_number),
    INDEX idx_created_at (created_at)
) COMMENT '配置版本表';

-- 配置变更详情表
CREATE TABLE config_changes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '变更ID',
    version_id BIGINT NOT NULL COMMENT '版本ID',
    config_key VARCHAR(128) NOT NULL COMMENT '配置键',
    old_value LONGTEXT COMMENT '原值',
    new_value LONGTEXT COMMENT '新值',
    change_type TINYINT DEFAULT 1 COMMENT '变更类型：1-新增，2-修改，3-删除',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (version_id) REFERENCES config_versions(id) ON DELETE CASCADE,
    INDEX idx_version_id (version_id),
    INDEX idx_config_key (config_key),
    INDEX idx_change_type (change_type)
) COMMENT '配置变更详情表';

-- ==================== 权限管理相关表 ====================

-- 用户表
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(64) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(128) NOT NULL COMMENT '密码',
    real_name VARCHAR(64) COMMENT '真实姓名',
    email VARCHAR(128) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    last_login_at TIMESTAMP NULL COMMENT '最后登录时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_status (status)
) COMMENT '用户表';

-- 角色表
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_code VARCHAR(64) NOT NULL UNIQUE COMMENT '角色编码',
    role_name VARCHAR(128) NOT NULL COMMENT '角色名称',
    role_desc TEXT COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_role_code (role_code),
    INDEX idx_status (status)
) COMMENT '角色表';

-- 用户角色关联表
CREATE TABLE user_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) COMMENT '用户角色关联表';

-- 权限表
CREATE TABLE permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    perm_code VARCHAR(64) NOT NULL UNIQUE COMMENT '权限编码',
    perm_name VARCHAR(128) NOT NULL COMMENT '权限名称',
    perm_desc TEXT COMMENT '权限描述',
    resource_type VARCHAR(32) COMMENT '资源类型',
    resource_id BIGINT COMMENT '资源ID',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_perm_code (perm_code),
    INDEX idx_resource (resource_type, resource_id),
    INDEX idx_status (status)
) COMMENT '权限表';

-- 角色权限关联表
CREATE TABLE role_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) COMMENT '角色权限关联表';

-- ==================== 应用权限管理相关表 ====================

-- 应用权限表（用户对特定应用的权限）
CREATE TABLE app_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    app_id BIGINT NOT NULL COMMENT '应用ID',
    permission_type VARCHAR(32) NOT NULL COMMENT '权限类型：READ-只读，WRITE-读写，ADMIN-管理员',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (app_id) REFERENCES applications(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_app (user_id, app_id),
    INDEX idx_user_id (user_id),
    INDEX idx_app_id (app_id),
    INDEX idx_permission_type (permission_type),
    INDEX idx_status (status)
) COMMENT '应用权限表';

-- ==================== 推送管理相关表 ====================

-- 推送任务表
CREATE TABLE push_tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    app_id BIGINT NOT NULL COMMENT '应用ID',
    env_id BIGINT NOT NULL COMMENT '环境ID',
    version_id BIGINT NOT NULL COMMENT '版本ID',
    task_type TINYINT DEFAULT 1 COMMENT '任务类型：1-立即推送，2-定时推送，3-灰度推送',
    push_strategy TINYINT DEFAULT 1 COMMENT '推送策略：1-全量推送，2-增量推送',
    target_instances TEXT COMMENT '目标实例列表（JSON格式）',
    gray_ratio INT DEFAULT 100 COMMENT '灰度比例（0-100）',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待执行，1-执行中，2-成功，3-失败',
    start_time TIMESTAMP NULL COMMENT '开始时间',
    end_time TIMESTAMP NULL COMMENT '结束时间',
    success_count INT DEFAULT 0 COMMENT '成功数量',
    fail_count INT DEFAULT 0 COMMENT '失败数量',
    error_message TEXT COMMENT '错误信息',
    created_by VARCHAR(64) NOT NULL COMMENT '创建人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (app_id) REFERENCES applications(id) ON DELETE CASCADE,
    FOREIGN KEY (env_id) REFERENCES environments(id) ON DELETE CASCADE,
    FOREIGN KEY (version_id) REFERENCES config_versions(id) ON DELETE CASCADE,
    INDEX idx_app_id (app_id),
    INDEX idx_env_id (env_id),
    INDEX idx_version_id (version_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) COMMENT '推送任务表';

-- 推送详情表
CREATE TABLE push_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '详情ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    instance_id VARCHAR(128) NOT NULL COMMENT '实例ID',
    instance_ip VARCHAR(64) COMMENT '实例IP',
    push_status TINYINT DEFAULT 0 COMMENT '推送状态：0-待推送，1-推送中，2-成功，3-失败',
    push_time TIMESTAMP NULL COMMENT '推送时间',
    response_time INT COMMENT '响应时间（毫秒）',
    error_message TEXT COMMENT '错误信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (task_id) REFERENCES push_tasks(id) ON DELETE CASCADE,
    INDEX idx_task_id (task_id),
    INDEX idx_instance_id (instance_id),
    INDEX idx_push_status (push_status)
) COMMENT '推送详情表';

-- ==================== 客户端连接管理相关表 ====================

-- 客户端连接表
CREATE TABLE client_connections (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '连接ID',
    app_id BIGINT NOT NULL COMMENT '应用ID',
    instance_id VARCHAR(128) NOT NULL COMMENT '实例ID',
    instance_ip VARCHAR(64) COMMENT '实例IP',
    client_version VARCHAR(32) COMMENT '客户端版本',
    connection_id VARCHAR(128) COMMENT 'WebSocket连接ID',
    last_heartbeat TIMESTAMP NULL COMMENT '最后心跳时间',
    status TINYINT DEFAULT 1 COMMENT '状态：1-在线，0-离线',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (app_id) REFERENCES applications(id) ON DELETE CASCADE,
    INDEX idx_app_id (app_id),
    INDEX idx_instance_id (instance_id),
    INDEX idx_connection_id (connection_id),
    INDEX idx_status (status),
    INDEX idx_last_heartbeat (last_heartbeat)
) COMMENT '客户端连接表';

-- ==================== 监控统计相关表 ====================

-- 配置访问日志表
CREATE TABLE config_access_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    app_id BIGINT NOT NULL COMMENT '应用ID',
    instance_id VARCHAR(128) COMMENT '实例ID',
    config_key VARCHAR(128) COMMENT '配置键',
    access_type TINYINT DEFAULT 1 COMMENT '访问类型：1-读取，2-写入',
    response_time INT COMMENT '响应时间（毫秒）',
    success TINYINT DEFAULT 1 COMMENT '是否成功：1-成功，0-失败',
    error_message TEXT COMMENT '错误信息',
    client_ip VARCHAR(64) COMMENT '客户端IP',
    user_agent TEXT COMMENT '用户代理',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_app_id (app_id),
    INDEX idx_instance_id (instance_id),
    INDEX idx_config_key (config_key),
    INDEX idx_access_type (access_type),
    INDEX idx_created_at (created_at)
) COMMENT '配置访问日志表';

-- 系统操作日志表
CREATE TABLE operation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(64) COMMENT '用户名',
    operation_type VARCHAR(64) NOT NULL COMMENT '操作类型',
    operation_desc TEXT COMMENT '操作描述',
    resource_type VARCHAR(32) COMMENT '资源类型',
    resource_id BIGINT COMMENT '资源ID',
    request_ip VARCHAR(64) COMMENT '请求IP',
    request_method VARCHAR(16) COMMENT '请求方法',
    request_url TEXT COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_result TEXT COMMENT '响应结果',
    execution_time INT COMMENT '执行时间（毫秒）',
    status TINYINT DEFAULT 1 COMMENT '状态：1-成功，0-失败',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_username (username),
    INDEX idx_operation_type (operation_type),
    INDEX idx_resource (resource_type, resource_id),
    INDEX idx_created_at (created_at)
) COMMENT '系统操作日志表';

-- ==================== 初始化数据 ====================

-- 插入默认环境
INSERT INTO environments (env_code, env_name, env_desc, sort_order) VALUES
('dev', '开发环境', '开发测试环境', 1),
('test', '测试环境', '功能测试环境', 2),
('prod', '生产环境', '生产运行环境', 3);

-- 插入示例应用
INSERT INTO applications (app_code, app_name, app_desc, owner, contact_email) VALUES
('user-service', '用户服务', '用户管理微服务', '张三', 'zhangsan@example.com'),
('order-service', '订单服务', '订单管理微服务', '李四', 'lisi@example.com'),
('payment-service', '支付服务', '支付处理微服务', '王五', 'wangwu@example.com');

-- 插入示例配置项
INSERT INTO config_items (app_id, env_id, config_key, config_value, config_type, description) VALUES
(1, 1, 'server.port', '8081', 2, '服务端口'),
(1, 1, 'database.url', 'jdbc:mysql://localhost:3306/user_dev', 1, '数据库连接URL'),
(1, 1, 'redis.host', 'localhost', 1, 'Redis主机地址'),
(1, 1, 'redis.port', '6379', 2, 'Redis端口'),
(1, 2, 'server.port', '8081', 2, '服务端口'),
(1, 2, 'database.url', 'jdbc:mysql://test-db:3306/user_test', 1, '数据库连接URL'),
(1, 2, 'redis.host', 'test-redis', 1, 'Redis主机地址'),
(1, 2, 'redis.port', '6379', 2, 'Redis端口'),
(1, 3, 'server.port', '8081', 2, '服务端口'),
(1, 3, 'database.url', 'jdbc:mysql://prod-db:3306/user_prod', 1, '数据库连接URL'),
(1, 3, 'redis.host', 'prod-redis', 1, 'Redis主机地址'),
(1, 3, 'redis.port', '6379', 2, 'Redis端口'),
(2, 1, 'server.port', '8082', 2, '服务端口'),
(2, 1, 'database.url', 'jdbc:mysql://localhost:3306/order_dev', 1, '数据库连接URL'),
(2, 2, 'server.port', '8082', 2, '服务端口'),
(2, 2, 'database.url', 'jdbc:mysql://test-db:3306/order_test', 1, '数据库连接URL'),
(2, 3, 'server.port', '8082', 2, '服务端口'),
(2, 3, 'database.url', 'jdbc:mysql://prod-db:3306/order_prod', 1, '数据库连接URL'),
(3, 1, 'server.port', '8083', 2, '服务端口'),
(3, 1, 'database.url', 'jdbc:mysql://localhost:3306/payment_dev', 1, '数据库连接URL'),
(3, 2, 'server.port', '8083', 2, '服务端口'),
(3, 2, 'database.url', 'jdbc:mysql://test-db:3306/payment_test', 1, '数据库连接URL'),
(3, 3, 'server.port', '8083', 2, '服务端口'),
(3, 3, 'database.url', 'jdbc:mysql://prod-db:3306/payment_prod', 1, '数据库连接URL');

-- 插入默认角色
INSERT INTO roles (role_code, role_name, role_desc) VALUES
('admin', '系统管理员', '拥有所有权限'),
('app_owner', '应用负责人', '管理指定应用的配置'),
('config_editor', '配置编辑员', '编辑配置内容'),
('config_viewer', '配置查看员', '查看配置内容');

-- 插入默认权限
INSERT INTO permissions (perm_code, perm_name, perm_desc, resource_type) VALUES
('app:create', '创建应用', '创建新应用', 'application'),
('app:read', '查看应用', '查看应用信息', 'application'),
('app:update', '更新应用', '更新应用信息', 'application'),
('app:delete', '删除应用', '删除应用', 'application'),
('config:create', '创建配置', '创建配置项', 'config'),
('config:read', '查看配置', '查看配置内容', 'config'),
('config:update', '更新配置', '更新配置内容', 'config'),
('config:delete', '删除配置', '删除配置项', 'config'),
('version:create', '创建版本', '创建配置版本', 'version'),
('version:read', '查看版本', '查看版本信息', 'version'),
('version:rollback', '版本回滚', '回滚到指定版本', 'version'),
('push:execute', '执行推送', '执行配置推送', 'push'),
('push:read', '查看推送', '查看推送任务', 'push');

-- 为管理员角色分配所有权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, id FROM permissions;

-- 插入默认管理员用户（密码：admin123）
INSERT INTO users (username, password, real_name, email) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '系统管理员', 'admin@example.com');

-- 为默认用户分配管理员角色
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);

-- ==================== 创建索引优化 ====================

-- 为常用查询创建复合索引
CREATE INDEX idx_config_app_env_status ON config_items(app_id, env_id, status);
CREATE INDEX idx_version_app_env_created ON config_versions(app_id, env_id, created_at);
CREATE INDEX idx_push_app_env_status ON push_tasks(app_id, env_id, status);
CREATE INDEX idx_log_app_created ON config_access_logs(app_id, created_at);
CREATE INDEX idx_operation_user_created ON operation_logs(user_id, created_at);

-- ==================== 创建视图 ====================

-- 配置概览视图
CREATE VIEW v_config_overview AS
SELECT 
    a.app_code,
    a.app_name,
    e.env_code,
    e.env_name,
    COUNT(ci.id) as config_count,
    COUNT(CASE WHEN ci.status = 1 THEN 1 END) as active_config_count,
    MAX(ci.updated_at) as last_updated
FROM applications a
CROSS JOIN environments e
LEFT JOIN config_items ci ON a.id = ci.app_id AND e.id = ci.env_id
WHERE a.status = 1 AND e.status = 1
GROUP BY a.id, e.id;

-- 推送统计视图
CREATE VIEW v_push_statistics AS
SELECT 
    a.app_code,
    a.app_name,
    e.env_code,
    e.env_name,
    COUNT(pt.id) as total_tasks,
    COUNT(CASE WHEN pt.status = 2 THEN 1 END) as success_tasks,
    COUNT(CASE WHEN pt.status = 3 THEN 1 END) as failed_tasks,
    AVG(pt.success_count) as avg_success_count,
    AVG(pt.fail_count) as avg_fail_count
FROM applications a
CROSS JOIN environments e
LEFT JOIN push_tasks pt ON a.id = pt.app_id AND e.id = pt.env_id
WHERE a.status = 1 AND e.status = 1
GROUP BY a.id, e.id; 