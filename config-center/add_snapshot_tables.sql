-- 添加配置快照相关表的SQL脚本
-- 请在MySQL数据库中执行此脚本

USE config_center;

-- 配置快照表
CREATE TABLE IF NOT EXISTS config_snapshots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '快照ID',
    app_id BIGINT NOT NULL COMMENT '应用ID',
    env_id BIGINT NOT NULL COMMENT '环境ID',
    snapshot_name VARCHAR(128) NOT NULL COMMENT '快照名称',
    snapshot_desc TEXT COMMENT '快照描述',
    version_number VARCHAR(32) NOT NULL COMMENT '版本号',
    snapshot_type TINYINT DEFAULT 1 COMMENT '快照类型：1-暂存，2-发布',
    status TINYINT DEFAULT 1 COMMENT '状态：1-有效，0-无效',
    config_data LONGTEXT NOT NULL COMMENT '完整配置数据（JSON格式）',
    config_count INT DEFAULT 0 COMMENT '配置项数量',
    created_by VARCHAR(64) NOT NULL COMMENT '创建人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (app_id) REFERENCES applications(id) ON DELETE CASCADE,
    FOREIGN KEY (env_id) REFERENCES environments(id) ON DELETE CASCADE,
    UNIQUE KEY uk_app_env_version (app_id, env_id, version_number),
    INDEX idx_app_id (app_id),
    INDEX idx_env_id (env_id),
    INDEX idx_version_number (version_number),
    INDEX idx_snapshot_type (snapshot_type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) COMMENT '配置快照表';

-- 配置快照详情表（存储快照中的每个配置项）
CREATE TABLE IF NOT EXISTS config_snapshot_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '快照项ID',
    snapshot_id BIGINT NOT NULL COMMENT '快照ID',
    config_key VARCHAR(128) NOT NULL COMMENT '配置键',
    config_value LONGTEXT COMMENT '配置值',
    config_type TINYINT DEFAULT 1 COMMENT '配置类型：1-字符串，2-数字，3-布尔，4-JSON，5-YAML，6-Properties',
    is_encrypted TINYINT DEFAULT 0 COMMENT '是否加密：1-是，0-否',
    is_required TINYINT DEFAULT 0 COMMENT '是否必填：1-是，0-否',
    default_value LONGTEXT COMMENT '默认值',
    description TEXT COMMENT '配置描述',
    group_id BIGINT DEFAULT 0 COMMENT '配置组ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (snapshot_id) REFERENCES config_snapshots(id) ON DELETE CASCADE,
    INDEX idx_snapshot_id (snapshot_id),
    INDEX idx_config_key (config_key),
    INDEX idx_group_id (group_id),
    INDEX idx_sort_order (sort_order)
) COMMENT '配置快照详情表';

-- 配置发布记录表（记录发布历史）
CREATE TABLE IF NOT EXISTS config_publish_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '发布记录ID',
    app_id BIGINT NOT NULL COMMENT '应用ID',
    env_id BIGINT NOT NULL COMMENT '环境ID',
    snapshot_id BIGINT NOT NULL COMMENT '快照ID',
    version_number VARCHAR(32) NOT NULL COMMENT '版本号',
    publish_type TINYINT DEFAULT 1 COMMENT '发布类型：1-立即发布，2-定时发布，3-灰度发布',
    publish_status TINYINT DEFAULT 0 COMMENT '发布状态：0-待发布，1-发布中，2-成功，3-失败',
    target_instances TEXT COMMENT '目标实例列表（JSON格式）',
    success_count INT DEFAULT 0 COMMENT '成功数量',
    fail_count INT DEFAULT 0 COMMENT '失败数量',
    error_message TEXT COMMENT '错误信息',
    published_by VARCHAR(64) NOT NULL COMMENT '发布人',
    published_at TIMESTAMP NULL COMMENT '发布时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (app_id) REFERENCES applications(id) ON DELETE CASCADE,
    FOREIGN KEY (env_id) REFERENCES environments(id) ON DELETE CASCADE,
    FOREIGN KEY (snapshot_id) REFERENCES config_snapshots(id) ON DELETE CASCADE,
    INDEX idx_app_id (app_id),
    INDEX idx_env_id (env_id),
    INDEX idx_snapshot_id (snapshot_id),
    INDEX idx_version_number (version_number),
    INDEX idx_publish_status (publish_status),
    INDEX idx_published_at (published_at)
) COMMENT '配置发布记录表';

-- 验证表是否创建成功
SELECT 'config_snapshots' as table_name, COUNT(*) as record_count FROM config_snapshots
UNION ALL
SELECT 'config_snapshot_items' as table_name, COUNT(*) as record_count FROM config_snapshot_items
UNION ALL
SELECT 'config_publish_records' as table_name, COUNT(*) as record_count FROM config_publish_records;
