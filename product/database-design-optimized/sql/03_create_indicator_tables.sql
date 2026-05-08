-- =====================================================
-- Part 3: 指标管理类表（优化版）
-- =====================================================

-- 表10：bi_indicator_system（指标体系表）
CREATE TABLE bi_indicator_system (
  system_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  system_code VARCHAR(100) NOT NULL COMMENT '体系代码',
  system_name VARCHAR(200) NOT NULL COMMENT '体系名称',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
  sort_num INT NOT NULL DEFAULT 99 COMMENT '排序',
  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标记',
  deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (system_id),
  UNIQUE KEY uk_system_code (system_code),
  KEY idx_status_deleted (status, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指标体系表';

-- 表11：bi_indicator_category（指标分类表）
CREATE TABLE bi_indicator_category (
  cat_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  system_id BIGINT NOT NULL COMMENT '所属体系ID',
  cat_code VARCHAR(100) NOT NULL COMMENT '分类代码',
  cat_name VARCHAR(200) NOT NULL COMMENT '分类名称',
  sort_num INT NOT NULL DEFAULT 99 COMMENT '排序',
  status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态',
  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标记',
  deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (cat_id),
  UNIQUE KEY uk_system_cat_code (system_id, cat_code),
  KEY idx_system_id (system_id),
  KEY idx_status_deleted (status, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指标分类表';

-- 表12：bi_indicator（指标表，核心表 - 拆分优化版）
CREATE TABLE bi_indicator (
  zb_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  parent_zb_id BIGINT NOT NULL DEFAULT 0 COMMENT '父指标ID',
  zb_code VARCHAR(100) NOT NULL COMMENT '指标代码',
  zb_name VARCHAR(255) NOT NULL COMMENT '指标名称',
  system_id BIGINT NOT NULL COMMENT '体系ID',
  cat_id BIGINT NOT NULL COMMENT '分类ID',
  zb_meaning VARCHAR(2000) DEFAULT NULL COMMENT '指标意义',
  zb_caliber VARCHAR(2000) DEFAULT NULL COMMENT '指标口径说明',
  is_real_time TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否实时',
  has_decimal TINYINT(1) NOT NULL DEFAULT 0 COMMENT '有无小数',
  ratio_type VARCHAR(10) NOT NULL DEFAULT '1' COMMENT '比率类型',
  unit VARCHAR(50) DEFAULT NULL COMMENT '指标单位',
  config_type TINYINT(1) NOT NULL DEFAULT 1 COMMENT '配置方式',
  status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '指标状态',
  version INT NOT NULL DEFAULT 1 COMMENT '版本号',
  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标记',
  deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
  created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (zb_id),
  UNIQUE KEY uk_zb_code (zb_code),
  KEY idx_parent_zb_id (parent_zb_id),
  KEY idx_system_cat (system_id, cat_id, status),
  KEY idx_status_deleted (status, is_deleted),
  KEY idx_config_type (config_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指标基础信息表';

-- 表12-1：bi_indicator_formula（指标公式配置表）
CREATE TABLE bi_indicator_formula (
  formula_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  zb_id BIGINT NOT NULL COMMENT '指标ID',
  formula_text VARCHAR(1000) NOT NULL COMMENT '公式文本（如：@A003/@A001）',
  formula_parsed TEXT DEFAULT NULL COMMENT '解析后的公式结构（JSON）',
  dependency_zb_codes VARCHAR(500) DEFAULT NULL COMMENT '依赖的指标代码列表',
  calculation_order INT NOT NULL DEFAULT 0 COMMENT '计算顺序',
  status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (formula_id),
  UNIQUE KEY uk_zb_id (zb_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指标公式配置表';

-- 表12-2：bi_indicator_dimension（指标维度配置表）
CREATE TABLE bi_indicator_dimension (
  dim_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  zb_id BIGINT NOT NULL COMMENT '指标ID',
  fact_table VARCHAR(200) NOT NULL COMMENT '事实表名',
  measure_field VARCHAR(200) NOT NULL COMMENT '度量字段',
  dimension_field VARCHAR(200) DEFAULT NULL COMMENT '维度字段',
  filter_condition TEXT DEFAULT NULL COMMENT '过滤条件',
  aggregation_type VARCHAR(50) NOT NULL DEFAULT 'SUM' COMMENT '聚合类型（SUM/COUNT/AVG/MAX/MIN）',
  status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (dim_id),
  UNIQUE KEY uk_zb_id (zb_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指标维度配置表';

-- 表12-3：bi_indicator_sql（指标SQL配置表）
CREATE TABLE bi_indicator_sql (
  sql_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  zb_id BIGINT NOT NULL COMMENT '指标ID',
  sql_text TEXT NOT NULL COMMENT 'SQL语句',
  sql_parsed TEXT DEFAULT NULL COMMENT '解析后的SQL结构（JSON）',
  data_source VARCHAR(100) DEFAULT NULL COMMENT '数据源',
  cache_enabled TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用缓存',
  cache_expire_sec INT NOT NULL DEFAULT 3600 COMMENT '缓存过期时间（秒）',
  status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (sql_id),
  UNIQUE KEY uk_zb_id (zb_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指标SQL配置表';