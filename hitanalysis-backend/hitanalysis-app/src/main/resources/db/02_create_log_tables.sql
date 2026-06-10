-- =====================================================
-- Part 2: 系统日志类表（优化版）
-- =====================================================

-- 表6：sys_log_login（用户登录日志）
CREATE TABLE sys_log_login (
  log_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  username VARCHAR(50) NOT NULL COMMENT '登录账号',
  login_time DATETIME NOT NULL COMMENT '登录时间',
  logout_time DATETIME DEFAULT NULL COMMENT '退出时间',
  client_ip VARCHAR(50) DEFAULT NULL COMMENT '客户端IP',
  client_info VARCHAR(500) DEFAULT NULL COMMENT '客户端信息（UA）',
  login_status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '登录状态：0失败 1成功',
  fail_reason VARCHAR(200) DEFAULT NULL COMMENT '失败原因',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (log_id),
  KEY idx_user_time (user_id, login_time),
  KEY idx_login_time (login_time),
  KEY idx_login_status (login_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户登录日志表';

-- 表7：sys_log_menu（菜单点击日志）
CREATE TABLE sys_log_menu (
  log_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  menu_id BIGINT NOT NULL COMMENT '菜单ID',
  menu_name VARCHAR(255) NOT NULL COMMENT '菜单名称',
  click_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点击时间',
  session_duration_sec INT DEFAULT NULL COMMENT '停留时长（秒）',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (log_id),
  KEY idx_user_time (user_id, click_time),
  KEY idx_menu_time (menu_id, click_time),
  KEY idx_click_time (click_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单点击日志表';

-- 表8：sys_log_attention_zb（指标关注日志）
CREATE TABLE sys_log_attention_zb (
  log_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  zb_code VARCHAR(100) NOT NULL COMMENT '指标代码',
  zb_name VARCHAR(255) NOT NULL COMMENT '指标名称',
  click_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点击时间',
  click_count INT NOT NULL DEFAULT 1 COMMENT '点击次数',
  action_type TINYINT(1) NOT NULL DEFAULT 1 COMMENT '操作类型：1查看 2收藏 3取消收藏',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (log_id),
  KEY idx_user_zb (user_id, zb_code),
  KEY idx_zb_time (zb_code, click_time),
  KEY idx_click_time (click_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='指标关注日志表';

-- 表9：sys_log_ai（AI操作日志，核心表 - 改进版）
CREATE TABLE sys_log_ai (
  log_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  username VARCHAR(50) NOT NULL COMMENT '登录账号',
  session_id BIGINT DEFAULT NULL COMMENT '会话ID',
  ai_type TINYINT(1) NOT NULL COMMENT 'AI功能类型：1问答 2自动报告 3异常预警 4报表生成 5指标推荐',
  user_input TEXT DEFAULT NULL COMMENT '用户输入原文',
  user_input_masked TEXT DEFAULT NULL COMMENT '用户输入（脱敏版）',
  ai_output TEXT DEFAULT NULL COMMENT 'AI输出内容',
  generated_sql TEXT DEFAULT NULL COMMENT 'AI生成的SQL语句',
  generated_zb_code VARCHAR(500) DEFAULT NULL COMMENT '关联的指标代码',
  generated_report_id BIGINT DEFAULT NULL COMMENT '生成的报表ID',
  model_name VARCHAR(100) DEFAULT NULL COMMENT '使用的AI模型名称',
  token_input INT DEFAULT NULL COMMENT '输入token数',
  token_output INT DEFAULT NULL COMMENT '输出token数',
  status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0失败 1成功 2无数据',
  error_msg VARCHAR(1000) DEFAULT NULL COMMENT '错误信息',
  client_ip VARCHAR(50) DEFAULT NULL COMMENT '客户端IP',
  exec_time_ms INT DEFAULT NULL COMMENT 'AI执行耗时（毫秒）',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (log_id),
  KEY idx_user_time (user_id, create_time),
  KEY idx_ai_type (ai_type, create_time),
  KEY idx_session (session_id),
  KEY idx_status (status, create_time),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI操作日志表'
PARTITION BY RANGE (TO_DAYS(create_time)) (
  PARTITION p202501 VALUES LESS THAN (TO_DAYS('2025-02-01')),
  PARTITION p202502 VALUES LESS THAN (TO_DAYS('2025-03-01')),
  PARTITION p202503 VALUES LESS THAN (TO_DAYS('2025-04-01')),
  PARTITION p202504 VALUES LESS THAN (TO_DAYS('2025-05-01')),
  PARTITION p202505 VALUES LESS THAN (TO_DAYS('2025-06-01')),
  PARTITION p202506 VALUES LESS THAN (TO_DAYS('2025-07-01')),
  PARTITION p_future VALUES LESS THAN MAXVALUE
);