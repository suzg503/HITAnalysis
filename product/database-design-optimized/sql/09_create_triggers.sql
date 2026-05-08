-- =====================================================
-- Part 12: 触发器（新增）
-- =====================================================

-- 触发器1：用户表变更审计
DELIMITER //
CREATE TRIGGER tr_sys_user_audit_insert
 AFTER INSERT ON sys_user
FOR EACH ROW
BEGIN
  INSERT INTO bi_audit_log (
    table_name, operation_type, record_id, new_values,
    operation_user_id, operation_time
  )
  VALUES (
    'sys_user', 'INSERT', NEW.user_id,
    JSON_OBJECT(
      'user_id', NEW.user_id, 'username', NEW.username, 
      'real_name', NEW.real_name, 'role_id', NEW.role_id, 
      'hospital_id', NEW.hospital_id, 'status', NEW.status
    ),
    NEW.created_by, NOW()
  );
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER tr_sys_user_audit_update
 AFTER UPDATE ON sys_user
FOR EACH ROW
BEGIN
  INSERT INTO bi_audit_log (
    table_name, operation_type, record_id, old_values, new_values,
    changed_fields, operation_user_id, operation_time
  )
  VALUES (
    'sys_user', 'UPDATE', NEW.user_id,
    JSON_OBJECT(
      'user_id', OLD.user_id, 'username', OLD.username, 
      'real_name', OLD.real_name, 'role_id', OLD.role_id, 
      'hospital_id', OLD.hospital_id, 'status', OLD.status,
      'is_deleted', OLD.is_deleted
    ),
    JSON_OBJECT(
      'user_id', NEW.user_id, 'username', NEW.username, 
      'real_name', NEW.real_name, 'role_id', NEW.role_id, 
      'hospital_id', NEW.hospital_id, 'status', NEW.status,
      'is_deleted', NEW.is_deleted
    ),
    CONCAT_WS(',',
      IF(OLD.username != NEW.username, 'username', NULL),
      IF(OLD.real_name != NEW.real_name, 'real_name', NULL),
      IF(OLD.role_id != NEW.role_id, 'role_id', NULL),
      IF(OLD.hospital_id != NEW.hospital_id, 'hospital_id', NULL),
      IF(OLD.status != NEW.status, 'status', NULL),
      IF(OLD.is_deleted != NEW.is_deleted, 'is_deleted', NULL)
    ),
    NEW.updated_by, NOW()
  );
END //
DELIMITER ;

-- 触发器2：指标表变更审计
DELIMITER //
CREATE TRIGGER tr_bi_indicator_audit_insert
 AFTER INSERT ON bi_indicator
FOR EACH ROW
BEGIN
  INSERT INTO bi_audit_log (
    table_name, operation_type, record_id, new_values,
    operation_user_id, operation_time, operation_desc
  )
  VALUES (
    'bi_indicator', 'INSERT', NEW.zb_id,
    JSON_OBJECT(
      'zb_id', NEW.zb_id, 'zb_code', NEW.zb_code, 
      'zb_name', NEW.zb_name, 'system_id', NEW.system_id, 
      'cat_id', NEW.cat_id, 'config_type', NEW.config_type,
      'status', NEW.status, 'version', NEW.version
    ),
    NEW.created_by, NOW(),
    CONCAT('新增指标: ', NEW.zb_code, ' - ', NEW.zb_name)
  );
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER tr_bi_indicator_audit_update
 AFTER UPDATE ON bi_indicator
FOR EACH ROW
BEGIN
  -- 版本号自动递增（重大变更时）
  IF OLD.zb_name != NEW.zb_name OR OLD.zb_caliber != NEW.zb_caliber OR OLD.config_type != NEW.config_type THEN
    -- 需要在应用层处理版本递增，这里仅记录变更
    INSERT INTO bi_audit_log (
      table_name, operation_type, record_id, old_values, new_values,
      changed_fields, operation_user_id, operation_time, operation_desc
    )
    VALUES (
      'bi_indicator', 'UPDATE', NEW.zb_id,
      JSON_OBJECT(
        'zb_id', OLD.zb_id, 'zb_code', OLD.zb_code, 
        'zb_name', OLD.zb_name, 'zb_caliber', OLD.zb_caliber,
        'config_type', OLD.config_type, 'status', OLD.status,
        'version', OLD.version
      ),
      JSON_OBJECT(
        'zb_id', NEW.zb_id, 'zb_code', NEW.zb_code, 
        'zb_name', NEW.zb_name, 'zb_caliber', NEW.zb_caliber,
        'config_type', NEW.config_type, 'status', NEW.status,
        'version', NEW.version
      ),
      CONCAT_WS(',',
        IF(OLD.zb_name != NEW.zb_name, 'zb_name', NULL),
        IF(OLD.zb_caliber != NEW.zb_caliber, 'zb_caliber', NULL),
        IF(OLD.config_type != NEW.config_type, 'config_type', NULL),
        IF(OLD.status != NEW.status, 'status', NULL)
      ),
      NEW.updated_by, NOW(),
      CONCAT('更新指标: ', NEW.zb_code, ' - ', NEW.zb_name, ' (版本 ', OLD.version, ' -> ', NEW.version, ')')
    );
  END IF;
END //
DELIMITER ;

-- 触发器3：AI会话活跃时间自动维护
DELIMITER //
CREATE TRIGGER tr_ai_session_update_active_time
 BEFORE UPDATE ON bi_ai_session
FOR EACH ROW
BEGIN
  -- 消息数量增加时，自动更新最后活跃时间
  IF NEW.message_count > OLD.message_count THEN
    SET NEW.last_active_time = NOW();
  END IF;
  
  -- 会话结束时，记录最后活跃时间
  IF NEW.session_status = 2 AND OLD.session_status = 1 THEN
    SET NEW.last_active_time = NOW();
  END IF;
END //
DELIMITER ;

-- 触发器4：AI预警处理状态变更审计
DELIMITER //
CREATE TRIGGER tr_ai_alert_handle_audit
 AFTER UPDATE ON bi_ai_alert
FOR EACH ROW
BEGIN
  -- 仅在处理状态变更时记录
  IF OLD.handle_status != NEW.handle_status AND NEW.handle_status = 1 THEN
    INSERT INTO bi_audit_log (
      table_name, operation_type, record_id, old_values, new_values,
      changed_fields, operation_user_id, operation_time, operation_desc
    )
    VALUES (
      'bi_ai_alert', 'UPDATE', NEW.alert_id,
      JSON_OBJECT(
        'alert_id', OLD.alert_id, 'zb_code', OLD.zb_code,
        'handle_status', OLD.handle_status, 'handle_remark', OLD.handle_remark
      ),
      JSON_OBJECT(
        'alert_id', NEW.alert_id, 'zb_code', NEW.zb_code,
        'handle_status', NEW.handle_status, 'handle_remark', NEW.handle_remark,
        'handle_time', NEW.handle_time
      ),
      'handle_status,handle_remark,handle_time',
      NEW.handle_user_id, NOW(),
      CONCAT('处理预警: ', OLD.zb_name, ' - ', OLD.alert_msg)
    );
  END IF;
END //
DELIMITER ;

-- 触发器5：指标结果变更审计（仅记录人工修改）
DELIMITER //
CREATE TRIGGER tr_indicator_result_daily_audit_update
 AFTER UPDATE ON bi_indicator_result_daily
FOR EACH ROW
BEGIN
  -- 仅在人工修改指标值时记录（calc_time 为 NULL 表示可能是人工修改）
  IF OLD.indicator_value != NEW.indicator_value AND NEW.calc_time IS NULL THEN
    INSERT INTO bi_audit_log (
      table_name, operation_type, record_id, old_values, new_values,
      changed_fields, operation_time, operation_desc
    )
    VALUES (
      'bi_indicator_result_daily', 'UPDATE', NEW.result_id,
      JSON_OBJECT(
        'zb_code', OLD.zb_code, 'data_date', OLD.data_date,
        'org_name', OLD.org_name, 'indicator_value', OLD.indicator_value
      ),
      JSON_OBJECT(
        'zb_code', NEW.zb_code, 'data_date', NEW.data_date,
        'org_name', NEW.org_name, 'indicator_value', NEW.indicator_value
      ),
      'indicator_value',
      NOW(),
      CONCAT('人工修改指标值: ', NEW.zb_code, ' - ', NEW.org_name, ' - ', NEW.data_date)
    );
  END IF;
END //
DELIMITER ;