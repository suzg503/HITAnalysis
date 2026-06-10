# HITAnalysis - 项目启动指南

## 项目简介

HITAnalysis（医疗数据AI智能分析平台）是一款面向医疗行业的私有化部署BI分析软件。通过自然语言对话实现从"对话"到"报表"的极简转化，解决医疗数据分析门槛高、配置繁琐、数据安全敏感的痛点。

## 技术架构

### 后端技术栈
- **Java 17** + **Spring Boot 3.2.5**
- **MyBatis Plus 3.5.5** - ORM框架
- **MySQL 8.0** - 主数据库
- **Redis (Redisson 3.27.2)** - 缓存和分布式锁
- **JWT** - 认证授权
- **LangChain4j 0.35.0** - AI模型集成
- **Knife4j/SpringDoc** - API文档

### 前端技术栈
- **Vue 3.4** + **TypeScript 5.4**
- **Vite 5.1** - 构建工具
- **Element Plus 2.6** - UI组件库
- **ECharts 5.5** - 图表可视化
- **Pinia** - 状态管理
- **Axios** - HTTP客户端

## 环境要求

### 必需环境
- **JDK 17** 或更高版本
- **Node.js 18** 或更高版本
- **MySQL 8.0** 或更高版本
- **Redis 7.0** 或更高版本

### 开发工具建议
- **IntelliJ IDEA 2023+** (后端开发)
- **VS Code** 或 **WebStorm** (前端开发)
- **Navicat** 或 **DBeaver** (数据库管理)

## 快速启动

### 1. 数据库初始化

```bash
# 进入数据库脚本目录
cd product/database-design-optimized/sql

# Windows系统执行（PowerShell）
.\init-database.ps1

# Linux/Mac系统执行
bash init-database.sh
```

或手动执行SQL脚本：

```bash
# 创建数据库和基础表
mysql -u root -p < 01_create_database.sql

# 创建日志表
mysql -u root -p bi_db < 02_create_log_tables.sql

# 创建指标相关表
mysql -u root -p bi_db < 03_create_indicator_tables.sql

# 创建目标值表
mysql -u root -p bi_db < 04_create_target_tables.sql

# 创建AI分析表
mysql -u root -p bi_db < 05_create_analysis_ai_tables.sql

# 创建汇总表
mysql -u root -p bi_db < 06_create_summary_tables.sql

# 创建存储过程
mysql -u root -p bi_db < 07_create_procedures.sql

# 创建视图
mysql -u root -p bi_db < 08_create_views.sql

# 创建触发器
mysql -u root -p bi_db < 09_create_triggers.sql

# 初始化基础数据
mysql -u root -p bi_db < 10_init_data.sql
```

**数据库配置检查**：
```sql
-- 验证数据库创建成功
SHOW DATABASES LIKE 'bi_db';

-- 验证表创建成功
USE bi_db;
SHOW TABLES;

-- 验证基础数据
SELECT COUNT(*) FROM sys_user;      -- 应返回至少1条（管理员）
SELECT COUNT(*) FROM sys_role;      -- 应返回5条角色
SELECT COUNT(*) FROM bi_indicator;  -- 应返回10条指标
```

### 2. Redis启动

```bash
# Windows
redis-server.exe

# Linux/Mac
redis-server

# 验证Redis连接
redis-cli ping  # 应返回 PONG
```

### 3. 后端启动

```bash
# 进入后端目录
cd hitanalysis-backend

# 使用Maven编译（首次启动）
mvn clean install -DskipTests

# 启动应用
cd hitanalysis-app
mvn spring-boot:run

# 或使用IDE直接运行 HitAnalysisApplication.java
```

**后端启动成功标志**：
- 控制台输出：`Started HitAnalysisApplication in X seconds`
- 访问：http://localhost:8080/api/swagger-ui.html (API文档)
- 访问：http://localhost:8080/api/doc.html (Knife4j文档)

### 4. 前端启动

```bash
# 进入前端目录
cd hitanalysis-frontend

# 安装依赖（首次启动）
npm install

# 启动开发服务器
npm run dev

# 或使用pnpm/yarn
pnpm install && pnpm dev
yarn install && yarn dev
```

**前端启动成功标志**：
- 控制台输出：`VITE v5.1.6 ready in X ms`
- 访问：http://localhost:5173
- 自动跳转到登录页面

### 5. 验证系统运行

1. **登录测试**：
   - 访问前端：http://localhost:5173
   - 使用默认管理员账号登录：
     - 用户名：`admin`
     - 密码：`admin123` (需要先在数据库中更新密码哈希)

2. **API测试**：
   - 访问API文档：http://localhost:8080/api/doc.html
   - 测试用户列表接口：`GET /api/v1/users`

3. **数据库连接测试**：
   - 在API文档中测试需要数据库的接口
   - 检查控制台日志是否有数据库连接错误

## 配置说明

### 后端配置文件

**application.yml** (主配置文件)
```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bi_db
    username: root
    password: root  # 根据实际情况修改

  data:
    redis:
      host: localhost
      port: 6379
      password:        # 如有密码则填写

jwt:
  secret: hitanalysis-jwt-secret-key-2026-medical-ai-platform
  expiration: 86400000  # 24小时
```

**application-dev.yml** (开发环境配置)
- 用于开发环境的特定配置覆盖

### 前端配置文件

**vite.config.ts** (构建配置)
```typescript
export default defineConfig({
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
})
```

**.env.development** (开发环境变量)
```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_TITLE=HITAnalysis
```

## 常见问题

### 数据库连接失败
```bash
# 检查MySQL服务状态
mysql -u root -p

# 检查数据库是否存在
SHOW DATABASES LIKE 'bi_db';

# 检查用户权限
SHOW GRANTS FOR 'root'@'localhost';
```

### Redis连接失败
```bash
# 检查Redis服务状态
redis-cli ping

# 检查Redis配置
redis-cli CONFIG GET bind
redis-cli CONFIG GET port
```

### 后端启动失败
- 检查JDK版本：`java -version` (应为17+)
- 检查Maven版本：`mvn -version`
- 检查端口占用：`netstat -ano | findstr 8080` (Windows)
- 查看详细错误日志

### 前端启动失败
- 检查Node.js版本：`node -version` (应为18+)
- 清理依赖缓存：`npm cache clean --force`
- 删除node_modules重新安装：`rm -rf node_modules && npm install`

## 开发指南

### 后端开发

**项目结构**：
```
hitanalysis-backend/
├── hitanalysis-common/     # 公共模块（工具、异常、结果封装）
├── hitanalysis-system/     # 系统模块（用户、角色、权限）
├── hitanalysis-metadata/   # 元数据模块（指标、维度管理）
├── hitanalysis-report/     # 报表模块（标准报表、AI报表）
├── hitanalysis-ai/         # AI模块（意图识别、SQL生成）
└── hitanalysis-app/        # 应用启动模块（配置、入口）
```

**添加新模块步骤**：
1. 在父POM中添加module定义
2. 创建模块目录和pom.xml
3. 添加Controller、Service、Mapper等
4. 在application.yml中配置扫描路径

### 前端开发

**项目结构**：
```
hitanalysis-frontend/
├── src/
│   ├── api/                # API接口定义
│   ├── components/         # 可复用组件
│   ├── views/              # 页面组件
│   ├── stores/             # Pinia状态管理
│   ├── router/             # 路由配置
│   ├── types/              # TypeScript类型定义
│   ├── constants/          # 常量定义
│   └── utils/              # 工具函数
├── public/                 # 静态资源
└── tests/                  # 测试文件
```

**添加新页面步骤**：
1. 在`src/views/`下创建页面组件
2. 在`src/router/index.ts`中添加路由配置
3. 在`src/api/`中添加对应的API接口
4. 如需要，在数据库`sys_menu`表中添加菜单项

## 生产部署

### 后端部署

```bash
# 打包生产版本
mvn clean package -Dmaven.test.skip=true

# 运行
java -jar hitanalysis-app/target/hitanalysis-app-1.0.0-SNAPSHOT.jar

# 或使用外部配置
java -jar hitanalysis-app.jar --spring.config.location=/path/to/application.yml
```

### 前端部署

```bash
# 构建生产版本
npm run build

# 生成的dist目录可部署到任何Web服务器
# Nginx配置示例：
server {
    listen 80;
    server_name your-domain.com;

    root /path/to/hitanalysis-frontend/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 环境变量配置

**后端环境变量**：
```bash
export SPRING_PROFILES_ACTIVE=prod
export MYSQL_HOST=your-db-host
export MYSQL_PORT=3306
export MYSQL_USER=your-username
export MYSQL_PASSWORD=your-password
export REDIS_HOST=your-redis-host
export REDIS_PORT=6379
```

**前端环境变量**：
```env
VITE_API_BASE_URL=https://your-api-domain/api
VITE_APP_TITLE=HITAnalysis生产环境
```

## 安全建议

1. **修改默认密码**：首次启动后立即修改admin用户密码
2. **JWT密钥**：生产环境必须使用强密钥，不要使用默认值
3. **数据库权限**：使用专用数据库用户，限制权限范围
4. **Redis密码**：生产环境Redis必须设置密码
5. **HTTPS**：生产环境必须使用HTTPS
6. **防火墙**：限制数据库和Redis的外部访问

## 监控和日志

### 后端日志
- 位置：`hitanalysis-app/logs/`
- 日志级别可通过`application.yml`调整
- 使用Logback配置日志格式和输出

### 前端日志
- 开发环境：浏览器控制台
- 生产环境：可集成日志服务

### 性能监控
- 后端：集成Spring Boot Actuator
- 数据库：MySQL慢查询日志
- Redis：Redis监控命令

## 技术支持

- **项目文档**：`product/` 目录下的设计文档
- **API文档**：http://localhost:8080/api/doc.html
- **数据库设计**：`product/database-design-optimized/README.md`

---

**版本**：v1.0.0
**更新日期**：2026-05-11
**维护团队**：HITAnalysis开发团队