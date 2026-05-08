-- =====================================================
-- BI System Database Design - Optimized by DBA
-- Version: 2.0.0
-- Date: 2025-05-08
-- Description: 医院运营决策支持系统数据库设计（DBA优化版）
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS bi_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE bi_db;

-- =====================================================
-- Part 1: 系统权限类表（优化版）
-- =====================================================

-- 表1：sys_user（用户表）
CREATE TABLE sys_user (
  user_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键，用户ID',
  username VARCHAR(50) NOT NULL COMMENT '登录账号',
  password_hash VARCHAR(255) NOT NULL COMMENT '加密密码（BCrypt）',
  real_name VARCHAR(100) DEFAULT NULL COMMENT '真实姓名',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  hospital_id BIGINT DEFAULT NULL COMMENT '医院ID',
  dept_option TINYINT(1) NOT NULL DEFAULT 1 COMMENT '科室选项：0隐藏 1显示',
  status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标记',
  deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (user_id),
  UNIQUE KEY uk_username (username),
  KEY idx_role_id (role_id),
  KEY idx_hospital_id (hospital_id),
  KEY idx_status_deleted (status, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 表2：sys_role（角色表）
CREATE TABLE sys_role (
  role_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
  role_code VARCHAR(50) NOT NULL COMMENT '角色代码（如：admin, dean, dept_director）',
  system_name VARCHAR(100) NOT NULL DEFAULT '运营决策支持系统' COMMENT '系统名称',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标记',
  deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (role_id),
  UNIQUE KEY uk_role_code (role_code),
  KEY idx_status_deleted (status, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 表3：sys_menu（菜单表，支持4级）
CREATE TABLE sys_menu (
  menu_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父菜单ID，0为一级',
  menu_name VARCHAR(255) NOT NULL COMMENT '菜单名称',
  menu_code VARCHAR(100) DEFAULT NULL COMMENT '菜单代码',
  menu_level TINYINT(1) NOT NULL DEFAULT 1 COMMENT '菜单层级：1/2/3/4',
  link_url VARCHAR(500) DEFAULT NULL COMMENT '链接地址',
  sort_num INT NOT NULL DEFAULT 99 COMMENT '排序',
  auto_load TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否自动加载（SSRS用）',
  show_option TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否显示选项（SSRS用）',
  default_param VARCHAR(500) DEFAULT NULL COMMENT '默认参数',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标记',
  deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (menu_id),
  KEY idx_parent_id (parent_id),
  KEY idx_level_sort (menu_level, sort_num),
  KEY idx_status_deleted (status, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';

-- 表4：sys_role_menu（角色菜单权限表）
CREATE TABLE sys_role_menu (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  menu_id BIGINT NOT NULL COMMENT '菜单ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_menu (role_id, menu_id),
  KEY idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单权限表';

-- 表5：sys_user_dept（用户科室权限表）
CREATE TABLE sys_user_dept (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  dept_code VARCHAR(50) NOT NULL COMMENT '科室代码',
  dept_name VARCHAR(200) NOT NULL COMMENT '科室名称',
  dept_type TINYINT(1) NOT NULL DEFAULT 1 COMMENT '类型：1HIS科室 2病案科室 3病区',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_dept (user_id, dept_code, dept_type),
  KEY idx_dept_type (dept_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户科室权限表';