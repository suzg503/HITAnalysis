# BI System Database Design - DBA Optimized Version

## 项目概述

本项目是医院运营决策支持系统（BI System）的数据库设计，经过专业DBA优化，基于原始设计进行了全面的性能、安全性、可扩展性改进。

## 设计理念

### 核心原则

1. **数据安全至上**
   - 所有核心表添加软删除机制（`is_deleted`, `deleted_at`）
   - 完整的审计字段（`created_by`, `updated_by`）
   - 数据变更审计表记录所有关键操作

2. **性能驱动优化**
   - 添加完善的索引设计
   - 日志类表使用分区（按月分区）
   - 创建汇总表预聚合高频查询数据
   - JSON字段改用原生JSON类型

3. **架构适配思维**
   - 指标配置拆分为3个独立表（公式、维度、SQL）
   - 目标值表改进为date类型，支持多种时间粒度
   - 添加汇总表（日/月/年）降低实时查询压力

4. **可扩展性设计**
   - 主键统一使用BIGINT，支持分库分表
   - 分区设计支持数据归档和冷热分离
   - 版本字段支持指标配置变更追溯

## 优化要点总结

### 1. 索引优化（P0优先级）

| 表名 | 新增索引 | 索引类型 | 用途 |
|------|---------|---------|------|
| sys_user | idx_hospital_id, idx_status_deleted | normal | 医院筛选、状态筛选 |
| sys_menu | idx_parent_id, idx_level_sort | normal | 菜单树查询、排序 |
| bi_indicator | idx_parent_zb_id, idx_system_cat | normal | 树形结构、体系分类查询 |
| bi_custom_report | idx_create_user, idx_permission_type | normal | 私有报表、权限筛选 |
| bi_ai_alert | idx_zb_time, idx_handle_status | normal | 指标预警查询、未处理列表 |
| bi_indicator_result_daily | idx_zb_date, idx_data_date | normal | 指标数据查询（高频） |

### 2. 字段优化

- `password` → `password_hash`（更明确字段含义）
- `decimal(18,4)` → `decimal(14,4)`（业务指标精度更合理）
- `text` → `varchar(2000)`（可控长度字段，减少存储开销）
- `year int, month int` → `target_date date`（统一时间类型）
- 添加JSON类型字段用于配置存储

### 3. 分区设计

适用表：
- `sys_log_ai`（AI操作日志）
- `bi_indicator_result_daily`（日汇总）
- `bi_audit_log`（审计日志）

分区策略：按月分区，保留MAXVALUE分区用于新数据

### 4. 表拆分设计

原始 `bi_indicator` 拆分为：
- `bi_indicator`（基础信息）
- `bi_indicator_formula`（公式配置）
- `bi_indicator_dimension`（维度配置）
- `bi_indicator_sql`（SQL配置）

优势：
- 避免单表字段过多
- 支持不同配置类型的独立扩展
- 公式解析结果可缓存

### 5. 汇总表设计

新增汇总表：
- `bi_indicator_result_daily`（日汇总）
- `bi_indicator_result_monthly`（月汇总）
- `bi_indicator_result_yearly`（年汇总）

包含预计算字段：
- 同比环比变化率
- 目标完成率
- 最大最小平均值

### 6. 安全性设计

- 软删除机制（避免误删数据）
- 审计日志表（记录所有变更）
- AI日志添加脱敏字段
- 会话表添加状态和活跃时间

### 7. 存储过程设计

核心存储过程：
- `sp_calc_indicator_change_rate`：自动计算同比环比
- `sp_detect_anomaly_and_alert`：自动检测异常并生成预警
- `sp_cleanup_expired_data`：清理过期数据
- `sp_calc_monthly_summary`：月度汇总计算

### 8. 视图设计

业务视图：
- `v_user_permission`：用户权限视图
- `v_indicator_full`：指标完整信息视图
- `v_indicator_daily_with_change`：带异常判断的日数据视图
- `v_unhandled_alerts`：未处理预警视图
- `v_user_activity_stats`：用户活跃度统计

### 9. 触发器设计

审计触发器：
- 用户表变更审计
- 指标表变更审计
- AI预警处理审计
- 指标结果人工修改审计

自动维护触发器：
- AI会话活跃时间自动更新

## 数据库架构

### 表分类

1. **系统权限类**（5表）
   - sys_user, sys_role, sys_menu, sys_role_menu, sys_user_dept

2. **系统日志类**（4表）
   - sys_log_login, sys_log_menu, sys_log_attention_zb, sys_log_ai

3. **指标管理类**（9表）
   - bi_indicator_system, bi_indicator_category, bi_indicator, 
   - bi_indicator_formula, bi_indicator_dimension, bi_indicator_sql,
   - bi_indicator_drill, bi_indicator_relation, bi_indicator_spec

4. **目标值管理类**（2表）
   - bi_target_setting, bi_target_value

5. **自助分析类**（3表）
   - bi_report_template, bi_custom_report, bi_online_analysis

6. **AI功能类**（3表）
   - bi_ai_session, bi_ai_report, bi_ai_alert

7. **指标汇总类**（3表 + 新增）
   - bi_indicator_result_daily, bi_indicator_result_monthly, bi_indicator_result_yearly

8. **数据字典与审计类**（2表 + 新增）
   - bi_data_dict, bi_audit_log

### 总计：30张核心表 + 4个存储过程 + 7个视图 + 5个触发器

## 使用说明

### 初始化数据库

```bash
# 1. 创建数据库和核心表
mysql -u root -p < sql/01_create_database.sql
mysql -u root -p bi_db < sql/02_create_log_tables.sql
mysql -u root -p bi_db < sql/03_create_indicator_tables.sql
mysql -u root -p bi_db < sql/04_create_target_tables.sql
mysql -u root -p bi_db < sql/05_create_analysis_ai_tables.sql
mysql -u root -p bi_db < sql/06_create_summary_tables.sql

# 2. 创建存储过程、视图、触发器
mysql -u root -p bi_db < sql/07_create_procedures.sql
mysql -u root -p bi_db < sql/08_create_views.sql
mysql -u root -p bi_db < sql/09_create_triggers.sql

# 3. 初始化数据
mysql -u root -p bi_db < sql/10_init_data.sql
```

### 分区维护

```sql
-- 添加新月份分区（每月1日执行）
ALTER TABLE sys_log_ai ADD PARTITION (
  PARTITION p202507 VALUES LESS THAN (TO_DAYS('2025-08-01'))
);

-- 删除旧分区（超过保留期）
ALTER TABLE sys_log_ai DROP PARTITION p202401;
```

### 性能监控

```sql
-- 查看分区使用情况
SELECT PARTITION_NAME, TABLE_ROWS 
FROM information_schema.PARTITIONS 
WHERE TABLE_NAME = 'sys_log_ai' AND TABLE_SCHEMA = 'bi_db';

-- 查看索引使用情况
SELECT INDEX_NAME, CARDINALITY 
FROM information_schema.STATISTICS 
WHERE TABLE_NAME = 'bi_indicator' AND TABLE_SCHEMA = 'bi_db';
```

## 后续优化建议

### P1优先级（中期优化）

1. 冷热数据分离
   - 日志表超过3个月的数据迁移到归档库
   - 使用定期脚本自动迁移

2. 缓存设计
   - 指标元数据缓存到Redis
   - 用户权限缓存到Redis

3. 读写分离
   - 汇总表查询使用从库
   - 实时写入使用主库

### P2优先级（长期优化）

1. 分库分表
   - 按医院分库（如果多医院部署）
   - 指标结果表按年分表

2. 数据湖集成
   - 历史数据迁移到数据湖（如ClickHouse）
   - 实时数据保留在MySQL

## 版本历史

- **v2.0.0** (2025-05-08): DBA优化版，全面改进索引、分区、安全性
- **v1.0.0**: 原始设计（豆包生成）

## 联系方式

如有问题或建议，请联系数据库团队。

---

*"好的数据库设计是性能和安全的基石，不是PPT上的图表。"*
*"备份是生存必需品，优化是持续改进，架构是权衡之选。"*