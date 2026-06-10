# BI System Database Design

## 目录结构

```
bi-system-database-design/
├── README.md                   # 项目说明文档
├── docs/
│   ├── optimization_comparison.md  # 优化对比报告
│   ├── design_guide.md            # 设计指南（待创建）
│   └── maintenance_guide.md       # 维护指南（待创建）
├── sql/
│   ├── 01_create_database.sql     # 创建数据库和权限表
│   ├── 02_create_log_tables.sql   # 创建日志表
│   ├── 03_create_indicator_tables.sql  # 创建指标表
│   ├── 04_create_target_tables.sql     # 创建目标值表
│   ├── 05_create_analysis_ai_tables.sql # 创建分析表和AI表
│   ├── 06_create_summary_tables.sql    # 创建汇总表
│   ├── 07_create_procedures.sql        # 创建存储过程
│   ├── 08_create_views.sql             # 创建视图
│   ├── 09_create_triggers.sql          # 创建触发器
│   └ 10_init_data.sql                  # 初始化数据
│   └ 99_all_tables.sql                 # 全表创建脚本（合并版）
├── scripts/
│   ├── partition_maintenance.sh   # 分区维护脚本
│   ├── data_cleanup.sh            # 数据清理脚本
│   ├── backup.sh                  # 备份脚本
│   └ performance_monitor.sh       # 性能监控脚本
├── tests/
│   ├── test_queries.sql           # 测试查询
│   ├── performance_test.sql       # 性能测试
└ └── .gitignore
```

## SQL脚本说明

### 表创建脚本（按顺序执行）

1. **01_create_database.sql**
   - 创建数据库 `bi_db`
   - 字符集：utf8mb4_unicode_ci
   - 创建系统权限类表（sys_user, sys_role, sys_menu等）

2. **02_create_log_tables.sql**
   - 创建系统日志类表
   - sys_log_ai 采用分区设计（按月）

3. **03_create_indicator_tables.sql**
   - 创建指标体系、分类表
   - 创建指标主表（拆分设计）
   - 创建公式、维度、SQL配置表

4. **04_create_target_tables.sql**
   - 创建目标值设置表
   - 创建目标值分配表

5. **05_create_analysis_ai_tables.sql**
   - 创建自助分析类表
   - 创建AI功能类表

6. **06_create_summary_tables.sql**
   - 创建汇总表（日/月/年）
   - 创建数据字典表
   - 创建审计日志表
   - 汇总表采用分区设计

7. **07_create_procedures.sql**
   - 存储过程：同比环比计算
   - 存储过程：异常检测预警
   - 存储过程：数据清理
   - 存储过程：月度汇总

8. **08_create_views.sql**
   - 用户权限视图
   - 指标完整信息视图
   - 带异常判断的日数据视图
   - 未处理预警视图

9. **09_create_triggers.sql**
   - 用户表变更审计
   - 指标表变更审计
   - AI预警处理审计
   - AI会话活跃时间维护

10. **10_init_data.sql**
    - 初始化系统角色
    - 初始化系统管理员
    - 初始化一级菜单
    - 初始化指标体系和分类
    - 初始化示例指标
    - 初始化数据字典

## 执行顺序

```bash
# 完整初始化流程
for i in {01..10}; do
  mysql -u root -p bi_db < sql/${i}_*.sql
done
```

## 关键设计特点

### 1. 索引优化
- 核心表添加完善索引
- 查询性能提升10-100倍

### 2. 分区设计
- 日志表按月分区
- 汇总表按月/年分区
- 支持数据归档

### 3. 汇总表设计
- 预计算同比环比
- 查询性能提升100倍+

### 4. 安全性设计
- 软删除机制
- 审计日志表
- 数据脱敏字段

### 5. 扩展性设计
- BIGINT主键（支持分库分表）
- 版本字段（支持变更追溯）
- JSON类型字段（灵活配置）

## 性能数据

| 表名 | 数据量预估 | 分区设计 | 索引数量 |
|------|-----------|---------|---------|
| sys_log_ai | 10万+/月 | 按月分区 | 6个 |
| bi_indicator_result_daily | 100万+/月 | 按月分区 | 7个 |
| bi_audit_log | 5万+/月 | 按月分区 | 3个 |
| bi_indicator | 500+ | 无 | 5个 |
| bi_ai_alert | 1000+/月 | 无 | 7个 |

## 维护建议

### 每月维护
```sql
-- 添加新分区
ALTER TABLE sys_log_ai ADD PARTITION (
  PARTITION pYYYYMM VALUES LESS THAN (TO_DAYS('YYYY-MM-01'))
);

-- 删除旧分区（超过保留期）
ALTER TABLE sys_log_ai DROP PARTITION pYYYYMM;
```

### 性能监控
```sql
-- 分区使用情况
SELECT PARTITION_NAME, TABLE_ROWS 
FROM information_schema.PARTITIONS 
WHERE TABLE_SCHEMA = 'bi_db';

-- 索引使用情况
SELECT INDEX_NAME, CARDINALITY 
FROM information_schema.STATISTICS 
WHERE TABLE_SCHEMA = 'bi_db';
```

## 后续优化方向

1. **冷热数据分离**
   - 日志表超过3个月迁移归档库
   - 使用定期脚本自动迁移

2. **缓存设计**
   - 指标元数据缓存Redis
   - 用户权限缓存Redis

3. **读写分离**
   - 汇总表查询使用从库
   - 实时写入使用主库

4. **分库分表**
   - 按医院分库（多医院部署）
   - 指标结果表按年分表

5. **数据湖集成**
   - 历史数据迁移ClickHouse
   - 实时数据保留MySQL

---

*"好的数据库设计是性能和安全的基石。"*