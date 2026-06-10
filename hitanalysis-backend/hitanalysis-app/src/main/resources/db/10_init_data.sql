-- =====================================================
-- Part 13: 初始化数据（新增）
-- =====================================================

-- 初始化系统角色
INSERT INTO sys_role (role_id, role_name, role_code, system_name, remark, status) VALUES
(1, '系统管理员', 'admin', '运营决策支持系统', '系统最高权限管理员', 1),
(2, '医院院长', 'dean', '运营决策支持系统', '医院级领导，查看全院数据', 1),
(3, '科室主任', 'dept_director', '运营决策支持系统', '科室级领导，查看本科室数据', 1),
(4, '数据分析师', 'analyst', '运营决策支持系统', '数据分析和报表制作权限', 1),
(5, '普通用户', 'user', '运营决策支持系统', '基础查看权限', 1);

-- 初始化系统管理员用户（密码需要在应用层加密）
INSERT INTO sys_user (user_id, username, password_hash, real_name, role_id, hospital_id, dept_option, status) VALUES
(1, 'admin', '$2a$10$placeholder_hash_need_update', '系统管理员', 1, 0, 1, 1);

-- 初始化一级菜单
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_code, menu_level, link_url, sort_num, status) VALUES
(1, 0, '今日动态', 'today_dynamic', 1, '/actualtime/today_dynamic', 1, 1),
(2, 0, '业务量分析', 'business_analysis', 1, '/analysis/business', 2, 1),
(3, 0, '收入分析', 'revenue_analysis', 1, '/analysis/revenue', 3, 1),
(4, 0, '质量分析', 'quality_analysis', 1, '/analysis/quality', 4, 1),
(5, 0, '效率分析', 'efficiency_analysis', 1, '/analysis/efficiency', 5, 1),
(6, 0, '绩效考核', 'performance', 1, '/performance/assessment', 6, 1),
(7, 0, 'AI智能助手', 'ai_assistant', 1, '/ai/assistant', 7, 1),
(8, 0, '自助分析', 'custom_analysis', 1, '/custom/analysis', 8, 1),
(9, 0, '系统管理', 'system_management', 1, '/system/manage', 9, 1);

-- 初始化角色菜单权限（系统管理员拥有所有权限）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9),
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 8),
(3, 1), (3, 2), (3, 4), (3, 5), (3, 7),
(4, 1), (4, 2), (4, 3), (4, 4), (4, 5), (4, 7), (4, 8),
(5, 1), (5, 7);

-- 初始化指标体系
INSERT INTO bi_indicator_system (system_id, system_code, system_name, remark, status, sort_num) VALUES
(1, 'YWTX', '业务体系', '门诊、住院、手术等业务量相关指标', 1, 1),
(2, 'SRTX', '收入体系', '医疗收入、药品收入等收入相关指标', 1, 2),
(3, 'ZLTX', '质量体系', '医疗质量、服务质量等质量相关指标', 1, 3),
(4, 'XLTX', '效率体系', '床位使用率、周转次数等效率相关指标', 1, 4),
(5, 'KJTX', '成本体系', '医疗成本、运营成本等成本相关指标', 1, 5);

-- 初始化指标分类
INSERT INTO bi_indicator_category (cat_id, system_id, cat_code, cat_name, sort_num, status) VALUES
(1, 1, 'MZFX', '门诊分析', 1, 1),
(2, 1, 'ZYFX', '住院分析', 2, 1),
(3, 1, 'SSFX', '手术分析', 3, 1),
(4, 2, 'YSFX', '医疗收入分析', 1, 1),
(5, 2, 'YPSR', '药品收入分析', 2, 1),
(6, 3, 'YLZL', '医疗质量分析', 1, 1),
(7, 3, 'FWZL', '服务质量分析', 2, 1),
(8, 4, 'CWXL', '床位效率分析', 1, 1),
(9, 4, 'ZYXL', '住院效率分析', 2, 1);

-- 初始化示例指标（基础指标）
INSERT INTO bi_indicator (zb_id, parent_zb_id, zb_code, zb_name, system_id, cat_id, zb_meaning, is_real_time, has_decimal, ratio_type, unit, config_type, status, version) VALUES
(1, 0, 'A001', '门急诊人次', 1, 1, '反映医院门诊和急诊的业务量', 0, 0, '1', '人次', 2, 1, 1),
(2, 0, 'A002', '门诊人次', 1, 1, '反映医院门诊的业务量', 0, 0, '1', '人次', 2, 1, 1),
(3, 0, 'A003', '急诊人次', 1, 1, '反映医院急诊的业务量', 0, 0, '1', '人次', 2, 1, 1),
(4, 0, 'A004', '住院人次', 1, 2, '反映医院住院的业务量', 0, 0, '1', '人次', 2, 1, 1),
(5, 0, 'A005', '出院人次', 1, 2, '反映医院出院的业务量', 0, 0, '1', '人次', 2, 1, 1),
(6, 0, 'A006', '手术台次', 1, 3, '反映医院手术的业务量', 0, 0, '1', '台次', 2, 1, 1),
(7, 0, 'B001', '医疗总收入', 2, 4, '反映医院医疗总收入', 0, 1, '1', '万元', 2, 1, 1),
(8, 0, 'B002', '门诊收入', 2, 4, '反映医院门诊收入', 0, 1, '1', '万元', 2, 1, 1),
(9, 0, 'B003', '住院收入', 2, 4, '反映医院住院收入', 0, 1, '1', '万元', 2, 1, 1),
(10, 0, 'C001', '床位使用率', 4, 8, '反映床位的使用效率', 0, 1, '%', '%', 1, 1, 1);

-- 初始化指标公式配置
INSERT INTO bi_indicator_formula (zb_id, formula_text, dependency_zb_codes, calculation_order, status) VALUES
(1, '@A002+@A003', 'A002,A003', 1, 1),
(10, '@A004/@bed_count*100', 'A004', 2, 1);

-- 初始化指标维度配置
INSERT INTO bi_indicator_dimension (zb_id, fact_table, measure_field, dimension_field, aggregation_type, status) VALUES
(1, 'fact_visit_daily', 'visit_count', 'dept_code,doctor_code', 'SUM', 1),
(2, 'fact_visit_daily', 'visit_count', 'dept_code,doctor_code', 'SUM', 1),
(3, 'fact_visit_daily', 'emergency_count', 'dept_code', 'SUM', 1),
(4, 'fact_admission_daily', 'admission_count', 'dept_code,ward_code', 'SUM', 1),
(5, 'fact_discharge_daily', 'discharge_count', 'dept_code,ward_code', 'SUM', 1),
(6, 'fact_surgery_daily', 'surgery_count', 'dept_code,oper_room', 'SUM', 1),
(7, 'fact_revenue_daily', 'revenue_amount', 'dept_code,revenue_type', 'SUM', 1),
(8, 'fact_revenue_daily', 'revenue_amount', 'dept_code', 'SUM', 1),
(9, 'fact_revenue_daily', 'revenue_amount', 'dept_code', 'SUM', 1);

-- 初始化数据字典
INSERT INTO bi_data_dict (dict_type, dict_code, dict_name, dict_value, sort_num, status) VALUES
('ai_type', '1', '问答', '1', 1, 1),
('ai_type', '2', '自动报告', '2', 2, 1),
('ai_type', '3', '异常预警', '3', 3, 1),
('ai_type', '4', '报表生成', '4', 4, 1),
('ai_type', '5', '指标推荐', '5', 5, 1),
('alert_type', '1', '突增', '1', 1, 1),
('alert_type', '2', '突降', '2', 2, 1),
('alert_type', '3', '超标', '3', 3, 1),
('alert_type', '4', '异常', '4', 4, 1),
('alert_level', '1', '低', '1', 1, 1),
('alert_level', '2', '中', '2', 2, 1),
('alert_level', '3', '高', '3', 3, 1),
('org_level', '1', '全院', '1', 1, 1),
('org_level', '2', '分院', '2', 2, 1),
('org_level', '3', '科室', '3', 3, 1),
('target_type', '1', '年度', '1', 1, 1),
('target_type', '2', '月度', '2', 2, 1),
('target_type', '3', '季度', '3', 3, 1);