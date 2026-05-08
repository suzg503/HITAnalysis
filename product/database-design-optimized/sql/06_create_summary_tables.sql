-- =====================================================
-- Part 8: 指标结果汇总表（新增 - 性能优化关键）
-- =====================================================

-- 表26：bi_indicator_result_daily（指标日汇总表）
CREATE TABLE bi_indicator_result_daily (
  result_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  zb_id BIGINT NOT NULL COMMENT '指标ID',
  zb_code VARCHAR(100) NOT NULL COMMENT '指标代码',
  zb_name VARCHAR(255) NOT NULL COMMENT '指标名称',
  data_date DATE NOT NULL COMMENT '数据日期',
  org_level TINYINT(1) NOT NULL DEFAULT 1 COMMENT '组织层级',
  org_id BIGINT NOT NULL DEFAULT 0 COMMENT '组织ID',
  org_code VARCHAR(100) DEFAULT NULL COMMENT '组织代码',
  org_name VARCHAR(200) DEFAULT NULL COMMENT '组织名称',
  indicator_value DECIMAL(14,4) DEFAULT NULL COMMENT '指标值',
  indicator_count INT DEFAULT NULL COMMENT '计数类型指标的值',
  yesterday_value DECIMAL(14,4) DEFAULT NULL COMMENT '昨日值',
  day_change_rate DECIMAL(10,4) DEFAULT NULL COMMENT '日环比变化率（%）',
  last_week_value DECIMAL(14,4) DEFAULT NULL COMMENT '上周同期值',
  week_change_rate DECIMAL(10,4) DEFAULT NULL COMMENT '周同比变化率（%）',
  last_month_value DECIMAL(14,4) DEFAULT NULL COMMENT '上月同期值',
  month_change_rate DECIMAL(10,4) DEFAULT NULL COMMENT '月同比变化率（%）',
  last_year_value DECIMAL(14,4) DEFAULT NULL COMMENT '去年同期值',
  year_change_rate DECIMAL(10,4) DEFAULT NULL COMMENT '年同比变化率（%）',
  target_value DECIMAL(14,4) DEFAULT NULL COMMENT '目标值',
  completion_rate DECIMAL(10,4) DEFAULT NULL COMMENT '完成率（%）',
  data_source VARCHAR(100) DEFAULT NULL COMMENT '数据来源',
  calc_time DATETIME DEFAULT NULL COMMENT '计算时间',
  is_valid TINYINT(1) NOT NULL DEFAULT 1 COMMENT '数据是否有效',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (result_id),
  UNIQUE KEY uk_zb_date_org (zb_id, data_date, org_level, org_id),
  KEY idx_zb_date (zb_id, data_date),
  KEY idx_data_date (data_date),
  KEY idx_org (org_level, org_id),
  KEY idx_zb_code (zb_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指标日汇总表'
PARTITION BY RANGE (TO_DAYS(data_date)) (
  PARTITION p202501 VALUES LESS THAN (TO_DAYS('2025-02-01')),
  PARTITION p202502 VALUES LESS THAN (TO_DAYS('2025-03-01')),
  PARTITION p202503 VALUES LESS THAN (TO_DAYS('2025-04-01')),
  PARTITION p202504 VALUES LESS THAN (TO_DAYS('2025-05-01')),
  PARTITION p202505 VALUES LESS THAN (TO_DAYS('2025-06-01')),
  PARTITION p202506 VALUES LESS THAN (TO_DAYS('2025-07-01')),
  PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- 表27：bi_indicator_result_monthly（指标月汇总表）
CREATE TABLE bi_indicator_result_monthly (
  result_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  zb_id BIGINT NOT NULL COMMENT '指标ID',
  zb_code VARCHAR(100) NOT NULL COMMENT '指标代码',
  zb_name VARCHAR(255) NOT NULL COMMENT '指标名称',
  data_month DATE NOT NULL COMMENT '数据月份（yyyy-mm-01）',
  org_level TINYINT(1) NOT NULL DEFAULT 1 COMMENT '组织层级',
  org_id BIGINT NOT NULL DEFAULT 0 COMMENT '组织ID',
  org_code VARCHAR(100) DEFAULT NULL COMMENT '组织代码',
  org_name VARCHAR(200) DEFAULT NULL COMMENT '组织名称',
  indicator_value DECIMAL(14,4) DEFAULT NULL COMMENT '指标值',
  indicator_count INT DEFAULT NULL COMMENT '计数类型指标的值',
  indicator_avg DECIMAL(14,4) DEFAULT NULL COMMENT '平均值（用于日均）',
  indicator_max DECIMAL(14,4) DEFAULT NULL COMMENT '最大值',
  indicator_min DECIMAL(14,4) DEFAULT NULL COMMENT '最小值',
  last_month_value DECIMAL(14,4) DEFAULT NULL COMMENT '上月值',
  month_change_rate DECIMAL(10,4) DEFAULT NULL COMMENT '月环比变化率（%）',
  last_year_value DECIMAL(14,4) DEFAULT NULL COMMENT '去年同期值',
  year_change_rate DECIMAL(10,4) DEFAULT NULL COMMENT '年同比变化率（%）',
  target_value DECIMAL(14,4) DEFAULT NULL COMMENT '目标值',
  completion_rate DECIMAL(10,4) DEFAULT NULL COMMENT '完成率（%）',
  month_progress_rate DECIMAL(10,4) DEFAULT NULL COMMENT '月进度率（%）',
  data_source VARCHAR(100) DEFAULT NULL COMMENT '数据来源',
  calc_time DATETIME DEFAULT NULL COMMENT '计算时间',
  is_valid TINYINT(1) NOT NULL DEFAULT 1 COMMENT '数据是否有效',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (result_id),
  UNIQUE KEY uk_zb_month_org (zb_id, data_month, org_level, org_id),
  KEY idx_zb_month (zb_id, data_month),
  KEY idx_data_month (data_month),
  KEY idx_org (org_level, org_id),
  KEY idx_zb_code (zb_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指标月汇总表';

-- 表28：bi_indicator_result_yearly（指标年汇总表）
CREATE TABLE bi_indicator_result_yearly (
  result_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  zb_id BIGINT NOT NULL COMMENT '指标ID',
  zb_code VARCHAR(100) NOT NULL COMMENT '指标代码',
  zb_name VARCHAR(255) NOT NULL COMMENT '指标名称',
  data_year INT NOT NULL COMMENT '数据年份',
  org_level TINYINT(1) NOT NULL DEFAULT 1 COMMENT '组织层级',
  org_id BIGINT NOT NULL DEFAULT 0 COMMENT '组织ID',
  org_code VARCHAR(100) DEFAULT NULL COMMENT '组织代码',
  org_name VARCHAR(200) DEFAULT NULL COMMENT '组织名称',
  indicator_value DECIMAL(14,4) DEFAULT NULL COMMENT '指标值',
  indicator_count INT DEFAULT NULL COMMENT '计数类型指标的值',
  indicator_avg DECIMAL(14,4) DEFAULT NULL COMMENT '平均值',
  indicator_max DECIMAL(14,4) DEFAULT NULL COMMENT '最大值',
  indicator_min DECIMAL(14,4) DEFAULT NULL COMMENT '最小值',
  last_year_value DECIMAL(14,4) DEFAULT NULL COMMENT '去年值',
  year_change_rate DECIMAL(10,4) DEFAULT NULL COMMENT '年同比变化率（%）',
  target_value DECIMAL(14,4) DEFAULT NULL COMMENT '目标值',
  completion_rate DECIMAL(10,4) DEFAULT NULL COMMENT '完成率（%）',
  data_source VARCHAR(100) DEFAULT NULL COMMENT '数据来源',
  calc_time DATETIME DEFAULT NULL COMMENT '计算时间',
  is_valid TINYINT(1) NOT NULL DEFAULT 1 COMMENT '数据是否有效',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (result_id),
  UNIQUE KEY uk_zb_year_org (zb_id, data_year, org_level, org_id),
  KEY idx_zb_year (zb_id, data_year),
  KEY idx_data_year (data_year),
  KEY idx_org (org_level, org_id),
  KEY idx_zb_code (zb_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指标年汇总表';

-- =====================================================
-- Part 9: 数据字典与审计表（新增）
-- =====================================================

-- 表29：bi_data_dict（数据字典表）
CREATE TABLE bi_data_dict (
  dict_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  dict_type VARCHAR(100) NOT NULL COMMENT '字典类型',
  dict_code VARCHAR(100) NOT NULL COMMENT '字典代码',
  dict_name VARCHAR(200) NOT NULL COMMENT '字典名称',
  dict_value VARCHAR(500) DEFAULT NULL COMMENT '字典值',
  parent_code VARCHAR(100) DEFAULT NULL COMMENT '父级代码',
  sort_num INT NOT NULL DEFAULT 99 COMMENT '排序',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (dict_id),
  UNIQUE KEY uk_type_code (dict_type, dict_code),
  KEY idx_dict_type (dict_type),
  KEY idx_parent_code (parent_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典表';

-- 表30：bi_audit_log（数据变更审计表）
CREATE TABLE bi_audit_log (
  audit_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  table_name VARCHAR(100) NOT NULL COMMENT '表名',
  operation_type VARCHAR(20) NOT NULL COMMENT '操作类型（INSERT/UPDATE/DELETE）',
  record_id BIGINT NOT NULL COMMENT '记录ID',
  old_values JSON DEFAULT NULL COMMENT '变更前的值',
  new_values JSON DEFAULT NULL COMMENT '变更后的值',
  changed_fields VARCHAR(500) DEFAULT NULL COMMENT '变更的字段列表',
  operation_user_id BIGINT DEFAULT NULL COMMENT '操作用户ID',
  operation_user_name VARCHAR(100) DEFAULT NULL COMMENT '操作用户名',
  operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  client_ip VARCHAR(50) DEFAULT NULL COMMENT '客户端IP',
  operation_desc VARCHAR(500) DEFAULT NULL COMMENT '操作描述',
  PRIMARY KEY (audit_id),
  KEY idx_table_record (table_name, record_id),
  KEY idx_operation_time (operation_time),
  KEY idx_user_time (operation_user_id, operation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据变更审计表'
PARTITION BY RANGE (TO_DAYS(operation_time)) (
  PARTITION p202501 VALUES LESS THAN (TO_DAYS('2025-02-01')),
  PARTITION p202502 VALUES LESS THAN (TO_DAYS('2025-03-01')),
  PARTITION p202503 VALUES LESS THAN (TO_DAYS('2025-04-01')),
  PARTITION p202504 VALUES LESS THAN (TO_DAYS('2025-05-01')),
  PARTITION p202505 VALUES LESS THAN (TO_DAYS('2025-06-01')),
  PARTITION p_future VALUES LESS THAN MAXVALUE
);