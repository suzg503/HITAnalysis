-- =====================================================
-- Part 6: 自助分析类表（优化版）
-- =====================================================

-- 表20：bi_report_template（自定义报表模板表）
CREATE TABLE bi_report_template (
  tpl_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  tpl_name VARCHAR(200) NOT NULL COMMENT '模板名称',
  tpl_code VARCHAR(100) DEFAULT NULL COMMENT '模板代码',
  tpl_layout JSON DEFAULT NULL COMMENT '模板布局（JSON）',
  tpl_config JSON DEFAULT NULL COMMENT '模板配置（JSON）',
  tpl_thumbnail VARCHAR(500) DEFAULT NULL COMMENT '模板缩略图',
  is_public TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否公开',
  use_count INT NOT NULL DEFAULT 0 COMMENT '使用次数',
  create_user_id BIGINT NOT NULL COMMENT '创建人ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (tpl_id),
  KEY idx_create_user (create_user_id),
  KEY idx_is_public (is_public),
  KEY idx_use_count (use_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自定义报表模板表';

-- 表21：bi_custom_report（自定义报表表）
CREATE TABLE bi_custom_report (
  report_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  report_name VARCHAR(200) NOT NULL COMMENT '报表名称',
  report_code VARCHAR(100) DEFAULT NULL COMMENT '报表代码',
  tpl_id BIGINT DEFAULT NULL COMMENT '使用的模板ID',
  parent_menu_id BIGINT DEFAULT NULL COMMENT '父级菜单ID',
  report_type TINYINT(1) NOT NULL DEFAULT 1 COMMENT '报表类型',
  permission_type TINYINT(1) NOT NULL DEFAULT 1 COMMENT '权限类型',
  time_granularity TINYINT(1) NOT NULL DEFAULT 1 COMMENT '时间粒度',
  condition_source TINYINT(1) NOT NULL DEFAULT 1 COMMENT '条件来源',
  dept_filter JSON DEFAULT NULL COMMENT '科室筛选配置',
  chart_config JSON DEFAULT NULL COMMENT '图表配置',
  refresh_interval_sec INT DEFAULT NULL COMMENT '刷新间隔（秒）',
  is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否激活',
  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标记',
  deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
  create_user_id BIGINT NOT NULL COMMENT '创建人ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (report_id),
  KEY idx_tpl_id (tpl_id),
  KEY idx_create_user (create_user_id, permission_type),
  KEY idx_permission_type (permission_type),
  KEY idx_parent_menu_id (parent_menu_id),
  KEY idx_active_deleted (is_active, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自定义报表表';

-- 表22：bi_online_analysis（在线分析表）
CREATE TABLE bi_online_analysis (
  analysis_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  analysis_name VARCHAR(200) NOT NULL COMMENT '分析名称',
  analysis_code VARCHAR(100) DEFAULT NULL COMMENT '分析代码',
  analysis_type TINYINT(1) NOT NULL DEFAULT 1 COMMENT '分析类型',
  description VARCHAR(500) DEFAULT NULL COMMENT '描述',
  is_public TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否公开',
  parent_menu_id BIGINT DEFAULT NULL COMMENT '父级菜单ID',
  config_json JSON DEFAULT NULL COMMENT '配置JSON',
  analysis_result_json JSON DEFAULT NULL COMMENT '分析结果JSON',
  execute_count INT NOT NULL DEFAULT 0 COMMENT '执行次数',
  last_execute_time DATETIME DEFAULT NULL COMMENT '最后执行时间',
  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标记',
  deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
  create_user_id BIGINT NOT NULL COMMENT '创建人ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (analysis_id),
  KEY idx_create_user (create_user_id, is_public),
  KEY idx_is_public (is_public),
  KEY idx_parent_menu_id (parent_menu_id),
  KEY idx_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='在线分析表';

-- =====================================================
-- Part 7: AI功能类表（新增 - 优化版）
-- =====================================================

-- 表23：bi_ai_session（AI会话表）
CREATE TABLE bi_ai_session (
  session_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  session_name VARCHAR(200) DEFAULT NULL COMMENT '会话名称',
  session_status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '会话状态：1进行中 2已结束',
  message_count INT NOT NULL DEFAULT 0 COMMENT '消息数量',
  last_active_time DATETIME DEFAULT NULL COMMENT '最后活跃时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (session_id),
  KEY idx_user_id (user_id),
  KEY idx_status_time (session_status, last_active_time),
  KEY idx_last_active_time (last_active_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI会话表';

-- 表24：bi_ai_report（AI生成报告表）
CREATE TABLE bi_ai_report (
  report_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  report_type TINYINT(1) NOT NULL COMMENT '报告类型',
  report_name VARCHAR(200) NOT NULL COMMENT '报告名称',
  start_date DATE DEFAULT NULL COMMENT '开始日期',
  end_date DATE DEFAULT NULL COMMENT '结束日期',
  content_json JSON DEFAULT NULL COMMENT '报告内容JSON',
  summary_text TEXT DEFAULT NULL COMMENT '报告摘要',
  file_url_pdf VARCHAR(500) DEFAULT NULL COMMENT 'PDF下载地址',
  file_url_word VARCHAR(500) DEFAULT NULL COMMENT 'Word下载地址',
  file_url_html VARCHAR(500) DEFAULT NULL COMMENT 'HTML下载地址',
  generation_time_sec INT DEFAULT NULL COMMENT '生成耗时（秒）',
  status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0失败 1成功 2生成中',
  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标记',
  deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (report_id),
  KEY idx_user_id (user_id),
  KEY idx_report_type (report_type, create_time),
  KEY idx_date_range (start_date, end_date),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI生成报告表';

-- 表25：bi_ai_alert（AI预警记录表）
CREATE TABLE bi_ai_alert (
  alert_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  zb_id BIGINT NOT NULL COMMENT '指标ID',
  zb_code VARCHAR(100) NOT NULL COMMENT '指标代码',
  zb_name VARCHAR(255) NOT NULL COMMENT '指标名称',
  alert_type TINYINT(1) NOT NULL COMMENT '预警类型',
  alert_level TINYINT(1) NOT NULL DEFAULT 1 COMMENT '预警级别',
  alert_value DECIMAL(14,4) DEFAULT NULL COMMENT '预警值',
  threshold_value DECIMAL(14,4) DEFAULT NULL COMMENT '阈值',
  reference_value DECIMAL(14,4) DEFAULT NULL COMMENT '参考值',
  change_rate DECIMAL(10,4) DEFAULT NULL COMMENT '变化率（%）',
  alert_msg VARCHAR(1000) DEFAULT NULL COMMENT '预警信息',
  alert_time DATETIME NOT NULL COMMENT '预警时间',
  data_date DATE DEFAULT NULL COMMENT '数据日期',
  org_level TINYINT(1) DEFAULT NULL COMMENT '组织层级',
  org_id BIGINT DEFAULT NULL COMMENT '组织ID',
  org_name VARCHAR(200) DEFAULT NULL COMMENT '组织名称',
  is_push TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否推送',
  push_channel VARCHAR(100) DEFAULT NULL COMMENT '推送渠道',
  push_time DATETIME DEFAULT NULL COMMENT '推送时间',
  handle_status TINYINT(1) NOT NULL DEFAULT 0 COMMENT '处理状态',
  handle_user_id BIGINT DEFAULT NULL COMMENT '处理人ID',
  handle_time DATETIME DEFAULT NULL COMMENT '处理时间',
  handle_remark VARCHAR(500) DEFAULT NULL COMMENT '处理备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (alert_id),
  KEY idx_zb_time (zb_id, alert_time),
  KEY idx_zb_alert_type (zb_id, alert_type),
  KEY idx_alert_level (alert_level, handle_status),
  KEY idx_handle_status (handle_status, create_time),
  KEY idx_alert_time (alert_time),
  KEY idx_data_date (data_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI预警记录表';