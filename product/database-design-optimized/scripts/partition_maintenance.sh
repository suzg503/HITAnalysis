#!/bin/bash
# =====================================================
# 分区维护脚本 - 每月执行
# 功能：添加新分区、删除旧分区
# =====================================================

set -e

# 数据库连接信息
DB_HOST="localhost"
DB_PORT="3306"
DB_USER="root"
DB_PASS=""  # 需要在执行时输入
DB_NAME="bi_db"

# 分区表列表
PARTITION_TABLES=(
  "sys_log_ai"
  "bi_indicator_result_daily"
  "bi_audit_log"
)

# 分区保留月数（超过此数量的分区将被删除）
RETENTION_MONTHS=12

# 获取当前年月
CURRENT_YEAR=$(date +%Y)
CURRENT_MONTH=$(date +%m)
NEXT_MONTH=$(date -d "next month" +%m)
NEXT_YEAR=$(date -d "next month" +%Y)

# 计算下个月的第一天（分区边界）
NEXT_MONTH_FIRST_DAY="${NEXT_YEAR}-${NEXT_MONTH}-01"

echo "======================================================"
echo "分区维护脚本 - $(date)"
echo "======================================================"
echo "当前时间: ${CURRENT_YEAR}-${CURRENT_MONTH}"
echo "下月第一天: ${NEXT_MONTH_FIRST_DAY}"
echo "保留月数: ${RETENTION_MONTHS}"
echo "======================================================"

# 函数：添加新分区
add_new_partition() {
  local table=$1
  local partition_name="p${NEXT_YEAR}${NEXT_MONTH}"
  local partition_value="TO_DAYS('${NEXT_MONTH_FIRST_DAY}')"
  
  echo ">>> 添加分区: $table - $partition_name"
  
  mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "
    ALTER TABLE $table ADD PARTITION (
      PARTITION $partition_name VALUES LESS THAN ($partition_value)
    );
  "
  
  echo ">>> 分区添加成功"
}

# 函数：删除旧分区
delete_old_partition() {
  local table=$1
  local delete_year=$(date -d "$RETENTION_MONTHS months ago" +%Y)
  local delete_month=$(date -d "$RETENTION_MONTHS months ago" +%m)
  local partition_name="p${delete_year}${delete_month}"
  
  echo ">>> 检查旧分区: $table - $partition_name"
  
  # 检查分区是否存在
  local partition_exists=$(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" -N -e "
    SELECT COUNT(*) 
    FROM information_schema.PARTITIONS 
    WHERE TABLE_SCHEMA='$DB_NAME' 
      AND TABLE_NAME='$table'
      AND PARTITION_NAME='$partition_name';
  ")
  
  if [ "$partition_exists" -gt 0 ]; then
    echo ">>> 删除分区: $table - $partition_name"
    
    mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "
      ALTER TABLE $table DROP PARTITION $partition_name;
    "
    
    echo ">>> 分区删除成功"
  else
    echo ">>> 分区不存在，跳过"
  fi
}

# 函数：查看分区列表
show_partitions() {
  local table=$1
  
  echo ">>> 当前分区列表: $table"
  
  mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "
    SELECT 
      PARTITION_NAME,
      PARTITION_DESCRIPTION,
      TABLE_ROWS,
      DATA_LENGTH/1024/1024 AS 'DATA_SIZE(MB)',
      INDEX_LENGTH/1024/1024 AS 'INDEX_SIZE(MB)'
    FROM information_schema.PARTITIONS
    WHERE TABLE_SCHEMA='$DB_NAME' AND TABLE_NAME='$table'
    ORDER BY PARTITION_ORDINAL_POSITION;
  "
}

# 主流程
echo "======================================================"
echo "步骤1: 添加新分区"
echo "======================================================"

for table in "${PARTITION_TABLES[@]}"; do
  add_new_partition "$table"
done

echo ""
echo "======================================================"
echo "步骤2: 删除旧分区"
echo "======================================================"

for table in "${PARTITION_TABLES[@]}"; do
  delete_old_partition "$table"
done

echo ""
echo "======================================================"
echo "步骤3: 查看分区状态"
echo "======================================================"

for table in "${PARTITION_TABLES[@]}"; do
  echo ""
  show_partitions "$table"
done

echo ""
echo "======================================================"
echo "分区维护完成 - $(date)"
echo "======================================================"

# 使用说明
# 执行方式：
# chmod +x scripts/partition_maintenance.sh
# ./scripts/partition_maintenance.sh
# 
# 或添加到crontab（每月1日执行）：
# 0 2 1 * * /path/to/scripts/partition_maintenance.sh >> /var/log/partition_maintenance.log 2>&1