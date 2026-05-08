# HITAnalysis - 医疗数据 AI 智能分析平台

## 数据库设计文档

**版本：** v1.0  
**状态：** 待评审  
**DBA：** 数据库团队  
**最后更新：** 2026-05-06  
**密级：** 内部机密

---

## 目录

1. [数据库概述](#1-数据库概述)
2. [MySQL数据库设计](#2-mysql数据库设计)
3. [ClickHouse数据库设计](#3-clickhouse数据库设计)
4. [Redis数据结构设计](#4-redis数据结构设计)
5. [Milvus数据结构设计](#5-milvus数据结构设计)
6. [数据同步策略](#6-数据同步策略)
7. [性能优化建议](#7-性能优化建议)

---

## 1. 数据库概述

### 1.1 数据库分层

```
┌─────────────────────────────────────────────────────────────┐
│                      【应用数据层】                           │
│  用户数据、权限数据、报表配置、元数据                         │
│  存储引擎：MySQL 8.0                                         │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│                      【分析数据层】                           │
│  业务数据、指标数据、聚合数据                                 │
│  存储引擎：ClickHouse                                        │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│                      【缓存数据层】                           │
│  会话数据、热点数据、查询结果                                 │
│  存储引擎：Redis 7.x                                         │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│                      【向量数据层】                           │
│  维度/指标向量、业务规则向量                                   │
│  存储引擎：Milvus                                            │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 数据库选型

| 数据库 | 用途 | 理由 |
|--------|------|------|
| **MySQL 8.0** | 存储元数据、权限数据、用户数据 | 成熟稳定、事务支持、团队熟悉 |
| **ClickHouse** | 存储业务数据、指标数据、分析查询 | 列式存储、查询性能强、支持实时分析 |
| **Redis 7.x** | 缓存会话数据、热点数据、查询结果 | 高性能、支持多种数据结构 |
| **Milvus** | 存储维度/指标向量、业务规则向量 | 向量检索性能好、支持大规模数据 |

---

## 2. MySQL数据库设计

### 2.1 用户权限相关表

#### 用户表（user）

```sql
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
  `real_name` VARCHAR(100) COMMENT '真实姓名',
  `email` VARCHAR(100) COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `last_login_time` TIMESTAMP NULL COMMENT '最后登录时间',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  INDEX `idx_email` (`email`),
  INDEX `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

#### 角色表（role）

```sql
CREATE TABLE `role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `code` VARCHAR(50) NOT NULL COMMENT '角色编码',
  `description` VARCHAR(200) COMMENT '角色描述',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';
```

#### 用户角色关联表（user_role）

```sql
CREATE TABLE `user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';
```

#### 用户数据权限表（user_data_permission）

```sql
CREATE TABLE `user_data_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `permission_type` ENUM('self', 'dept', 'hospital', 'cross_hospital', 'all') NOT NULL COMMENT '权限类型',
  `scope_data` JSON COMMENT '权限范围数据（跨院区时存储院区ID列表）',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户数据权限表';
```

#### 用户科室关联表（user_dept）

```sql
CREATE TABLE `user_dept` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `dept_id` BIGINT NOT NULL COMMENT '科室ID',
  `is_primary` BOOLEAN DEFAULT FALSE COMMENT '是否主科室',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户科室关联表';
```

#### 用户院区关联表（user_hospital）

```sql
CREATE TABLE `user_hospital` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `hospital_id` BIGINT NOT NULL COMMENT '院区ID',
  `is_primary` BOOLEAN DEFAULT FALSE COMMENT '是否主院区',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_hospital_id` (`hospital_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户院区关联表';
```

#### AI权限表（ai_permission）

```sql
CREATE TABLE `ai_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `can_use_ai` BOOLEAN DEFAULT TRUE COMMENT '是否允许使用AI',
  `ai_report_visibility` ENUM('private', 'dept', 'hospital', 'all') DEFAULT 'private' COMMENT 'AI报表可见性',
  `ai_export_permission` BOOLEAN DEFAULT FALSE COMMENT 'AI导出权限',
  `daily_query_limit` INT DEFAULT 100 COMMENT '每日查询次数限制',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI权限表';
```

### 2.2 菜单权限相关表

#### 菜单表（menu）

```sql
CREATE TABLE `menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父菜单ID',
  `name` VARCHAR(100) NOT NULL COMMENT '菜单名称',
  `code` VARCHAR(50) NOT NULL COMMENT '菜单编码',
  `path` VARCHAR(200) COMMENT '路由路径',
  `icon` VARCHAR(50) COMMENT '图标',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';
```

#### 角色菜单权限表（role_menu_permission）

```sql
CREATE TABLE `role_menu_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
  INDEX `idx_role_id` (`role_id`),
  INDEX `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单权限表';
```

#### 按钮权限表（button_permission）

```sql
CREATE TABLE `button_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `code` VARCHAR(50) NOT NULL COMMENT '按钮编码',
  `name` VARCHAR(100) NOT NULL COMMENT '按钮名称',
  `menu_id` BIGINT COMMENT '所属菜单ID',
  `description` VARCHAR(200) COMMENT '描述',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  INDEX `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='按钮权限表';
```

#### 角色按钮权限表（role_button_permission）

```sql
CREATE TABLE `role_button_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `button_id` BIGINT NOT NULL COMMENT '按钮ID',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_button` (`role_id`, `button_id`),
  INDEX `idx_role_id` (`role_id`),
  INDEX `idx_button_id` (`button_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色按钮权限表';
```

### 2.3 元数据相关表

#### 维度表（dimension）

```sql
CREATE TABLE `dimension` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '维度ID',
  `name` VARCHAR(100) NOT NULL COMMENT '维度名称',
  `code` VARCHAR(50) NOT NULL COMMENT '维度编码',
  `type` ENUM('org', 'time', 'business') NOT NULL COMMENT '维度类型：org组织 time时间 business业务',
  `level` INT COMMENT '层级',
  `parent_id` BIGINT COMMENT '父维度ID',
  `description` VARCHAR(200) COMMENT '维度描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  INDEX `idx_type` (`type`),
  INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维度表';
```

#### 维度值表（dimension_value）

```sql
CREATE TABLE `dimension_value` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '维度值ID',
  `dimension_id` BIGINT NOT NULL COMMENT '维度ID',
  `code` VARCHAR(50) NOT NULL COMMENT '维度值编码',
  `name` VARCHAR(100) NOT NULL COMMENT '维度值名称',
  `parent_id` BIGINT COMMENT '父维度值ID',
  `level` INT COMMENT '层级',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dimension_code` (`dimension_id`, `code`),
  INDEX `idx_dimension_id` (`dimension_id`),
  INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维度值表';
```

#### 维度别名表（dimension_alias）

```sql
CREATE TABLE `dimension_alias` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `dimension_value_id` BIGINT NOT NULL COMMENT '维度值ID',
  `alias` VARCHAR(100) NOT NULL COMMENT '别名',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_dimension_value_id` (`dimension_value_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维度别名表';
```

#### 指标表（metric）

```sql
CREATE TABLE `metric` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '指标ID',
  `name` VARCHAR(100) NOT NULL COMMENT '指标名称',
  `code` VARCHAR(50) NOT NULL COMMENT '指标编码',
  `category_level1` VARCHAR(50) COMMENT '一级分类',
  `category_level2` VARCHAR(50) COMMENT '二级分类',
  `formula` TEXT COMMENT '计算公式',
  `unit` VARCHAR(20) COMMENT '单位',
  `precision` INT DEFAULT 2 COMMENT '精度',
  `description` VARCHAR(200) COMMENT '指标描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  INDEX `idx_category_level1` (`category_level1`),
  INDEX `idx_category_level2` (`category_level2`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标表';
```

#### 指标维度关联表（metric_dimension）

```sql
CREATE TABLE `metric_dimension` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `metric_id` BIGINT NOT NULL COMMENT '指标ID',
  `dimension_id` BIGINT NOT NULL COMMENT '维度ID',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_metric_dimension` (`metric_id`, `dimension_id`),
  INDEX `idx_metric_id` (`metric_id`),
  INDEX `idx_dimension_id` (`dimension_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标维度关联表';
```

#### 目标值表（target_value）

```sql
CREATE TABLE `target_value` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `metric_id` BIGINT NOT NULL COMMENT '指标ID',
  `dimension_filter` JSON COMMENT '维度过滤条件',
  `target_value` DECIMAL(20, 4) NOT NULL COMMENT '目标值',
  `effective_date` DATE NOT NULL COMMENT '生效日期',
  `expiry_date` DATE COMMENT '失效日期',
  `version` INT DEFAULT 1 COMMENT '版本',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_metric_id` (`metric_id`),
  INDEX `idx_effective_date` (`effective_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='目标值表';
```

### 2.4 报表相关表

#### 标准报表表（standard_report）

```sql
CREATE TABLE `standard_report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '报表ID',
  `name` VARCHAR(100) NOT NULL COMMENT '报表名称',
  `code` VARCHAR(50) NOT NULL COMMENT '报表编码',
  `category_level1` VARCHAR(50) COMMENT '一级分类',
  `category_level2` VARCHAR(50) COMMENT '二级分类',
  `config` JSON NOT NULL COMMENT '报表配置',
  `description` VARCHAR(200) COMMENT '报表描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `created_by` BIGINT COMMENT '创建人',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  INDEX `idx_category_level1` (`category_level1`),
  INDEX `idx_category_level2` (`category_level2`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标准报表表';
```

#### AI报表表（ai_report）

```sql
CREATE TABLE `ai_report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '报表ID',
  `name` VARCHAR(100) NOT NULL COMMENT '报表名称',
  `config` JSON NOT NULL COMMENT '报表配置',
  `visibility` ENUM('private', 'dept', 'hospital', 'all') DEFAULT 'private' COMMENT '可见性',
  `folder_id` BIGINT COMMENT '文件夹ID',
  `status` ENUM('preview', 'saved', 'published', 'archived', 'deleted') DEFAULT 'saved' COMMENT '状态',
  `created_by` BIGINT NOT NULL COMMENT '创建人',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_created_by` (`created_by`),
  INDEX `idx_folder_id` (`folder_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI报表表';
```

#### AI报表文件夹表（ai_report_folder）

```sql
CREATE TABLE `ai_report_folder` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文件夹ID',
  `name` VARCHAR(100) NOT NULL COMMENT '文件夹名称',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父文件夹ID',
  `created_by` BIGINT NOT NULL COMMENT '创建人',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_parent_id` (`parent_id`),
  INDEX `idx_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI报表文件夹表';
```

### 2.5 审计日志表

#### 操作日志表（operation_log）

```sql
CREATE TABLE `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` BIGINT COMMENT '用户ID',
  `username` VARCHAR(50) COMMENT '用户名',
  `operation` VARCHAR(50) NOT NULL COMMENT '操作类型',
  `module` VARCHAR(50) COMMENT '模块',
  `method` VARCHAR(200) COMMENT '方法',
  `params` TEXT COMMENT '参数',
  `ip` VARCHAR(50) COMMENT 'IP地址',
  `status` TINYINT COMMENT '状态：1成功 0失败',
  `error_msg` TEXT COMMENT '错误信息',
  `execution_time` INT COMMENT '执行时间（毫秒）',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_operation` (`operation`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
```

#### AI查询日志表（ai_query_log）

```sql
CREATE TABLE `ai_query_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `query_text` TEXT NOT NULL COMMENT '查询文本',
  `parsed_config` JSON COMMENT '解析配置',
  `generated_sql` TEXT COMMENT '生成的SQL',
  `execution_time` INT COMMENT '执行时间（毫秒）',
  `status` ENUM('success', 'failed', 'timeout') COMMENT '状态',
  `error_msg` TEXT COMMENT '错误信息',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_created_at` (`created_at`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI查询日志表';
```

---

## 3. ClickHouse数据库设计

### 3.1 业务数据表

#### 门诊数据表（outpatient_data）

```sql
CREATE TABLE outpatient_data (
  `id` UInt64,
  `hospital_id` UInt32 COMMENT '院区ID',
  `dept_id` UInt32 COMMENT '科室ID',
  `doctor_id` UInt32 COMMENT '医生ID',
  `patient_id` UInt64 COMMENT '患者ID',
  `visit_date` Date COMMENT '就诊日期',
  `visit_time` DateTime COMMENT '就诊时间',
  `visit_type` Enum8('outpatient' = 1, 'emergency' = 2) COMMENT '就诊类型',
  `diagnosis_code` String COMMENT '诊断编码',
  `diagnosis_name` String COMMENT '诊断名称',
  `total_amount` Decimal(18, 2) COMMENT '总金额',
  `self_pay_amount` Decimal(18, 2) COMMENT '自付金额',
  `insurance_amount` Decimal(18, 2) COMMENT '医保金额',
  `created_at` DateTime DEFAULT now()
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(visit_date)
ORDER BY (hospital_id, dept_id, visit_date)
SETTINGS index_granularity = 8192;
```

#### 住院数据表（inpatient_data）

```sql
CREATE TABLE inpatient_data (
  `id` UInt64,
  `hospital_id` UInt32 COMMENT '院区ID',
  `dept_id` UInt32 COMMENT '科室ID',
  `doctor_id` UInt32 COMMENT '医生ID',
  `patient_id` UInt64 COMMENT '患者ID',
  `admission_date` Date COMMENT '入院日期',
  `discharge_date` Date COMMENT '出院日期',
  `length_of_stay` UInt16 COMMENT '住院天数',
  `diagnosis_code` String COMMENT '诊断编码',
  `diagnosis_name` String COMMENT '诊断名称',
  `total_amount` Decimal(18, 2) COMMENT '总金额',
  `self_pay_amount` Decimal(18, 2) COMMENT '自付金额',
  `insurance_amount` Decimal(18, 2) COMMENT '医保金额',
  `created_at` DateTime DEFAULT now()
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(admission_date)
ORDER BY (hospital_id, dept_id, admission_date)
SETTINGS index_granularity = 8192;
```

#### 药品使用数据表（drug_usage_data）

```sql
CREATE TABLE drug_usage_data (
  `id` UInt64,
  `hospital_id` UInt32 COMMENT '院区ID',
  `dept_id` UInt32 COMMENT '科室ID',
  `doctor_id` UInt32 COMMENT '医生ID',
  `patient_id` UInt64 COMMENT '患者ID',
  `drug_code` String COMMENT '药品编码',
  `drug_name` String COMMENT '药品名称',
  `drug_type` Enum8('antibiotic' = 1, 'normal' = 2) COMMENT '药品类型',
  `usage_date` Date COMMENT '使用日期',
  `usage_time` DateTime COMMENT '使用时间',
  `dosage` Decimal(10, 2) COMMENT '剂量',
  `unit` String COMMENT '单位',
  `amount` Decimal(18, 2) COMMENT '金额',
  `created_at` DateTime DEFAULT now()
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(usage_date)
ORDER BY (hospital_id, dept_id, usage_date, drug_type)
SETTINGS index_granularity = 8192;
```

### 3.2 指标数据表

#### 门诊量指标表（metric_outpatient_count）

```sql
CREATE TABLE metric_outpatient_count (
  `hospital_id` UInt32 COMMENT '院区ID',
  `dept_id` UInt32 COMMENT '科室ID',
  `doctor_id` UInt32 COMMENT '医生ID',
  `date` Date COMMENT '日期',
  `count` UInt32 COMMENT '门诊人次',
  `created_at` DateTime DEFAULT now()
) ENGINE = SummingMergeTree()
PARTITION BY toYYYYMM(date)
ORDER BY (hospital_id, dept_id, doctor_id, date)
SETTINGS index_granularity = 8192;
```

#### 抗生素使用率指标表（metric_antibiotic_usage_rate）

```sql
CREATE TABLE metric_antibiotic_usage_rate (
  `hospital_id` UInt32 COMMENT '院区ID',
  `dept_id` UInt32 COMMENT '科室ID',
  `doctor_id` UInt32 COMMENT '医生ID',
  `date` Date COMMENT '日期',
  `total_count` UInt32 COMMENT '总处方数',
  `antibiotic_count` UInt32 COMMENT '抗生素处方数',
  `usage_rate` Decimal(10, 4) COMMENT '使用率',
  `created_at` DateTime DEFAULT now()
) ENGINE = ReplacingMergeTree()
PARTITION BY toYYYYMM(date)
ORDER BY (hospital_id, dept_id, doctor_id, date)
SETTINGS index_granularity = 8192;
```

### 3.3 物化视图

#### 门诊量日汇总物化视图（mv_outpatient_count_daily）

```sql
CREATE MATERIALIZED VIEW mv_outpatient_count_daily
ENGINE = SummingMergeTree()
PARTITION BY toYYYYMM(date)
ORDER BY (hospital_id, dept_id, doctor_id, date)
AS SELECT
  hospital_id,
  dept_id,
  doctor_id,
  toDate(visit_time) AS date,
  count() AS count,
  now() AS created_at
FROM outpatient_data
GROUP BY hospital_id, dept_id, doctor_id, date;
```

#### 抗生素使用率日汇总物化视图（mv_antibiotic_usage_rate_daily）

```sql
CREATE MATERIALIZED VIEW mv_antibiotic_usage_rate_daily
ENGINE = ReplacingMergeTree()
PARTITION BY toYYYYMM(date)
ORDER BY (hospital_id, dept_id, doctor_id, date)
AS SELECT
  hospital_id,
  dept_id,
  doctor_id,
  toDate(usage_time) AS date,
  count() AS total_count,
  countIf(drug_type = 'antibiotic') AS antibiotic_count,
  antibiotic_count / total_count * 100 AS usage_rate,
  now() AS created_at
FROM drug_usage_data
GROUP BY hospital_id, dept_id, doctor_id, date;
```

---

## 4. Redis数据结构设计

### 4.1 会话管理

#### 用户会话（String）

```
Key: session:{session_id}
Value: {"user_id": 123, "username": "zhangsan", "permissions": [...]}
TTL: 900秒（15分钟）
```

#### AI会话（Hash）

```
Key: ai_session:{session_id}
Fields:
  - user_id: 用户ID
  - query_history: 查询历史（JSON）
  - config: 当前配置（JSON）
  - created_at: 创建时间
TTL: 1800秒（30分钟）
```

### 4.2 权限缓存

#### 用户权限（String）

```
Key: user:permission:{user_id}
Value: {"data_permission": {...}, "menu_permissions": [...], "button_permissions": [...]}
TTL: 3600秒（1小时）
```

#### 用户数据权限（String）

```
Key: user:data_permission:{user_id}
Value: {"type": "dept", "scope_data": [...]}
TTL: 3600秒（1小时）
```

#### 用户AI权限（String）

```
Key: user:ai_permission:{user_id}
Value: {"can_use_ai": true, "ai_report_visibility": "private", "daily_query_limit": 100}
TTL: 3600秒（1小时）
```

### 4.3 查询结果缓存

#### 查询结果（String）

```
Key: query_result:{md5(query_params)}
Value: {"data": [...], "total": 100}
TTL: 300秒（5分钟）
```

#### 热点报表（String）

```
Key: hot_report:{report_id}
Value: {"data": [...], "config": {...}}
TTL: 600秒（10分钟）
```

### 4.4 AI查询次数

#### 每日查询次数（String）

```
Key: user:daily_query_count:{user_id}:{date}
Value: 查询次数
TTL: 86400秒（24小时）
```

---

## 5. Milvus数据结构设计

### 5.1 维度向量集合

#### 集合定义

```python
collection_name = "dimension_vectors"

fields = [
    {
        "name": "id",
        "type": DataType.INT64,
        "is_primary": True,
        "auto_id": True
    },
    {
        "name": "dimension_value_id",
        "type": DataType.INT64,
        "description": "维度值ID"
    },
    {
        "name": "name",
        "type": DataType.VARCHAR,
        "max_length": 100,
        "description": "维度值名称"
    },
    {
        "name": "alias",
        "type": DataType.VARCHAR,
        "max_length": 100,
        "description": "别名"
    },
    {
        "name": "vector",
        "type": DataType.FLOAT_VECTOR,
        "dim": 768,
        "description": "向量"
    }
]

index_params = {
    "index_type": "IVF_FLAT",
    "metric_type": "IP",
    "params": {"nlist": 128}
}
```

### 5.2 指标向量集合

#### 集合定义

```python
collection_name = "metric_vectors"

fields = [
    {
        "name": "id",
        "type": DataType.INT64,
        "is_primary": True,
        "auto_id": True
    },
    {
        "name": "metric_id",
        "type": DataType.INT64,
        "description": "指标ID"
    },
    {
        "name": "name",
        "type": DataType.VARCHAR,
        "max_length": 100,
        "description": "指标名称"
    },
    {
        "name": "description",
        "type": DataType.VARCHAR,
        "max_length": 200,
        "description": "指标描述"
    },
    {
        "name": "vector",
        "type": DataType.FLOAT_VECTOR,
        "dim": 768,
        "description": "向量"
    }
]

index_params = {
    "index_type": "IVF_FLAT",
    "metric_type": "IP",
    "params": {"nlist": 128}
}
```

---

## 6. 数据同步策略

### 6.1 业务数据同步

#### CDC同步流程

```
业务系统（HIS/EMR）
    ↓
CDC（Debezium）
    ↓
消息队列（RocketMQ）
    ↓
数据清洗（ETL）
    ↓
ClickHouse（业务数据仓库）
```

#### 同步频率

| 数据类型 | 同步频率 | 延迟 |
|---------|---------|------|
| 门诊数据 | 实时 | < 1分钟 |
| 住院数据 | 实时 | < 1分钟 |
| 药品使用数据 | 实时 | < 1分钟 |

### 6.2 指标数据同步

#### 定时计算流程

```
ClickHouse（业务数据）
    ↓
定时任务（每天凌晨）
    ↓
指标计算
    ↓
ClickHouse（指标数据）
```

#### 计算频率

| 指标类型 | 计算频率 | 计算时间 |
|---------|---------|---------|
| 门诊量指标 | 每天 | 凌晨2:00 |
| 抗生素使用率指标 | 每天 | 凌晨2:30 |

---

## 7. 性能优化建议

### 7.1 MySQL优化

#### 索引优化

| 表名 | 索引字段 | 索引类型 | 说明 |
|------|---------|---------|------|
| user | username | UNIQUE | 用户名唯一索引 |
| user | email | INDEX | 邮箱索引 |
| user | phone | INDEX | 手机号索引 |
| operation_log | user_id | INDEX | 用户ID索引 |
| operation_log | created_at | INDEX | 创建时间索引 |
| ai_query_log | user_id | INDEX | 用户ID索引 |
| ai_query_log | created_at | INDEX | 创建时间索引 |

#### 查询优化

- 使用EXPLAIN分析慢查询
- 避免SELECT *，只查询需要的字段
- 合理使用JOIN，避免多表关联
- 使用分页查询，避免全表扫描

### 7.2 ClickHouse优化

#### 分区优化

- 按日期分区（PARTITION BY toYYYYMM(date)）
- 每个分区数据量控制在100GB以内
- 定期清理过期分区

#### 排序键优化

- 选择查询频率高的字段作为排序键
- 排序键顺序：高基数 → 低基数
- 避免使用低基数字段作为排序键

#### 物化视图优化

- 使用物化视图预计算常用指标
- 定期刷新物化视图
- 监控物化视图性能

### 7.3 Redis优化

#### 内存优化

- 合理设置TTL，避免内存泄漏
- 使用合适的数据结构（String、Hash、List等）
- 定期清理过期数据

#### 查询优化

- 使用Pipeline批量操作
- 避免大key，拆分为小key
- 使用Lua脚本减少网络往返

---

## 附录

### A. 数据库初始化脚本

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS hitanalysis DEFAULT CHARSET utf8mb4;

-- 使用数据库
USE hitanalysis;

-- 执行所有建表语句
-- （见上文各表定义）
```

### B. 参考文档

- [架构设计文档](./HITAnalysis_ARCHITECTURE_v1.0.md)
- [技术选型报告](./HITAnalysis_TECH_SELECTION_v1.0.md)
- [部署运维文档](./HITAnalysis_DEPLOYMENT_v1.0.md)

---

> **本文档已完整。**  
> 如对本数据库设计有任何疑问或需进一步澄清，请联系数据库团队。
