-- =============================================
-- RBAC权限管理系统 - 数据库初始化脚本
-- =============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS authvs CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE authvs;

-- 删除旧表（如果存在）
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS users;

-- 1. 用户表 (users) - 支持后台管理员和门户用户
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    user_type TINYINT(1) NOT NULL DEFAULT 1 COMMENT '用户类型：1-门户用户(PORTAL) 2-后台管理员(ADMIN)',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at TIMESTAMP NULL COMMENT '创建时间',
    updated_at TIMESTAMP NULL COMMENT '更新时间',
    KEY idx_username (username),
    KEY idx_email (email),
    KEY idx_phone (phone),
    KEY idx_user_type (user_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 角色表 (roles) - 区分门户和后台角色
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    role_type TINYINT(1) NOT NULL DEFAULT 1 COMMENT '角色类型：1-门户角色 2-后台角色',
    description VARCHAR(200) COMMENT '角色描述',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at TIMESTAMP NULL COMMENT '创建时间',
    updated_at TIMESTAMP NULL COMMENT '更新时间',
    UNIQUE KEY uk_role_code_type (role_code, role_type),
    KEY idx_role_type (role_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 3. 权限表 (permissions) - 模块级权限控制
CREATE TABLE permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL COMMENT '权限编码',
    permission_type TINYINT(1) NOT NULL DEFAULT 1 COMMENT '权限类型：1-门户权限 2-后台权限',
    module_path VARCHAR(100) COMMENT '模块路径',
    parent_id BIGINT DEFAULT 0 COMMENT '父级权限ID，0表示顶级',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    description VARCHAR(200) COMMENT '权限描述',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at TIMESTAMP NULL COMMENT '创建时间',
    updated_at TIMESTAMP NULL COMMENT '更新时间',
    UNIQUE KEY uk_permission_code_type (permission_code, permission_type),
    KEY idx_permission_type (permission_type),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表（模块级）';

-- 4. 用户角色关联表 (user_roles)
CREATE TABLE user_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_at TIMESTAMP NULL COMMENT '创建时间',
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 5. 角色权限关联表 (role_permissions)
CREATE TABLE role_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    created_at TIMESTAMP NULL COMMENT '创建时间',
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    KEY idx_role_id (role_id),
    KEY idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- =============================================
-- 初始化示例数据
-- =============================================

-- 后台管理角色
INSERT INTO roles (id, role_name, role_code, role_type, description) VALUES
(1, '超级管理员', 'SUPER_ADMIN', 2, '后台超级管理员，拥有所有权限'),
(2, '系统管理员', 'SYSTEM_ADMIN', 2, '后台系统管理员，负责系统配置和维护'),
(3, '安全管理员', 'SECURITY_ADMIN', 2, '后台安全管理员，负责安全策略和权限管理'),
(4, '审计管理员', 'AUDIT_ADMIN', 2, '后台审计管理员，负责系统审计和日志管理');

-- 门户用户角色
INSERT INTO roles (id, role_name, role_code, role_type, description) VALUES
(5, '普通用户', 'NORMAL_USER', 1, '门户普通用户');



-- 后台管理权限（模块级）
-- 一级权限
INSERT INTO permissions (id, permission_name, permission_code, permission_type, module_path, parent_id, sort_order, description) VALUES
(1, '用户管理', 'USER_MODULE', 2, '/admin/users', 0, 1, '后台用户管理模块'),
(2, '审计管理', 'AUDIT_MODULE', 2, '/admin/audit', 0, 2, '后台审计管理模块'),
(3, '认证管理', 'AUTH_MODULE', 2, '/admin/auth', 0, 3, '后台认证管理模块'),
(4, '系统设置', 'SYSTEM_MODULE', 2, '/admin/settings', 0, 4, '后台系统设置模块');

-- 二级权限 - 用户管理
INSERT INTO permissions (id, permission_name, permission_code, permission_type, module_path, parent_id, sort_order, description) VALUES
(5, '用户列表', 'USER_LIST', 2, '/admin/users/list', 1, 1, '用户列表管理'),
(6, '管理员列表', 'ADMIN_LIST', 2, '/admin/users/admin', 1, 2, '管理员列表管理');

-- 二级权限 - 审计管理
INSERT INTO permissions (id, permission_name, permission_code, permission_type, module_path, parent_id, sort_order, description) VALUES
(7, '审计列表', 'AUDIT_LIST', 2, '/admin/audit/list', 2, 1, '审计列表查询（支持后台和门户日志）');

-- 二级权限 - 认证管理
INSERT INTO permissions (id, permission_name, permission_code, permission_type, module_path, parent_id, sort_order, description) VALUES
(8, '客户端列表', 'CLIENT_LIST', 2, '/admin/auth/clients', 3, 1, '认证客户端列表管理');

-- 二级权限 - 系统设置
INSERT INTO permissions (id, permission_name, permission_code, permission_type, module_path, parent_id, sort_order, description) VALUES
(9, '基础配置', 'BASIC_CONFIG', 2, '/admin/settings/config', 4, 1, '系统基础配置管理');

-- 后台管理员用户（密码：admin123, sysadmin123, secadmin123, auditadmin123）
INSERT INTO users (username, password, email, phone, user_type, enabled) VALUES
('admin', '$2a$10$1jd0t.AWFGX3IJZdgjUfl.cH1Kvl02DW.S4YgbJbESnsyW3gC2abK', 'admin@example.com', '13800138000', 2, 1),
('sysadmin', '$2a$10$V9.2Q0EX3hXKK3cAJ1eJb.FFCfVqoGdL77fBBhBVqXG5EeX/2hVhS', 'sysadmin@example.com', '13800138001', 2, 1),
('secadmin', '$2a$10$xQsUvGHPcFpkkPd2xFEYo.R92eGQRBKp8TTEMzQRxKfvKVfMaK1Jm', 'secadmin@example.com', '13800138002', 2, 1),
('auditadmin', '$2a$10$xQsUvGHPcFpkkPd2xFEYo.R92eGQRBKp8TTEMzQRxKfvKVfMaK1Jm', 'auditadmin@example.com', '13800138003', 2, 1);

-- 门户用户（密码：user123）
INSERT INTO users (username, password, email, phone, user_type, enabled) VALUES
('user001', '$2a$10$xQsUvGHPcFpkkPd2xFEYo.R92eGQRBKp8TTEMzQRxKfvKVfMaK1Jm', 'user001@example.com', '13900139001', 1, 1),
('user002', '$2a$10$xQsUvGHPcFpkkPd2xFEYo.R92eGQRBKp8TTEMzQRxKfvKVfMaK1Jm', 'user002@example.com', '13900139002', 1, 1);



-- 用户角色关联
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- admin 拥有 超级管理员 角色
(2, 2), -- sysadmin 拥有 系统管理员 角色
(3, 3), -- secadmin 拥有 安全管理员 角色
(4, 4), -- auditadmin 拥有 审计管理员 角色
(5, 5), -- user001 拥有 普通用户 角色
(6, 5); -- user002 拥有 普通用户 角色

-- 角色权限关联（若有子菜单则自动包含父菜单）
INSERT INTO role_permissions (role_id, permission_id) VALUES
-- 系统管理员：用户管理父级 + 子级，系统设置父级 + 子级
(2, 1), (2, 5), (2, 6), (2, 4), (2, 9),
-- 安全管理员：认证管理父级 + 子级
(3, 3), (3, 8),
-- 审计管理员：审计管理父级 + 子级
(4, 2), (4, 7);
-- =============================================
-- 操作日志表
-- =============================================
DROP TABLE IF EXISTS operation_log;

CREATE TABLE operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '操作用户名',
    
    operation_type VARCHAR(20) COMMENT '操作类型：LOGIN LOGOUT CREATE UPDATE DELETE QUERY EXPORT IMPORT',
    operation_module VARCHAR(50) COMMENT '操作模块',
    operation_desc VARCHAR(500) COMMENT '操作描述',
    
    request_method VARCHAR(10) COMMENT '请求方法：GET POST PUT DELETE',
    request_url VARCHAR(500) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数（JSON格式，敏感信息脱敏）',
    response_result TEXT COMMENT '返回结果（JSON格式）',
    
    ip_address VARCHAR(50) COMMENT 'IP地址',
    device_type VARCHAR(20) COMMENT '设备类型：WEB IOS ANDROID PC',
    user_agent VARCHAR(500) COMMENT '用户代理',
    
    platform_type TINYINT(1) DEFAULT 1 COMMENT '平台类型：1-门户 2-后台管理',
    
    execute_time BIGINT COMMENT '执行时长（毫秒）',
    status VARCHAR(20) COMMENT '执行状态：SUCCESS FAIL',
    error_message TEXT COMMENT '错误信息',
    
    created_at TIMESTAMP NULL COMMENT '创建时间',
    
    KEY idx_user_id (user_id),
    KEY idx_username (username),
    KEY idx_operation_type (operation_type),
    KEY idx_operation_module (operation_module),
    KEY idx_platform_type (platform_type),
    KEY idx_status (status),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';