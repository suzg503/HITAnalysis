-- =====================================================
-- Part 10: 存储过程与函数（新增）
-- =====================================================

-- 存储过程1：计算指标的同比环比
DELIMITER //
CREATE PROCEDURE sp_calc_indicator_change_rate(
  IN p_zb_id BIGINT,
  IN p_data_date DATE,
  IN p_org_level TINYINT,
  IN p_org_id BIGINT
)
BEGIN
  DECLARE v_current_value DECIMAL(14,4);
  DECLARE v_yesterday_value DECIMAL(14,4);
  DECLARE v_last_week_value DECIMAL(14,4);
  DECLARE v_last_month_value DECIMAL(14,4);
  DECLARE v_last_year_value DECIMAL(14,4);
  
  -- 获取当前值
  SELECT indicator_value INTO v_current_value
  FROM bi_indicator_result_daily
  WHERE zb_id = p_zb_id AND data_date = p_data_date AND org_level = p_org_level AND org_id = p_org_id;
  
  -- 获取昨日值并计算日环比
  SELECT indicator_value INTO v_yesterday_value
  FROM bi_indicator_result_daily
  WHERE zb_id = p_zb_id AND data_date = DATE_SUB(p_data_date, INTERVAL 1 DAY) AND org_level = p_org_level AND org_id = p_org_id;
  
  -- 获取上周同期值并计算周同比
  SELECT indicator_value INTO v_last_week_value
  FROM bi_indicator_result_daily
  WHERE zb_id = p_zb_id AND data_date = DATE_SUB(p_data_date, INTERVAL 7 DAY) AND org_level = p_org_level AND org_id = p_org_id;
  
  -- 获取上月同期值并计算月同比
  SELECT indicator_value INTO v_last_month_value
  FROM bi_indicator_result_daily
  WHERE zb_id = p_zb_id AND data_date = DATE_SUB(p_data_date, INTERVAL 1 MONTH) AND org_level = p_org_level AND org_id = p_org_id;
  
  -- 获取去年同期值并计算年同比
  SELECT indicator_value INTO v_last_year_value
  FROM bi_indicator_result_daily
  WHERE zb_id = p_zb_id AND data_date = DATE_SUB(p_data_date, INTERVAL 1 YEAR) AND org_level = p_org_level AND org_id = p_org_id;
  
  -- 更新同比环比值
  UPDATE bi_indicator_result_daily
  SET 
    yesterday_value = v_yesterday_value,
    day_change_rate = IF(v_yesterday_value IS NOT NULL AND v_yesterday_value != 0, 
                         ROUND((v_current_value - v_yesterday_value) / v_yesterday_value * 100, 4), NULL),
    last_week_value = v_last_week_value,
    week_change_rate = IF(v_last_week_value IS NOT NULL AND v_last_week_value != 0, 
                          ROUND((v_current_value - v_last_week_value) / v_last_week_value * 100, 4), NULL),
    last_month_value = v_last_month_value,
    month_change_rate = IF(v_last_month_value IS NOT NULL AND v_last_month_value != 0, 
                           ROUND((v_current_value - v_last_month_value) / v_last_month_value * 100, 4), NULL),
    last_year_value = v_last_year_value,
    year_change_rate = IF(v_last_year_value IS NOT NULL AND v_last_year_value != 0, 
                          ROUND((v_current_value - v_last_year_value) / v_last_year_value * 100, 4), NULL),
    calc_time = NOW()
  WHERE zb_id = p_zb_id AND data_date = p_data_date AND org_level = p_org_level AND org_id = p_org_id;
END //
DELIMITER ;

-- 存储过程2：自动检测异常并生成预警
DELIMITER //
CREATE PROCEDURE sp_detect_anomaly_and_alert(
  IN p_zb_id BIGINT,
  IN p_threshold_day DECIMAL(10,4),
  IN p_threshold_week DECIMAL(10,4),
  IN p_threshold_month DECIMAL(10,4)
)
BEGIN
  DECLARE v_done INT DEFAULT FALSE;
  DECLARE v_data_date DATE;
  DECLARE v_org_level TINYINT;
  DECLARE v_org_id BIGINT;
  DECLARE v_zb_code VARCHAR(100);
  DECLARE v_zb_name VARCHAR(255);
  DECLARE v_current_value DECIMAL(14,4);
  DECLARE v_day_change_rate DECIMAL(10,4);
  DECLARE v_week_change_rate DECIMAL(10,4);
  DECLARE v_month_change_rate DECIMAL(10,4);
  
  DECLARE cur_results CURSOR FOR
    SELECT data_date, org_level, org_id, zb_code, zb_name, indicator_value, 
           day_change_rate, week_change_rate, month_change_rate
    FROM bi_indicator_result_daily
    WHERE zb_id = p_zb_id AND data_date = CURDATE() - INTERVAL 1 DAY;
  
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = TRUE;
  
  OPEN cur_results;
  
  read_loop: LOOP
    FETCH cur_results INTO v_data_date, v_org_level, v_org_id, v_zb_code, v_zb_name, 
                            v_current_value, v_day_change_rate, v_week_change_rate, v_month_change_rate;
    
    IF v_done THEN
      LEAVE read_loop;
    END IF;
    
    -- 检测日环比异常
    IF v_day_change_rate IS NOT NULL AND ABS(v_day_change_rate) > ABS(p_threshold_day) THEN
      INSERT INTO bi_ai_alert (
        zb_id, zb_code, zb_name, alert_type, alert_level, alert_value, 
        threshold_value, change_rate, alert_msg, alert_time, data_date, 
        org_level, org_id, org_name, handle_status
      )
      VALUES (
        p_zb_id, v_zb_code, v_zb_name, 
        IF(v_day_change_rate > 0, 1, 2), -- 1突增 2突降
        IF(ABS(v_day_change_rate) > ABS(p_threshold_day) * 2, 3, IF(ABS(v_day_change_rate) > ABS(p_threshold_day) * 1.5, 2, 1)),
        v_current_value, p_threshold_day, v_day_change_rate,
        CONCAT(v_zb_name, ' 日环比变化 ', v_day_change_rate, '%，超过阈值 ', p_threshold_day, '%'),
        NOW(), v_data_date, v_org_level, v_org_id, NULL, 0
      );
    END IF;
    
    -- 检测周同比异常
    IF v_week_change_rate IS NOT NULL AND ABS(v_week_change_rate) > ABS(p_threshold_week) THEN
      INSERT INTO bi_ai_alert (
        zb_id, zb_code, zb_name, alert_type, alert_level, alert_value, 
        threshold_value, change_rate, alert_msg, alert_time, data_date, 
        org_level, org_id, org_name, handle_status
      )
      VALUES (
        p_zb_id, v_zb_code, v_zb_name, 
        IF(v_week_change_rate > 0, 1, 2),
        IF(ABS(v_week_change_rate) > ABS(p_threshold_week) * 2, 3, IF(ABS(v_week_change_rate) > ABS(p_threshold_week) * 1.5, 2, 1)),
        v_current_value, p_threshold_week, v_week_change_rate,
        CONCAT(v_zb_name, ' 周同比变化 ', v_week_change_rate, '%，超过阈值 ', p_threshold_week, '%'),
        NOW(), v_data_date, v_org_level, v_org_id, NULL, 0
      );
    END IF;
    
    -- 检测月同比异常
    IF v_month_change_rate IS NOT NULL AND ABS(v_month_change_rate) > ABS(p_threshold_month) THEN
      INSERT INTO bi_ai_alert (
        zb_id, zb_code, zb_name, alert_type, alert_level, alert_value, 
        threshold_value, change_rate, alert_msg, alert_time, data_date, 
        org_level, org_id, org_name, handle_status
      )
      VALUES (
        p_zb_id, v_zb_code, v_zb_name, 
        IF(v_month_change_rate > 0, 1, 2),
        IF(ABS(v_month_change_rate) > ABS(p_threshold_month) * 2, 3, IF(ABS(v_month_change_rate) > ABS(p_threshold_month) * 1.5, 2, 1)),
        v_current_value, p_threshold_month, v_month_change_rate,
        CONCAT(v_zb_name, ' 月同比变化 ', v_month_change_rate, '%，超过阈值 ', p_threshold_month, '%'),
        NOW(), v_data_date, v_org_level, v_org_id, NULL, 0
      );
    END IF;
    
  END LOOP;
  
  CLOSE cur_results;
END //
DELIMITER ;

-- 存储过程3：清理过期会话和日志
DELIMITER //
CREATE PROCEDURE sp_cleanup_expired_data(
  IN p_session_expire_days INT,
  IN p_log_retention_months INT
)
BEGIN
  -- 清理过期AI会话
  UPDATE bi_ai_session
  SET session_status = 2
  WHERE session_status = 1 
    AND last_active_time < DATE_SUB(NOW(), INTERVAL p_session_expire_days DAY);
  
  -- 清理超过保留期的日志（仅标记删除，不物理删除）
  -- 登录日志
  UPDATE sys_log_login
  SET status = 2 -- 标记为归档状态
  WHERE create_time < DATE_SUB(NOW(), INTERVAL p_log_retention_months MONTH);
  
  -- 菜单点击日志
  UPDATE sys_log_menu
  SET status = 2
  WHERE create_time < DATE_SUB(NOW(), INTERVAL p_log_retention_months MONTH);
  
  -- AI操作日志
  UPDATE sys_log_ai
  SET status = 2
  WHERE create_time < DATE_SUB(NOW(), INTERVAL p_log_retention_months MONTH);
END //
DELIMITER ;

-- 存储过程4：月度汇总计算
DELIMITER //
CREATE PROCEDURE sp_calc_monthly_summary(
  IN p_data_month DATE -- yyyy-mm-01
)
BEGIN
  INSERT INTO bi_indicator_result_monthly (
    zb_id, zb_code, zb_name, data_month, org_level, org_id, org_code, org_name,
    indicator_value, indicator_count, indicator_avg, indicator_max, indicator_min,
    last_month_value, month_change_rate, last_year_value, year_change_rate,
    target_value, completion_rate, calc_time
  )
  SELECT 
    d.zb_id, d.zb_code, d.zb_name, p_data_month, d.org_level, d.org_id, d.org_code, d.org_name,
    SUM(d.indicator_value) AS indicator_value,
    SUM(d.indicator_count) AS indicator_count,
    AVG(d.indicator_value) AS indicator_avg,
    MAX(d.indicator_value) AS indicator_max,
    MIN(d.indicator_value) AS indicator_min,
    m.indicator_value AS last_month_value,
    IF(m.indicator_value IS NOT NULL AND m.indicator_value != 0, 
       ROUND((SUM(d.indicator_value) - m.indicator_value) / m.indicator_value * 100, 4), NULL) AS month_change_rate,
    y.indicator_value AS last_year_value,
    IF(y.indicator_value IS NOT NULL AND y.indicator_value != 0, 
       ROUND((SUM(d.indicator_value) - y.indicator_value) / y.indicator_value * 100, 4), NULL) AS year_change_rate,
    t.target_value,
    IF(t.target_value IS NOT NULL AND t.target_value != 0, 
       ROUND(SUM(d.indicator_value) / t.target_value * 100, 4), NULL) AS completion_rate,
    NOW() AS calc_time
  FROM bi_indicator_result_daily d
  LEFT JOIN bi_indicator_result_monthly m 
    ON d.zb_id = m.zb_id AND d.org_level = m.org_level AND d.org_id = m.org_id 
    AND m.data_month = DATE_SUB(p_data_month, INTERVAL 1 MONTH)
  LEFT JOIN bi_indicator_result_monthly y 
    ON d.zb_id = y.zb_id AND d.org_level = y.org_level AND d.org_id = y.org_id 
    AND y.data_month = DATE_SUB(p_data_month, INTERVAL 1 YEAR)
  LEFT JOIN bi_target_value t 
    ON d.zb_id = t.zb_id AND d.org_level = t.org_level AND d.org_id = t.org_id 
    AND t.target_date = p_data_month AND t.target_type = 2
  WHERE d.data_date >= p_data_month AND d.data_date < DATE_ADD(p_data_month, INTERVAL 1 MONTH)
  GROUP BY d.zb_id, d.zb_code, d.zb_name, d.org_level, d.org_id, d.org_code, d.org_name
  ON DUPLICATE KEY UPDATE
    indicator_value = VALUES(indicator_value),
    indicator_count = VALUES(indicator_count),
    indicator_avg = VALUES(indicator_avg),
    indicator_max = VALUES(indicator_max),
    indicator_min = VALUES(indicator_min),
    last_month_value = VALUES(last_month_value),
    month_change_rate = VALUES(month_change_rate),
    last_year_value = VALUES(last_year_value),
    year_change_rate = VALUES(year_change_rate),
    target_value = VALUES(target_value),
    completion_rate = VALUES(completion_rate),
    calc_time = NOW();
END //
DELIMITER ;