-- 修复 config_changes 表，添加缺失的 updated_at 列
-- 这个脚本解决 Hibernate 验证错误

USE config_center;

-- 为 config_changes 表添加 updated_at 列
ALTER TABLE config_changes 
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 为 config_versions 表添加 updated_at 列（如果不存在）
ALTER TABLE config_versions 
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 验证修复结果
DESCRIBE config_changes;
DESCRIBE config_versions; 