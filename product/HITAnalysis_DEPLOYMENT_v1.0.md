# HITAnalysis - 医疗数据 AI 智能分析平台

## 部署运维文档

**版本：** v1.0  
**状态：** 待评审  
**运维团队：** 运维团队  
**最后更新：** 2026-05-06  
**密级：** 内部机密

---

## 目录

1. [部署概述](#1-部署概述)
2. [环境准备](#2-环境准备)
3. [应用部署](#3-应用部署)
4. [数据库部署](#4-数据库部署)
5. [AI服务部署](#5-ai服务部署)
6. [监控告警](#6-监控告警)
7. [备份恢复](#7-备份恢复)
8. [故障处理](#8-故障处理)
9. [日常运维](#9-日常运维)

---

## 1. 部署概述

### 1.1 部署架构

```
┌─────────────────────────────────────────────────────────────┐
│                        【负载均衡】                          │
│                    Nginx / HAProxy                          │
└────────────────────┬────────────────────────────────────────┘
                     │
        ┌────────────┴────────────┐
        │                         │
┌───────▼────────┐      ┌────────▼────────┐
│  应用实例1      │      │  应用实例2      │
│  (Spring Boot) │      │  (Spring Boot) │
└───────┬────────┘      └────────┬────────┘
        │                        │
        └────────────┬───────────┘
                     │
        ┌────────────┴────────────┐
        │                         │
┌───────▼────────┐      ┌────────▼────────┐
│  MySQL主从     │      │  ClickHouse集群 │
│  (元数据)      │      │  (业务数据)     │
└────────────────┘      └────────┬────────┘
                                 │
                        ┌────────▼────────┐
                        │  Redis集群     │
                        │  (缓存)        │
                        └────────────────┘
```

### 1.2 部署方式

- **容器化部署**：使用Docker + Docker Compose
- **自动化部署**：使用CI/CD流水线
- **灰度发布**：支持蓝绿部署、金丝雀发布

---

## 2. 环境准备

### 2.1 硬件配置

#### 生产环境配置

| 组件 | 数量 | CPU | 内存 | 存储 | GPU | 说明 |
|------|------|-----|------|------|-----|------|
| **应用服务器** | 2台 | 16核 | 32GB | 500GB SSD | - | Spring Boot应用 |
| **数据库服务器** | 2台 | 16核 | 64GB | 2TB SSD | - | MySQL主从 |
| **OLAP服务器** | 3台 | 32核 | 128GB | 4TB SSD | - | ClickHouse集群 |
| **Redis服务器** | 3台 | 8核 | 32GB | 500GB SSD | - | Redis哨兵 |
| **AI服务器** | 1台 | 32核 | 128GB | 2TB SSD | 1×A100 40GB | LLM推理 |
| **消息队列服务器** | 2台 | 8核 | 16GB | 500GB SSD | - | RocketMQ |
| **监控服务器** | 1台 | 8核 | 16GB | 1TB SSD | - | Prometheus + Grafana |
| **日志服务器** | 1台 | 8核 | 16GB | 2TB SSD | - | ELK Stack |

#### 测试环境配置

| 组件 | 数量 | CPU | 内存 | 存储 | GPU | 说明 |
|------|------|-----|------|------|-----|------|
| **应用服务器** | 1台 | 8核 | 16GB | 200GB SSD | - | Spring Boot应用 |
| **数据库服务器** | 1台 | 8核 | 32GB | 500GB SSD | - | MySQL |
| **OLAP服务器** | 1台 | 16核 | 64GB | 1TB SSD | - | ClickHouse |
| **Redis服务器** | 1台 | 4核 | 16GB | 200GB SSD | - | Redis |
| **AI服务器** | 1台 | 16核 | 64GB | 500GB SSD | 1×T4 16GB | LLM推理 |

### 2.2 软件环境

#### 操作系统

| 组件 | 操作系统 | 版本 |
|------|---------|------|
| **应用服务器** | CentOS | 7.9 |
| **数据库服务器** | CentOS | 7.9 |
| **AI服务器** | Ubuntu | 20.04 |

#### 基础软件

| 软件 | 版本 | 用途 |
|------|------|------|
| **Docker** | 24.0+ | 容器化 |
| **Docker Compose** | 2.20+ | 容器编排 |
| **Nginx** | 1.24+ | 负载均衡 |
| **JDK** | 17+ | Java运行环境 |
| **Python** | 3.10+ | AI服务 |
| **CUDA** | 12.0+ | GPU加速 |

---

## 3. 应用部署

### 3.1 Docker Compose配置

#### docker-compose.yml

```yaml
version: '3.8'

services:
  # 应用服务
  app:
    image: hitanalysis/app:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=mysql-master
      - DB_PORT=3306
      - REDIS_HOST=redis-master
      - REDIS_PORT=6379
      - CLICKHOUSE_HOST=clickhouse-node1
      - CLICKHOUSE_PORT=8123
      - AI_SERVICE_URL=http://ai-service:8000
    depends_on:
      - mysql-master
      - redis-master
      - clickhouse-node1
      - ai-service
    deploy:
      replicas: 2
      resources:
        limits:
          cpus: '4'
          memory: 8G
    networks:
      - app-network

  # AI服务
  ai-service:
    image: hitanalysis/ai-service:latest
    ports:
      - "8000:8000"
    environment:
      - MODEL_PATH=/models/qwen2.5-7b-instruct
      - VECTOR_DB_HOST=milvus
      - VECTOR_DB_PORT=19530
    deploy:
      resources:
        reservations:
          devices:
            - driver: nvidia
              count: 1
              capabilities: [gpu]
    volumes:
      - /data/models:/models
    networks:
      - ai-network

  # MySQL主库
  mysql-master:
    image: mysql:8.0
    ports:
      - "3306:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=root_password
      - MYSQL_DATABASE=hitanalysis
    volumes:
      - mysql-master-data:/var/lib/mysql
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    command: --server-id=1 --log-bin=mysql-bin --binlog-format=ROW
    networks:
      - db-network

  # MySQL从库
  mysql-slave:
    image: mysql:8.0
    ports:
      - "3307:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=root_password
      - MYSQL_DATABASE=hitanalysis
    volumes:
      - mysql-slave-data:/var/lib/mysql
    command: --server-id=2 --relay-log=mysql-relay-bin --read-only=1
    depends_on:
      - mysql-master
    networks:
      - db-network

  # ClickHouse
  clickhouse-node1:
    image: clickhouse/clickhouse-server:latest
    ports:
      - "8123:8123"
      - "9000:9000"
    volumes:
      - clickhouse-data:/var/lib/clickhouse
      - ./clickhouse-config.xml:/etc/clickhouse-server/config.xml
    networks:
      - db-network

  # Redis主节点
  redis-master:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - redis-master-data:/data
    networks:
      - cache-network

  # Redis从节点
  redis-slave:
    image: redis:7-alpine
    ports:
      - "6380:6379"
    command: redis-server --slaveof redis-master 6379 --appendonly yes
    volumes:
      - redis-slave-data:/data
    depends_on:
      - redis-master
    networks:
      - cache-network

  # Redis哨兵
  redis-sentinel:
    image: redis:7-alpine
    command: redis-sentinel /etc/redis/sentinel.conf
    volumes:
      - ./sentinel.conf:/etc/redis/sentinel.conf
    depends_on:
      - redis-master
      - redis-slave
    networks:
      - cache-network

  # Milvus向量数据库
  milvus:
    image: milvusdb/milvus:latest
    ports:
      - "19530:19530"
    environment:
      - ETCD_ENDPOINTS=etcd:2379
      - MINIO_ADDRESS=minio:9000
    depends_on:
      - etcd
      - minio
    volumes:
      - milvus-data:/var/lib/milvus
    networks:
      - ai-network

  # etcd（Milvus依赖）
  etcd:
    image: quay.io/coreos/etcd:latest
    environment:
      - ETCD_AUTO_COMPACTION_RETENTION=10
    volumes:
      - etcd-data:/etcd
    networks:
      - ai-network

  # MinIO（Milvus依赖）
  minio:
    image: minio/minio:latest
    ports:
      - "9000:9000"
      - "9001:9001"
    environment:
      - MINIO_ACCESS_KEY=minioadmin
      - MINIO_SECRET_KEY=minioadmin
    command: server /data --console-address ":9001"
    volumes:
      - minio-data:/data
    networks:
      - ai-network

  # RocketMQ NameServer
  rocketmq-nameserver:
    image: apache/rocketmq:4.9.4
    ports:
      - "9876:9876"
    command: sh mqnameserver
    networks:
      - mq-network

  # RocketMQ Broker
  rocketmq-broker:
    image: apache/rocketmq:4.9.4
    ports:
      - "10909:10909"
      - "10911:10911"
    environment:
      - NAMESRV_ADDR=rocketmq-nameserver:9876
    command: sh mqbroker -c /opt/rocketmq/conf/broker.conf
    depends_on:
      - rocketmq-nameserver
    volumes:
      - rocketmq-broker-data:/opt/rocketmq/store
    networks:
      - mq-network

  # Prometheus
  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus-data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
    networks:
      - monitor-network

  # Grafana
  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana-data:/var/lib/grafana
    depends_on:
      - prometheus
    networks:
      - monitor-network

volumes:
  mysql-master-data:
  mysql-slave-data:
  clickhouse-data:
  redis-master-data:
  redis-slave-data:
  milvus-data:
  etcd-data:
  minio-data:
  rocketmq-broker-data:
  prometheus-data:
  grafana-data:

networks:
  app-network:
  db-network:
  cache-network:
  ai-network:
  mq-network:
  monitor-network:
```

### 3.2 部署步骤

#### 1. 拉取镜像

```bash
# 拉取应用镜像
docker pull hitanalysis/app:latest

# 拉取AI服务镜像
docker pull hitanalysis/ai-service:latest

# 拉取基础镜像
docker pull mysql:8.0
docker pull clickhouse/clickhouse-server:latest
docker pull redis:7-alpine
docker pull milvusdb/milvus:latest
```

#### 2. 启动服务

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看服务日志
docker-compose logs -f app
```

#### 3. 健康检查

```bash
# 检查应用服务
curl http://localhost:8080/actuator/health

# 检查AI服务
curl http://localhost:8000/health

# 检查MySQL
docker exec -it mysql-master mysql -uroot -proot_password -e "SELECT 1"

# 检查ClickHouse
docker exec -it clickhouse-node1 clickhouse-client --query "SELECT 1"

# 检查Redis
docker exec -it redis-master redis-cli ping
```

---

## 4. 数据库部署

### 4.1 MySQL部署

#### 主从配置

**主库配置（my-master.cnf）**

```ini
[mysqld]
server-id=1
log-bin=mysql-bin
binlog-format=ROW
gtid-mode=ON
enforce-gtid-consistency=ON
```

**从库配置（my-slave.cnf）**

```ini
[mysqld]
server-id=2
relay-log=mysql-relay-bin
read-only=1
```

#### 主从同步

```bash
# 在主库创建复制用户
mysql -uroot -proot_password -e "CREATE USER 'repl'@'%' IDENTIFIED BY 'repl_password'; GRANT REPLICATION SLAVE ON *.* TO 'repl'@'%';"

# 获取主库binlog位置
mysql -uroot -proot_password -e "SHOW MASTER STATUS;"

# 在从库配置主从同步
mysql -uroot -proot_password -e "CHANGE MASTER TO MASTER_HOST='mysql-master', MASTER_USER='repl', MASTER_PASSWORD='repl_password', MASTER_LOG_FILE='mysql-bin.000001', MASTER_LOG_POS=154;"

# 启动从库同步
mysql -uroot -proot_password -e "START SLAVE;"

# 检查从库同步状态
mysql -uroot -proot_password -e "SHOW SLAVE STATUS\G"
```

### 4.2 ClickHouse部署

#### 集群配置

**clickhouse-config.xml**

```xml
<?xml version="1.0"?>
<yandex>
    <listen_host>::</listen_host>
    <remote_servers>
        <hitanalysis_cluster>
            <shard>
                <replica>
                    <host>clickhouse-node1</host>
                    <port>9000</port>
                </replica>
            </shard>
            <shard>
                <replica>
                    <host>clickhouse-node2</host>
                    <port>9000</port>
                </replica>
            </shard>
            <shard>
                <replica>
                    <host>clickhouse-node3</host>
                    <port>9000</port>
                </replica>
            </shard>
        </hitanalysis_cluster>
    </remote_servers>
</yandex>
```

#### 创建数据库

```bash
# 连接ClickHouse
docker exec -it clickhouse-node1 clickhouse-client

# 创建数据库
CREATE DATABASE hitanalysis;

# 创建表（见数据库设计文档）
```

### 4.3 Redis部署

#### 哨兵配置

**sentinel.conf**

```conf
port 26379
sentinel monitor mymaster redis-master 6379 2
sentinel down-after-milliseconds mymaster 5000
sentinel parallel-syncs mymaster 1
sentinel failover-timeout mymaster 18000
```

#### 启动哨兵

```bash
docker-compose up -d redis-sentinel

# 检查哨兵状态
docker exec -it redis-sentinel redis-cli -p 26379 sentinel master mymaster
```

---

## 5. AI服务部署

### 5.1 模型部署

#### 下载模型

```bash
# 下载Qwen2.5-7B-Instruct模型
mkdir -p /data/models
cd /data/models
git clone https://www.modelscope.cn/qwen/Qwen2.5-7B-Instruct.git
```

#### 模型量化

```bash
# 使用vLLM量化模型
python -m vllm.entrypoints.api_server \
  --model /data/models/Qwen2.5-7B-Instruct \
  --quantization awq \
  --tensor-parallel-size 1 \
  --gpu-memory-utilization 0.9
```

### 5.2 向量数据库部署

#### Milvus部署

```bash
# 启动Milvus
docker-compose up -d milvus etcd minio

# 检查Milvus状态
curl http://localhost:19530/healthz
```

#### 创建集合

```python
from pymilvus import connections, Collection, FieldSchema, CollectionSchema, DataType

# 连接Milvus
connections.connect(host='localhost', port='19530')

# 创建维度向量集合
dimension_fields = [
    FieldSchema(name="id", dtype=DataType.INT64, is_primary=True, auto_id=True),
    FieldSchema(name="dimension_value_id", dtype=DataType.INT64),
    FieldSchema(name="name", dtype=DataType.VARCHAR, max_length=100),
    FieldSchema(name="alias", dtype=DataType.VARCHAR, max_length=100),
    FieldSchema(name="vector", dtype=DataType.FLOAT_VECTOR, dim=768)
]
dimension_schema = CollectionSchema(fields=dimension_fields, description="Dimension vectors")
dimension_collection = Collection(name="dimension_vectors", schema=dimension_schema)

# 创建索引
dimension_collection.create_index(
    field_name="vector",
    index_params={"index_type": "IVF_FLAT", "metric_type": "IP", "params": {"nlist": 128}}
)
```

---

## 6. 监控告警

### 6.1 Prometheus配置

#### prometheus.yml

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

alerting:
  alertmanagers:
    - static_configs:
        - targets: ['alertmanager:9093']

rule_files:
  - "alert_rules.yml"

scrape_configs:
  - job_name: 'spring-boot'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['app:8080']

  - job_name: 'mysql'
    static_configs:
      - targets: ['mysql-exporter:9104']

  - job_name: 'clickhouse'
    static_configs:
      - targets: ['clickhouse-exporter:9116']

  - job_name: 'redis'
    static_configs:
      - targets: ['redis-exporter:9121']

  - job_name: 'ai-service'
    static_configs:
      - targets: ['ai-service:8000']
```

### 6.2 告警规则

#### alert_rules.yml

```yaml
groups:
  - name: application_alerts
    rules:
      - alert: HighCPUUsage
        expr: 100 * (1 - avg(rate(node_cpu_seconds_total{mode="idle"}[5m])) by (instance)) > 80
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High CPU usage detected"
          description: "Instance {{ $labels.instance }} CPU usage is {{ $value }}%"

      - alert: HighMemoryUsage
        expr: (1 - (node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes)) * 100 > 85
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High memory usage detected"
          description: "Instance {{ $labels.instance }} memory usage is {{ $value }}%"

      - alert: HighResponseTime
        expr: histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m])) > 3
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High response time detected"
          description: "P95 response time is {{ $value }}s"

      - alert: HighErrorRate
        expr: rate(http_requests_total{status=~"5.."}[5m]) / rate(http_requests_total[5m]) > 0.01
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value }}%"
```

### 6.3 Grafana配置

#### 数据源配置

1. 登录Grafana（http://localhost:3000）
2. 进入Configuration → Data Sources
3. 添加Prometheus数据源
4. URL: http://prometheus:9090

#### 仪表盘配置

导入预定义仪表盘：
- Spring Boot应用监控
- MySQL监控
- ClickHouse监控
- Redis监控
- AI服务监控

---

## 7. 备份恢复

### 7.1 备份策略

| 数据类型 | 备份频率 | 保留时间 | 备份方式 |
|---------|---------|---------|---------|
| **MySQL数据** | 每日全量 + 每小时增量 | 30天 | mysqldump + binlog |
| **ClickHouse数据** | 每日全量 | 7天 | clickhouse-backup |
| **Redis数据** | 每小时 | 7天 | RDB快照 |
| **Milvus数据** | 每日 | 7天 | MinIO快照 |
| **应用日志** | 每日 | 90天 | ELK归档 |

### 7.2 备份脚本

#### MySQL备份脚本

```bash
#!/bin/bash
# MySQL备份脚本

BACKUP_DIR="/backup/mysql"
DATE=$(date +%Y%m%d_%H%M%S)
MYSQL_HOST="mysql-master"
MYSQL_USER="root"
MYSQL_PASSWORD="root_password"
DATABASE="hitanalysis"

# 创建备份目录
mkdir -p $BACKUP_DIR

# 全量备份
mysqldump -h$MYSQL_HOST -u$MYSQL_USER -p$MYSQL_PASSWORD \
  --single-transaction --routines --triggers \
  $DATABASE > $BACKUP_DIR/full_backup_$DATE.sql

# 压缩备份文件
gzip $BACKUP_DIR/full_backup_$DATE.sql

# 删除30天前的备份
find $BACKUP_DIR -name "full_backup_*.sql.gz" -mtime +30 -delete

echo "MySQL backup completed: full_backup_$DATE.sql.gz"
```

#### ClickHouse备份脚本

```bash
#!/bin/bash
# ClickHouse备份脚本

BACKUP_DIR="/backup/clickhouse"
DATE=$(date +%Y%m%d_%H%M%S)
CLICKHOUSE_HOST="clickhouse-node1"
DATABASE="hitanalysis"

# 创建备份目录
mkdir -p $BACKUP_DIR

# 备份数据
clickhouse-backup create hitanalysis_$DATE

# 删除7天前的备份
clickhouse-backup delete local $(clickhouse-backup list | grep hitanalysis | head -n -7)

echo "ClickHouse backup completed: hitanalysis_$DATE"
```

### 7.3 恢复流程

#### MySQL恢复

```bash
#!/bin/bash
# MySQL恢复脚本

BACKUP_FILE=$1
MYSQL_HOST="mysql-master"
MYSQL_USER="root"
MYSQL_PASSWORD="root_password"
DATABASE="hitanalysis"

# 检查备份文件是否存在
if [ ! -f "$BACKUP_FILE" ]; then
  echo "Backup file not found: $BACKUP_FILE"
  exit 1
fi

# 解压备份文件
gunzip -c $BACKUP_FILE > /tmp/restore.sql

# 恢复数据
mysql -h$MYSQL_HOST -u$MYSQL_USER -p$MYSQL_PASSWORD $DATABASE < /tmp/restore.sql

# 清理临时文件
rm /tmp/restore.sql

echo "MySQL restore completed: $BACKUP_FILE"
```

#### ClickHouse恢复

```bash
#!/bin/bash
# ClickHouse恢复脚本

BACKUP_NAME=$1

# 恢复数据
clickhouse-backup restore $BACKUP_NAME

echo "ClickHouse restore completed: $BACKUP_NAME"
```

---

## 8. 故障处理

### 8.1 常见故障

#### 应用服务无法启动

**排查步骤：**

1. 检查日志
```bash
docker-compose logs app
```

2. 检查端口占用
```bash
netstat -tlnp | grep 8080
```

3. 检查依赖服务
```bash
docker-compose ps
```

4. 重启服务
```bash
docker-compose restart app
```

#### 数据库连接失败

**排查步骤：**

1. 检查MySQL状态
```bash
docker-compose ps mysql-master
```

2. 检查MySQL日志
```bash
docker-compose logs mysql-master
```

3. 测试连接
```bash
docker exec -it mysql-master mysql -uroot -proot_password -e "SELECT 1"
```

4. 检查网络
```bash
docker exec -it app ping mysql-master
```

#### AI服务响应慢

**排查步骤：**

1. 检查GPU使用率
```bash
nvidia-smi
```

2. 检查模型加载状态
```bash
curl http://localhost:8000/health
```

3. 检查推理日志
```bash
docker-compose logs ai-service
```

4. 优化模型（量化、批处理）
```bash
# 使用vLLM量化模型
python -m vllm.entrypoints.api_server \
  --model /data/models/Qwen2.5-7B-Instruct \
  --quantization awq
```

### 8.2 故障切换

#### MySQL主从切换

```bash
# 1. 停止主库
docker-compose stop mysql-master

# 2. 提升从库为主库
docker exec -it mysql-slave mysql -uroot -proot_password -e "STOP SLAVE; RESET SLAVE ALL;"

# 3. 更新应用配置
# 修改DB_HOST为mysql-slave

# 4. 重启应用
docker-compose restart app
```

#### Redis故障切换

```bash
# Redis哨兵自动故障切换，无需手动操作

# 检查哨兵状态
docker exec -it redis-sentinel redis-cli -p 26379 sentinel master mymaster
```

---

## 9. 日常运维

### 9.1 日志管理

#### 查看应用日志

```bash
# 查看实时日志
docker-compose logs -f app

# 查看最近100行日志
docker-compose logs --tail=100 app

# 查看特定时间段的日志
docker-compose logs --since="2026-05-06T00:00:00" --until="2026-05-06T23:59:59" app
```

#### 日志归档

```bash
# 归档日志到ELK
docker-compose logs app | gzip > /backup/logs/app_$(date +%Y%m%d).log.gz

# 删除30天前的日志
find /backup/logs -name "*.log.gz" -mtime +30 -delete
```

### 9.2 性能优化

#### 应用性能优化

```bash
# 调整JVM参数
docker-compose up -d --force-recreate app

# 查看JVM状态
docker exec -it app jps
docker exec -it app jstat -gc <pid> 1000
```

#### 数据库性能优化

```bash
# MySQL慢查询分析
docker exec -it mysql-master mysql -uroot -proot_password -e "SHOW VARIABLES LIKE 'slow_query_log';"
docker exec -it mysql-master mysql -uroot -proot_password -e "SHOW VARIABLES LIKE 'long_query_time';"

# ClickHouse查询优化
docker exec -it clickhouse-node1 clickhouse-client --query "SELECT * FROM system.query_log WHERE type = 'QueryFinish' ORDER BY query_duration_ms DESC LIMIT 10"
```

### 9.3 安全加固

#### 定期更新

```bash
# 更新Docker镜像
docker-compose pull
docker-compose up -d

# 更新系统补丁
yum update -y
```

#### 安全审计

```bash
# 检查开放端口
netstat -tlnp

# 检查用户权限
docker exec -it mysql-master mysql -uroot -proot_password -e "SELECT user, host FROM mysql.user;"

# 检查敏感文件权限
ls -la /etc/passwd /etc/shadow
```

---

## 附录

### A. 端口清单

| 服务 | 端口 | 协议 | 说明 |
|------|------|------|------|
| Nginx | 80, 443 | HTTP/HTTPS | 负载均衡 |
| Spring Boot | 8080 | HTTP | 应用服务 |
| AI Service | 8000 | HTTP | AI服务 |
| MySQL | 3306 | TCP | 数据库 |
| ClickHouse | 8123, 9000 | HTTP/TCP | OLAP引擎 |
| Redis | 6379 | TCP | 缓存 |
| Milvus | 19530 | TCP | 向量数据库 |
| RocketMQ | 9876, 10909, 10911 | TCP | 消息队列 |
| Prometheus | 9090 | HTTP | 监控 |
| Grafana | 3000 | HTTP | 可视化 |

### B. 参考文档

- [架构设计文档](./HITAnalysis_ARCHITECTURE_v1.0.md)
- [技术选型报告](./HITAnalysis_TECH_SELECTION_v1.0.md)
- [数据库设计文档](./HITAnalysis_DATABASE_v1.0.md)

---

> **本文档已完整。**  
> 如对本部署运维有任何疑问或需进一步澄清，请联系运维团队。
