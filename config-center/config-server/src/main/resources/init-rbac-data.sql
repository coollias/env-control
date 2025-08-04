-- RBAC系统初始化数据
-- 在config_center数据库中执行

USE config_center;

-- ==================== 初始化角色数据 ====================

-- 插入基础角色
INSERT INTO roles (role_code, role_name, role_desc, status, created_at, updated_at) VALUES
('ADMIN', '系统管理员', '拥有系统所有权限的管理员角色', 1, NOW(), NOW()),
('DEVELOPER', '开发人员', '可以管理应用配置的开发人员角色', 1, NOW(), NOW()),
('OPERATOR', '运维人员', '可以查看配置的运维人员角色', 1, NOW(), NOW()),
('VIEWER', '查看者', '只能查看配置的普通用户角色', 1, NOW(), NOW());

-- ==================== 初始化权限数据 ====================

-- 系统管理权限
INSERT INTO permissions (perm_code, perm_name, perm_desc, resource_type, status, created_at, updated_at) VALUES
-- 用户管理权限
('user:create', '创建用户', '创建新用户的权限', 'user', 1, NOW(), NOW()),
('user:read', '查看用户', '查看用户信息的权限', 'user', 1, NOW(), NOW()),
('user:update', '更新用户', '更新用户信息的权限', 'user', 1, NOW(), NOW()),
('user:delete', '删除用户', '删除用户的权限', 'user', 1, NOW(), NOW()),

-- 角色管理权限
('role:create', '创建角色', '创建新角色的权限', 'role', 1, NOW(), NOW()),
('role:read', '查看角色', '查看角色信息的权限', 'role', 1, NOW(), NOW()),
('role:update', '更新角色', '更新角色信息的权限', 'role', 1, NOW(), NOW()),
('role:delete', '删除角色', '删除角色的权限', 'role', 1, NOW(), NOW()),

-- 权限管理权限
('permission:create', '创建权限', '创建新权限的权限', 'permission', 1, NOW(), NOW()),
('permission:read', '查看权限', '查看权限信息的权限', 'permission', 1, NOW(), NOW()),
('permission:update', '更新权限', '更新权限信息的权限', 'permission', 1, NOW(), NOW()),
('permission:delete', '删除权限', '删除权限的权限', 'permission', 1, NOW(), NOW()),

-- 应用管理权限
('app:create', '创建应用', '创建新应用的权限', 'application', 1, NOW(), NOW()),
('app:read', '查看应用', '查看应用信息的权限', 'application', 1, NOW(), NOW()),
('app:update', '更新应用', '更新应用信息的权限', 'application', 1, NOW(), NOW()),
('app:delete', '删除应用', '删除应用的权限', 'application', 1, NOW(), NOW()),

-- 环境管理权限
('env:create', '创建环境', '创建新环境的权限', 'environment', 1, NOW(), NOW()),
('env:read', '查看环境', '查看环境信息的权限', 'environment', 1, NOW(), NOW()),
('env:update', '更新环境', '更新环境信息的权限', 'environment', 1, NOW(), NOW()),
('env:delete', '删除环境', '删除环境的权限', 'environment', 1, NOW(), NOW()),

-- 配置管理权限
('config:create', '创建配置', '创建新配置的权限', 'config', 1, NOW(), NOW()),
('config:read', '查看配置', '查看配置信息的权限', 'config', 1, NOW(), NOW()),
('config:update', '更新配置', '更新配置信息的权限', 'config', 1, NOW(), NOW()),
('config:delete', '删除配置', '删除配置的权限', 'config', 1, NOW(), NOW()),

-- 系统监控权限
('system:monitor', '系统监控', '查看系统监控信息的权限', 'system', 1, NOW(), NOW()),
('system:log', '系统日志', '查看系统日志的权限', 'system', 1, NOW(), NOW()),
('system:backup', '系统备份', '执行系统备份的权限', 'system', 1, NOW(), NOW());

-- ==================== 角色权限关联 ====================

-- 系统管理员拥有所有权限
INSERT INTO role_permissions (role_id, permission_id, created_at) 
SELECT r.id, p.id, NOW()
FROM roles r, permissions p
WHERE r.role_code = 'ADMIN';

-- 开发人员权限
INSERT INTO role_permissions (role_id, permission_id, created_at) 
SELECT r.id, p.id, NOW()
FROM roles r, permissions p
WHERE r.role_code = 'DEVELOPER' 
AND p.perm_code IN (
    'app:read', 'app:update',
    'env:read', 'env:update',
    'config:create', 'config:read', 'config:update', 'config:delete'
);

-- 运维人员权限
INSERT INTO role_permissions (role_id, permission_id, created_at) 
SELECT r.id, p.id, NOW()
FROM roles r, permissions p
WHERE r.role_code = 'OPERATOR' 
AND p.perm_code IN (
    'app:read',
    'env:read',
    'config:read',
    'system:monitor', 'system:log'
);

-- 查看者权限
INSERT INTO role_permissions (role_id, permission_id, created_at) 
SELECT r.id, p.id, NOW()
FROM roles r, permissions p
WHERE r.role_code = 'VIEWER' 
AND p.perm_code IN (
    'app:read',
    'env:read',
    'config:read'
);

-- ==================== 用户角色关联 ====================

-- 为第一个用户分配管理员角色（假设用户ID为1）
INSERT INTO user_roles (user_id, role_id, created_at) VALUES
(1, (SELECT id FROM roles WHERE role_code = 'ADMIN'), NOW());

-- ==================== 应用权限示例 ====================

-- 为管理员分配所有应用的管理权限（假设应用ID为1,2,3）
INSERT INTO app_permissions (user_id, app_id, permission_type, status, created_at, updated_at) VALUES
(1, 1, 'ADMIN', 1, NOW(), NOW()),
(1, 2, 'ADMIN', 1, NOW(), NOW()),
(1, 3, 'ADMIN', 1, NOW(), NOW());

-- 注意：实际使用时需要根据真实的用户ID和应用ID来调整这些数据 