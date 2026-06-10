-- =====================================================
-- Part 11: 视图（新增）
-- =====================================================

-- 视图1：用户权限视图（包含角色和菜单权限）
CREATE VIEW v_user_permission AS
SELECT 
  u.user_id, u.username, u.real_name, u.hospital_id, u.status AS user_status,
  r.role_id, r.role_name, r.role_code,
  m.menu_id, m.menu_name, m.menu_level, m.link_url, m.parent_id
FROM sys_user u
INNER JOIN sys_role r ON u.role_id = r.role_id
INNER JOIN sys_role_menu rm ON r.role_id = rm.role_id
INNER JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE u.is_deleted = 0 AND u.status = 1 
  AND r.is_deleted = 0 AND r.status = 1 
  AND m.is_deleted = 0 AND m.status = 1;

-- 视图2：指标完整信息视图（包含体系、分类、公式、维度）
CREATE VIEW v_indicator_full AS
SELECT 
  i.zb_id, i.zb_code, i.zb_name, i.parent_zb_id, i.status,
  s.system_id, s.system_code, s.system_name,
  c.cat_id, c.cat_code, c.cat_name,
  i.is_real_time, i.has_decimal, i.ratio_type, i.unit,
  i.config_type,
  f.formula_text, f.dependency_zb_codes, f.calculation_order,
  d.fact_table, d.measure_field, d.dimension_field, d.aggregation_type,
  s.sql_text, s.cache_enabled, s.cache_expire_sec,
  i.create_time, i.update_time
FROM bi_indicator i
INNER JOIN bi_indicator_system s ON i.system_id = s.system_id
INNER JOIN bi_indicator_category c ON i.cat_id = c.cat_id
LEFT JOIN bi_indicator_formula f ON i.zb_id = f.zb_id
LEFT JOIN bi_indicator_dimension d ON i.zb_id = d.zb_id
LEFT JOIN bi_indicator_sql s ON i.zb_id = s.zb_id
WHERE i.is_deleted = 0 AND i.status = 1
  AND s.is_deleted = 0 AND s.status = 1
  AND c.is_deleted = 0 AND c.status = 1;

-- 视图3：指标日数据视图（包含同比环比）
CREATE VIEW v_indicator_daily_with_change AS
SELECT 
  d.zb_id, d.zb_code, d.zb_name, d.data_date,
  d.org_level, d.org_id, d.org_code, d.org_name,
  d.indicator_value,
  d.yesterday_value, d.day_change_rate,
  d.last_week_value, d.week_change_rate,
  d.last_month_value, d.month_change_rate,
  d.last_year_value, d.year_change_rate,
  d.target_value, d.completion_rate,
  -- 异常判断字段
  CASE 
    WHEN ABS(d.day_change_rate) > 50 THEN '日环比异常'
    WHEN ABS(d.week_change_rate) > 30 THEN '周同比异常'
    WHEN ABS(d.month_change_rate) > 20 THEN '月同比异常'
    WHEN ABS(d.year_change_rate) > 15 THEN '年同比异常'
    ELSE '正常'
  END AS anomaly_status,
  -- 预警级别
  CASE 
    WHEN ABS(d.day_change_rate) > 100 OR ABS(d.week_change_rate) > 50 THEN '高'
    WHEN ABS(d.day_change_rate) > 50 OR ABS(d.week_change_rate) > 30 THEN '中'
    WHEN ABS(d.month_change_rate) > 20 OR ABS(d.year_change_rate) > 15 THEN '低'
    ELSE '无'
  END AS alert_level
FROM bi_indicator_result_daily d
WHERE d.is_valid = 1;

-- 视图4：指标月度汇总视图
CREATE VIEW v_indicator_monthly_summary AS
SELECT 
  m.zb_id, m.zb_code, m.zb_name, m.data_month,
  m.org_level, m.org_id, m.org_code, m.org_name,
  m.indicator_value, m.indicator_avg, m.indicator_max, m.indicator_min,
  m.last_month_value, m.month_change_rate,
  m.last_year_value, m.year_change_rate,
  m.target_value, m.completion_rate, m.month_progress_rate,
  -- 月度趋势判断
  CASE 
    WHEN m.month_change_rate > 10 THEN '上升'
    WHEN m.month_change_rate < -10 THEN '下降'
    ELSE '平稳'
  END AS trend_status,
  -- 完成状态
  CASE 
    WHEN m.completion_rate >= 100 THEN '超额完成'
    WHEN m.completion_rate >= 80 THEN '基本完成'
    WHEN m.completion_rate >= 60 THEN '进度滞后'
    ELSE '严重滞后'
  END AS completion_status
FROM bi_indicator_result_monthly m
WHERE m.is_valid = 1;

-- 视图5：未处理预警视图
CREATE VIEW v_unhandled_alerts AS
SELECT 
  a.alert_id, a.zb_id, a.zb_code, a.zb_name,
  a.alert_type, a.alert_level, a.alert_value, a.threshold_value, a.change_rate,
  a.alert_msg, a.alert_time, a.data_date,
  a.org_level, a.org_id, a.org_name,
  CASE a.alert_type
    WHEN 1 THEN '突增'
    WHEN 2 THEN '突降'
    WHEN 3 THEN '超标'
    WHEN 4 THEN '异常'
    ELSE '未知'
  END AS alert_type_name,
  CASE a.alert_level
    WHEN 1 THEN '低'
    WHEN 2 THEN '中'
    WHEN 3 THEN '高'
    ELSE '未知'
  END AS alert_level_name,
  a.create_time
FROM bi_ai_alert a
WHERE a.handle_status = 0
ORDER BY a.alert_level DESC, a.alert_time DESC;

-- 视图6：用户活跃度统计视图
CREATE VIEW v_user_activity_stats AS
SELECT 
  u.user_id, u.username, u.real_name, r.role_name,
  COUNT(DISTINCT s.session_id) AS ai_session_count,
  COUNT(l.log_id) AS login_count,
  MAX(l.login_time) AS last_login_time,
  COUNT(m.log_id) AS menu_click_count,
  COUNT(a.log_id) AS ai_query_count,
  SUM(a.exec_time_ms) AS total_ai_exec_time_ms,
  AVG(a.exec_time_ms) AS avg_ai_exec_time_ms
FROM sys_user u
INNER JOIN sys_role r ON u.role_id = r.role_id
LEFT JOIN sys_log_login l ON u.user_id = l.user_id AND l.login_status = 1
LEFT JOIN sys_log_menu m ON u.user_id = m.user_id
LEFT JOIN sys_log_ai a ON u.user_id = a.user_id AND a.status = 1
LEFT JOIN bi_ai_session s ON u.user_id = s.user_id
WHERE u.is_deleted = 0 AND u.status = 1
GROUP BY u.user_id, u.username, u.real_name, r.role_name;

-- 视图7：热门指标视图
CREATE VIEW v_hot_indicators AS
SELECT 
  i.zb_id, i.zb_code, i.zb_name, s.system_name, c.cat_name,
  COUNT(DISTINCT l.user_id) AS view_user_count,
  COUNT(l.log_id) AS view_count,
  MAX(l.click_time) AS last_view_time,
  COUNT(DISTINCT t.user_id) AS theme_use_count
FROM bi_indicator i
INNER JOIN bi_indicator_system s ON i.system_id = s.system_id
INNER JOIN bi_indicator_category c ON i.cat_id = c.cat_id
LEFT JOIN sys_log_attention_zb l ON i.zb_code = l.zb_code
LEFT JOIN bi_theme_indicator t ON i.zb_id = t.zb_id
WHERE i.is_deleted = 0 AND i.status = 1
GROUP BY i.zb_id, i.zb_code, i.zb_name, s.system_name, c.cat_name
ORDER BY view_count DESC, view_user_count DESC
LIMIT 50;