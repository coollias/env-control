-- 修复数据库表结构
-- 为缺少 updated_at 字段的表添加该字段

USE config_center;

-- 修复 role_permissions 表
ALTER TABLE role_permissions 
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 修复 user_roles 表
ALTER TABLE user_roles 
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 验证修复结果
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'config_center' 
AND TABLE_NAME IN ('role_permissions', 'user_roles')
ORDER BY TABLE_NAME, ORDINAL_POSITION; 