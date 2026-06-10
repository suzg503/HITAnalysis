# HITAnalysis - 部署与运维手册

## 📋 文档概述

本文档提供HITAnalysis系统的生产环境部署指南和日常运维操作手册。

## 🚀 生产环境部署

### 1. 环境准备

#### 系统要求
- **操作系统**: Linux (推荐 CentOS 7+/Ubuntu 18.04+) 或 Windows Server 2016+
- **内存**: 最低8GB，推荐16GB+
- **CPU**: 最低4核，推荐8核+
- **磁盘**: 最低50GB，推荐100GB+ SSD

#### 软件环境
- **JDK 17** (OpenJDK 或 Oracle JDK)
- **MySQL 8.0+** (推荐主从复制架构)
- **Redis 7.0+** (推荐哨兵或集群模式)
- **Nginx 1.20+** (前端反向代理)

#### 安装必要软件

**Linux系统**:
```bash
# 安装JDK 17
yum install java-17-openjdk java-17-openjdk-devel  # CentOS
apt install openjdk-17-jdk                          # Ubuntu

# 安装MySQL 8.0
yum install mysql-server                            # CentOS
apt install mysql-server                            # Ubuntu

# 安装Redis
yum install redis                                   # CentOS
apt install redis-server                            # Ubuntu

# 安装Nginx
yum install nginx                                   # CentOS
apt install nginx                                   # Ubuntu
```

**Windows系统**:
- JDK 17: https://adoptium.net/
- MySQL: https://dev.mysql.com/downloads/mysql/
- Redis: https://github.com/microsoftarchive/redis/releases
- Nginx: http://nginx.org/en/download.html

### 2. 数据库部署

#### MySQL配置优化

创建配置文件 `/etc/my.cnf.d/hitanalysis.cnf` (Linux) 或 MySQL配置文件：

```ini
[mysqld]
# 基础配置
port = 3306
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci
default-storage-engine = INNODB

# 性能配置
max_connections = 500
innodb_buffer_pool_size = 4G         # 根据内存调整，建议总内存的70%
innodb_log_file_size = 1G
innodb_flush_log_at_trx_commit = 2
innodb_flush_method = O_DIRECT

# 查询优化
query_cache_type = 1
query_cache_size = 128M
tmp_table_size = 256M
max_heap_table_size = 256M

# 日志配置
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 2

# 安全配置
skip-name-resolve
bind-address = 127.0.0.1            # 仅本地访问，如需远程访问改为0.0.0.0
```

#### 初始化数据库

```bash
# 下载项目
git clone https://github.com/your-org/HITAnalysis.git
cd HITAnalysis/product/database-design-optimized/sql

# 执行初始化脚本
bash init-database.sh

# 验证数据库
mysql -u root -p -e "USE bi_db; SHOW TABLES; SELECT COUNT(*) FROM sys_user;"
```

#### 创建应用专用用户

```sql
-- 创建应用数据库用户
CREATE USER 'hitanalysis'@'%' IDENTIFIED BY 'Strong_Password_2026!';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX
ON bi_db.* TO 'hitanalysis'@'%';
FLUSH PRIVILEGES;

-- 验证权限
SHOW GRANTS FOR 'hitanalysis'@'%';
```

### 3. Redis部署

#### Redis配置

编辑 `/etc/redis.conf` (Linux) 或 `redis.windows.conf` (Windows):

```ini
# 基础配置
bind 127.0.0.1
port 6379
daemonize yes                        # Linux后台运行

# 性能配置
maxmemory 2gb                        # 根据系统内存调整
maxmemory-policy allkeys-lru
timeout 300

# 安全配置（必须设置密码）
requirepass Strong_Redis_Password_2026!

# 持久化配置
save 900 1
save 300 10
save 60 10000
appendonly yes
appendfsync everysec

# 日志配置
logfile /var/log/redis/redis.log     # Linux
loglevel notice
```

#### 启动Redis

```bash
# Linux
systemctl start redis
systemctl enable redis               # 开机自启
systemctl status redis               # 检查状态

# Windows
redis-server.exe redis.windows.conf
```

#### 验证Redis

```bash
redis-cli -a Strong_Redis_Password_2026! ping
# 应返回: PONG
```

### 4. 后端应用部署

#### 准备应用配置

创建生产环境配置文件 `application-prod.yml`:

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  profiles:
    active: prod

  datasource:
    url: jdbc:mysql://localhost:3306/bi_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: hitanalysis              # 使用专用用户
    password: Strong_Password_2026!    # 使用强密码
    druid:
      initial-size: 10
      min-idle: 10
      max-active: 50                  # 生产环境增加连接数

  data:
    redis:
      host: localhost
      port: 6379
      password: Strong_Redis_Password_2026!  # Redis密码

jwt:
  secret: HITANALYSIS-PRODUCTION-JWT-SECRET-KEY-CHANGE-THIS-IN-PRODUCTION  # 必须修改
  expiration: 86400000

logging:
  level:
    root: WARN                        # 生产环境降低日志级别
    com.hitanalysis: INFO
  file:
    name: /var/log/hitanalysis/application.log
```

#### 构建应用

```bash
cd hitanalysis-backend

# Maven构建
mvn clean package -Dmaven.test.skip=true -Pprod

# 构建产物位置
ls hitanalysis-app/target/hitanalysis-app-1.0.0-SNAPSHOT.jar
```

#### 部署应用

**Linux系统**:

```bash
# 创建应用目录
mkdir -p /opt/hitanalysis/{app,logs,config}

# 复制应用文件
cp hitanalysis-app/target/hitanalysis-app-1.0.0-SNAPSHOT.jar /opt/hitanalysis/app/
cp application-prod.yml /opt/hitanalysis/config/

# 创建启动脚本
cat > /opt/hitanalysis/start.sh << 'EOF'
#!/bin/bash
APP_NAME="hitanalysis-app-1.0.0-SNAPSHOT.jar"
APP_HOME="/opt/hitanalysis"
LOG_HOME="$APP_HOME/logs"
JAR_FILE="$APP_HOME/app/$APP_NAME"
CONFIG_FILE="$APP_HOME/config/application-prod.yml"

nohup java -Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -Dspring.config.location=$CONFIG_FILE \
  -Dlogging.file.name=$LOG_HOME/application.log \
  -jar $JAR_FILE \
  > $LOG_HOME/console.log 2>&1 &

echo $! > $APP_HOME/app.pid
echo "Application started. PID: $(cat $APP_HOME/app.pid)"
EOF

chmod +x /opt/hitanalysis/start.sh

# 创建停止脚本
cat > /opt/hitanalysis/stop.sh << 'EOF'
#!/bin/bash
APP_HOME="/opt/hitanalysis"
PID_FILE="$APP_HOME/app.pid"

if [ -f $PID_FILE ]; then
  PID=$(cat $PID_FILE)
  kill -15 $PID
  sleep 5
  if [ -f $PID_FILE ]; then
    rm -f $PID_FILE
  fi
  echo "Application stopped (PID: $PID)"
else
  echo "PID file not found"
fi
EOF

chmod +x /opt/hitanalysis/stop.sh

# 启动应用
/opt/hitanalysis/start.sh

# 检查状态
curl http://localhost:8080/api/actuator/health  # 如果启用了Actuator
```

**Windows系统**:

```powershell
# 创建目录
New-Item -ItemType Directory -Force -Path C:\HITAnalysis\app, C:\HITAnalysis\logs, C:\HITAnalysis\config

# 复制文件
Copy-Item hitanalysis-app\target\hitanalysis-app-1.0.0-SNAPSHOT.jar C:\HITAnalysis\app\
Copy-Item application-prod.yml C:\HITAnalysis\config\

# 启动应用（PowerShell）
Start-Process java -ArgumentList `
  "-Xms2g", "-Xmx4g", `
  "-Dspring.config.location=C:\HITAnalysis\config\application-prod.yml", `
  "-jar", "C:\HITAnalysis\app\hitanalysis-app-1.0.0-SNAPSHOT.jar" `
  -RedirectStandardOutput "C:\HITAnalysis\logs\console.log" `
  -RedirectStandardError "C:\HITAnalysis\logs\error.log"
```

#### 配置Systemd服务（Linux推荐）

```bash
# 创建systemd服务文件
cat > /etc/systemd/system/hitanalysis.service << 'EOF'
[Unit]
Description=HITAnalysis Application
After=mysql.service redis.service

[Service]
Type=simple
User=hitanalysis                      # 创建专用用户
Group=hitanalysis
WorkingDirectory=/opt/hitanalysis
ExecStart=/usr/bin/java -Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -Dspring.config.location=/opt/hitanalysis/config/application-prod.yml \
  -jar /opt/hitanalysis/app/hitanalysis-app-1.0.0-SNAPSHOT.jar
ExecStop=/bin/kill -15 $MAINPID
Restart=on-failure
RestartSec=10
StandardOutput=file:/opt/hitanalysis/logs/console.log
StandardError=file:/opt/hitanalysis/logs/error.log

[Install]
WantedBy=multi-user.target
EOF

# 创建专用用户
useradd -r -s /bin/false hitanalysis
chown -R hitanalysis:hitanalysis /opt/hitanalysis

# 启动服务
systemctl daemon-reload
systemctl start hitanalysis
systemctl enable hitanalysis          # 开机自启
systemctl status hitanalysis          # 检查状态
```

### 5. 前端部署

#### 构建前端

```bash
cd hitanalysis-frontend

# 配置生产环境变量
cat > .env.production << 'EOF'
VITE_API_BASE_URL=https://your-domain.com/api
VITE_APP_TITLE=HITAnalysis
EOF

# 构建生产版本
npm run build

# 构建产物位置
ls dist/
```

#### Nginx配置

创建Nginx配置文件 `/etc/nginx/conf.d/hitanalysis.conf`:

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 强制HTTPS（推荐）
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com;

    # SSL证书配置（必须）
    ssl_certificate /etc/nginx/ssl/your-domain.crt;
    ssl_certificate_key /etc/nginx/ssl/your-domain.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    # 前端静态文件
    root /opt/hitanalysis-frontend/dist;
    index index.html;

    # 前端路由
    location / {
        try_files $uri $uri/ /index.html;
        add_header Cache-Control "no-cache, no-store, must-revalidate";
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    # 后端API代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # 超时配置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;

        # 缓存配置（可选）
        proxy_cache_valid 200 10m;
    }

    # API文档（可选，生产环境建议关闭）
    location /api/doc.html {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        # 建议添加IP白名单限制
        allow 10.0.0.0/8;               # 内网访问
        deny all;
    }

    # 安全配置
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;

    # 日志配置
    access_log /var/log/nginx/hitanalysis-access.log;
    error_log /var/log/nginx/hitanalysis-error.log;
}
```

#### 部署前端文件

```bash
# 创建前端目录
mkdir -p /opt/hitanalysis-frontend

# 复制前端构建产物
cp -r hitanalysis-frontend/dist /opt/hitanalysis-frontend/

# 设置权限
chown -R nginx:nginx /opt/hitanalysis-frontend

# 启动Nginx
systemctl start nginx
systemctl enable nginx
systemctl status nginx
```

### 6. 验证部署

#### 检查服务状态

```bash
# 后端应用
curl http://localhost:8080/api/actuator/health  # 或访问任意API端点
curl http://localhost:8080/api/v1/users?pageNum=1&pageSize=10

# Nginx状态
systemctl status nginx
curl -I https://your-domain.com

# MySQL状态
systemctl status mysql
mysql -u hitanalysis -p -e "SELECT 1;"

# Redis状态
systemctl status redis
redis-cli -a Strong_Redis_Password_2026! ping
```

#### 访问系统

- **前端**: https://your-domain.com
- **API文档**: https://your-domain.com/api/doc.html (建议关闭或限制访问)
- **健康检查**: https://your-domain.com/api/actuator/health

## 🛠️ 运维操作手册

### 1. 日志管理

#### 日志文件位置
- **后端应用**: `/opt/hitanalysis/logs/`
  - `application.log`: 应用主日志
  - `console.log`: 控制台输出
  - `error.log`: 错误日志

- **Nginx**: `/var/log/nginx/`
  - `hitanalysis-access.log`: 访问日志
  - `hitanalysis-error.log`: 错误日志

- **MySQL**: `/var/log/mysql/`
  - `slow.log`: 慢查询日志
  - `error.log`: 错误日志

#### 日志查看命令

```bash
# 查看应用日志
tail -f /opt/hitanalysis/logs/application.log

# 查看错误日志
tail -f /opt/hitanalysis/logs/error.log

# 搜索特定错误
grep "ERROR" /opt/hitanalysis/logs/application.log | tail -20

# 查看访问日志
tail -f /var/log/nginx/hitanalysis-access.log

# 统计访问量
awk '{print $1}' /var/log/nginx/hitanalysis-access.log | sort | uniq -c | sort -rn | head -10
```

#### 日志清理脚本

```bash
# 创建日志清理脚本
cat > /opt/hitanalysis/clean_logs.sh << 'EOF'
#!/bin/bash
LOG_DIR="/opt/hitanalysis/logs"
NGINX_LOG_DIR="/var/log/nginx"
MYSQL_LOG_DIR="/var/log/mysql"

# 清理30天前的日志
find $LOG_DIR -name "*.log" -mtime +30 -exec rm -f {} \;
find $NGINX_LOG_DIR -name "*.log" -mtime +30 -exec rm -f {} \;

# 压缩7天前的日志
find $LOG_DIR -name "*.log" -mtime +7 -exec gzip {} \;

echo "Logs cleaned successfully"
EOF

chmod +x /opt/hitanalysis/clean_logs.sh

# 添加定时任务（每周执行）
echo "0 2 * * 0 /opt/hitanalysis/clean_logs.sh" | crontab -
```

### 2. 数据备份

#### MySQL备份脚本

```bash
cat > /opt/hitanalysis/backup_db.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/opt/hitanalysis/backups"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="bi_db"
DB_USER="root"
DB_PASS="your_root_password"

mkdir -p $BACKUP_DIR

# 全库备份
mysqldump -u$DB_USER -p$DB_PASS --single-transaction --routines --triggers \
  $DB_NAME > $BACKUP_DIR/hitanalysis_full_$DATE.sql

# 压缩备份文件
gzip $BACKUP_DIR/hitanalysis_full_$DATE.sql

# 删除30天前的备份
find $BACKUP_DIR -name "*.gz" -mtime +30 -exec rm -f {} \;

echo "Database backup completed: hitanalysis_full_$DATE.sql.gz"
EOF

chmod +x /opt/hitanalysis/backup_db.sh

# 每天凌晨2点执行备份
echo "0 2 * * * /opt/hitanalysis/backup_db.sh" | crontab -
```

#### Redis备份

Redis使用RDB和AOF持久化，定期保存dump文件：

```bash
# 手动触发RDB备份
redis-cli -a Strong_Redis_Password_2026! BGSAVE

# 自动备份脚本
cat > /opt/hitanalysis/backup_redis.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/opt/hitanalysis/backups"
REDIS_DIR="/var/lib/redis"
DATE=$(date +%Y%m%d_%H%M%S)

mkdir -p $BACKUP_DIR

# 复制RDB文件
cp $REDIS_DIR/dump.rdb $BACKUP_DIR/redis_dump_$DATE.rdb

# 复制AOF文件
cp $REDIS_DIR/appendonly.aof $BACKUP_DIR/redis_aof_$DATE.aof

# 压缩
gzip $BACKUP_DIR/redis_dump_$DATE.rdb
gzip $BACKUP_DIR/redis_aof_$DATE.aof

# 删除7天前的备份
find $BACKUP_DIR -name "redis_*" -mtime +7 -exec rm -f {} \;

echo "Redis backup completed"
EOF

chmod +x /opt/hitanalysis/backup_redis.sh
```

### 3. 性能监控

#### 系统监控脚本

```bash
cat > /opt/hitanalysis/monitor.sh << 'EOF'
#!/bin/bash

echo "=== System Status ==="
echo "CPU: $(top -bn1 | grep "Cpu(s)" | sed "s/.*, *\([0-9.]*\)%* id.*/\1/" | awk '{print 100 - $1"%"}')"
echo "Memory: $(free -m | awk '/Mem:/ {printf "%.2f%%", ($3/$2)*100}')"
echo "Disk: $(df -h | awk '/\/$/ {print $5}')"

echo "\n=== Application Status ==="
if systemctl is-active --quiet hitanalysis; then
    echo "✓ Application is running"
    ps aux | grep java | grep hitanalysis
else
    echo "✗ Application is stopped"
fi

echo "\n=== Database Status ==="
if systemctl is-active --quiet mysql; then
    echo "✓ MySQL is running"
    mysql -u hitanalysis -pStrong_Password_2026! -e "SHOW STATUS LIKE 'Threads_connected';"
else
    echo "✗ MySQL is stopped"
fi

echo "\n=== Redis Status ==="
if systemctl is-active --quiet redis; then
    echo "✓ Redis is running"
    redis-cli -a Strong_Redis_Password_2026! INFO | grep "connected_clients"
else
    echo "✗ Redis is stopped"
fi

echo "\n=== Nginx Status ==="
if systemctl is-active --quiet nginx; then
    echo "✓ Nginx is running"
    curl -I http://localhost 2>&1 | grep "HTTP"
else
    echo "✗ Nginx is stopped"
fi
EOF

chmod +x /opt/hitanalysis/monitor.sh
```

#### 定期监控（每小时）

```bash
# 添加监控定时任务
echo "0 * * * * /opt/hitanalysis/monitor.sh >> /opt/hitanalysis/logs/monitor.log" | crontab -
```

### 4. 故障处理

#### 应用崩溃处理

```bash
# 检查应用状态
systemctl status hitanalysis

# 查看错误日志
tail -100 /opt/hitanalysis/logs/error.log

# 重启应用
systemctl restart hitanalysis

# 如果无法启动，检查配置
java -jar /opt/hitanalysis/app/hitanalysis-app-1.0.0-SNAPSHOT.jar --spring.config.location=/opt/hitanalysis/config/application-prod.yml
```

#### 数据库连接失败

```bash
# 检查MySQL状态
systemctl status mysql

# 检查连接数
mysql -u root -p -e "SHOW STATUS LIKE 'Threads_connected';"

# 检查最大连接数
mysql -u root -p -e "SHOW VARIABLES LIKE 'max_connections';"

# 如果连接数过多，清理空闲连接
mysql -u root -p -e "SHOW PROCESSLIST;" | grep "Sleep" | awk '{print "KILL " $1 ";"}' | mysql -u root -p
```

#### Redis连接失败

```bash
# 检查Redis状态
systemctl status redis

# 检查Redis内存使用
redis-cli -a Strong_Redis_Password_2026! INFO memory | grep "used_memory_human"

# 如果内存满了，清理缓存
redis-cli -a Strong_Redis_Password_2026! FLUSHDB  # 注意：会清空当前数据库
```

### 5. 安全维护

#### 定期安全检查

```bash
# 检查系统更新
yum check-update                 # CentOS
apt update                       # Ubuntu

# 检查开放的端口
netstat -tulpn | grep LISTEN

# 检查防火墙规则
firewall-cmd --list-all          # CentOS
ufw status                       # Ubuntu

# 检查用户权限
mysql -u root -p -e "SELECT User, Host FROM mysql.user;"
```

#### 密码更新（每季度）

```bash
# 更新MySQL用户密码
mysql -u root -p -e "ALTER USER 'hitanalysis'@'%' IDENTIFIED BY 'New_Strong_Password_2026!';"

# 更新Redis密码
# 编辑redis.conf，修改requirepass
systemctl restart redis

# 更新应用配置
# 编辑application-prod.yml，修改数据库和Redis密码
systemctl restart hitanalysis
```

### 6. 版本升级

#### 应用升级步骤

```bash
# 1. 备份当前版本
cp /opt/hitanalysis/app/hitanalysis-app-1.0.0-SNAPSHOT.jar /opt/hitanalysis/backups/hitanalysis-app-backup.jar

# 2. 停止应用
systemctl stop hitanalysis

# 3. 更新应用文件
cp hitanalysis-app/target/hitanalysis-app-1.0.1-SNAPSHOT.jar /opt/hitanalysis/app/

# 4. 更新systemd服务（如有必要）
vim /etc/systemd/system/hitanalysis.service

# 5. 重载systemd
systemctl daemon-reload

# 6. 启动应用
systemctl start hitanalysis

# 7. 验证
systemctl status hitanalysis
curl http://localhost:8080/api/v1/users?pageNum=1&pageSize=1
```

#### 数据库升级

```bash
# 执行数据库升级脚本（如有）
mysql -u hitanalysis -p bi_db < database_upgrade_script.sql

# 验证数据库结构
mysql -u hitanalysis -p -e "USE bi_db; SHOW TABLES;"
```

## 📞 故障排查联系方式

- **系统管理员**: admin@your-domain.com
- **数据库管理员**: dba@your-domain.com
- **运维团队**: ops@your-domain.com

---

**版本**: v1.0.0
**更新日期**: 2026-05-11
**维护团队**: HITAnalysis运维团队