-- =====================================================
-- Part 4: 指标扩展类表（优化版）
-- =====================================================

-- 表13：bi_indicator_drill（指标下钻配置表）
CREATE TABLE bi_indicator_drill (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  zb_id BIGINT NOT NULL COMMENT '指标ID',
  drill_level TINYINT(1) NOT NULL DEFAULT 1 COMMENT '下钻层级',
  dimension_code VARCHAR(100) NOT NULL COMMENT '维度代码',
  dimension_name VARCHAR(200) NOT NULL COMMENT '维度名称',
  dimension_table VARCHAR(100) DEFAULT NULL COMMENT '维度表名',
  dimension_field VARCHAR(100) DEFAULT NULL COMMENT '维度字段名',
  sort_num INT NOT NULL DEFAULT 99 COMMENT '排序',
  status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_zb_level (zb_id, drill_level),
  KEY idx_dimension_code (dimension_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指标下钻配置表';

-- 表14：bi_indicator_relation（关联指标表）
CREATE TABLE bi_indicator_relation (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  zb_id BIGINT NOT NULL COMMENT '当前指标ID',
  related_zb_id BIGINT NOT NULL COMMENT '关联指标ID',
  relation_type TINYINT(1) NOT NULL DEFAULT 1 COMMENT '关系类型',
  relation_strength DECIMAL(5,2) DEFAULT NULL COMMENT '关联强度',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_zb_relation (zb_id, related_zb_id, relation_type),
  KEY idx_related_zb_id (related_zb_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关联指标表';

-- 表15：bi_application_theme（应用主题表）
CREATE TABLE bi_application_theme (
  theme_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  parent_theme_id BIGINT NOT NULL DEFAULT 0 COMMENT '父主题ID',
  theme_code VARCHAR(100) NOT NULL COMMENT '主题代码',
  theme_name VARCHAR(200) NOT NULL COMMENT '主题名称',
  system_source VARCHAR(200) NOT NULL DEFAULT '运营决策支持系统' COMMENT '系统来源',
  theme_icon VARCHAR(100) DEFAULT NULL COMMENT '主题图标',
  sort_num INT NOT NULL DEFAULT 99 COMMENT '排序',
  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标记',
  deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (theme_id),
  UNIQUE KEY uk_theme_code (theme_code),
  KEY idx_parent_theme_id (parent_theme_id),
  KEY idx_sort_num (sort_num)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应用主题表';

-- 表16：bi_theme_indicator（主题指标映射表）
CREATE TABLE bi_theme_indicator (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  theme_id BIGINT NOT NULL COMMENT '主题ID',
  zb_id BIGINT NOT NULL COMMENT '指标ID',
  sort_num INT NOT NULL DEFAULT 99 COMMENT '排序',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_theme_zb (theme_id, zb_id),
  KEY idx_zb_id (zb_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='主题指标映射表';

-- 表17：bi_indicator_spec（指标规范表）
CREATE TABLE bi_indicator_spec (
  spec_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  spec_name VARCHAR(200) NOT NULL COMMENT '规范名称',
  spec_code VARCHAR(100) NOT NULL COMMENT '规范代码',
  doc_summary TEXT DEFAULT NULL COMMENT '文档摘要',
  doc_remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  doc_source VARCHAR(200) DEFAULT NULL COMMENT '文档来源',
  effective_date DATE DEFAULT NULL COMMENT '生效日期',
  sort_num INT NOT NULL DEFAULT 99 COMMENT '排序',
  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标记',
  deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (spec_id),
  UNIQUE KEY uk_spec_code (spec_code),
  KEY idx_effective_date (effective_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指标规范表';

-- =====================================================
-- Part 5: 目标值管理类表（优化版）
-- =====================================================

-- 表18：bi_target_setting（目标值启用设置表）
CREATE TABLE bi_target_setting (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  zb_id BIGINT NOT NULL COMMENT '指标ID',
  is_enabled TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用',
  effective_date DATE DEFAULT NULL COMMENT '生效日期',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_zb_id (zb_id),
  KEY idx_is_enabled (is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='目标值启用设置表';

-- 表19：bi_target_value（目标值分配表，核心表 - 优化版）
CREATE TABLE bi_target_value (
  target_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  zb_id BIGINT NOT NULL COMMENT '指标ID',
  target_date DATE NOT NULL COMMENT '目标日期',
  target_type TINYINT(1) NOT NULL DEFAULT 2 COMMENT '目标类型：1年度 2月度 3季度',
  org_level TINYINT(1) NOT NULL DEFAULT 1 COMMENT '组织层级：1全院 2分院 3科室',
  org_id BIGINT NOT NULL DEFAULT 0 COMMENT '组织ID',
  org_code VARCHAR(100) DEFAULT NULL COMMENT '组织代码',
  org_name VARCHAR(200) DEFAULT NULL COMMENT '组织名称',
  last_year_value DECIMAL(14,4) DEFAULT NULL COMMENT '同期值',
  last_period_value DECIMAL(14,4) DEFAULT NULL COMMENT '上期值',
  target_increase_rate DECIMAL(10,4) NOT NULL DEFAULT 0.0000 COMMENT '目标增幅（%）',
  target_value DECIMAL(14,4) DEFAULT NULL COMMENT '目标值',
  actual_value DECIMAL(14,4) DEFAULT NULL COMMENT '实际值',
  completion_rate DECIMAL(10,4) DEFAULT NULL COMMENT '完成率（%）',
  is_locked TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否锁定',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (target_id),
  UNIQUE KEY uk_zb_date_org (zb_id, target_date, org_level, org_id),
  KEY idx_zb_date (zb_id, target_date),
  KEY idx_org (org_level, org_id),
  KEY idx_target_type (target_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='目标值分配表';