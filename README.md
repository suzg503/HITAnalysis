# HITAnalysis - 医疗数据AI智能分析平台

## 📋 项目简介

HITAnalysis是一款面向医疗行业的私有化部署BI分析软件。通过"工程师预设维度 + 本地化AI模型"的组合，实现从"对话"到"报表"的极简转化，解决医疗数据分析门槛高、配置繁琐、数据安全敏感的痛点。

**核心价值主张**：
- 🎯 **极简交互**：通过自然语言对话替代复杂报表配置
- 🔒 **绝对安全**：本地化模型部署，数据不出院
- 📊 **严谨专业**：底层维度由工程师管控，确保医疗计算逻辑无误
- 🎛️ **精细掌控**：支持多机构、按钮级权限控制

## 🏗️ 技术架构

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

### 项目模块
```
HITAnalysis/
├── hitanalysis-backend/          # 后端项目
│   ├── hitanalysis-common/       # 公共模块
│   ├── hitanalysis-system/       # 系统管理
│   ├── hitanalysis-metadata/     # 元数据管理
│   ├── hitanalysis-report/       # 报表管理
│   ├── hitanalysis-ai/           # AI模块
│   └── hitanalysis-app/          # 应用启动
├── hitanalysis-frontend/         # 前端项目
│   ├── src/
│   │   ├── views/                # 页面组件
│   │   ├── api/                  # API接口
│   │   ├── stores/               # 状态管理
│   │   └── components/           # UI组件
│   └── public/                   # 静态资源
├── product/                      # 设计文档
│   ├── database-design-optimized/ # 数据库设计
│   └── *.md                      # 其他文档
└── STARTUP_GUIDE.md              # 启动指南
```

## 🚀 快速启动

详细启动步骤请参考 [STARTUP_GUIDE.md](./STARTUP_GUIDE.md)

### 1. 数据库初始化
```bash
cd product/database-design-optimized/sql
# Windows: PowerShell
.\init-database.ps1
# Linux/Mac
bash init-database.sh
```

### 2. 后端启动
```bash
cd hitanalysis-backend
mvn clean install -DskipTests
cd hitanalysis-app
mvn spring-boot:run
```

### 3. 前端启动
```bash
cd hitanalysis-frontend
npm install
npm run dev
```

### 4. 访问系统
- 前端界面：http://localhost:5173
- API文档：http://localhost:8080/api/doc.html
- 默认账号：admin (密码需要在数据库中更新BCrypt哈希)

## 📚 文档资源

- [启动指南](./STARTUP_GUIDE.md) - 系统部署和启动详细步骤
- [产品需求文档](./product/prd.md) - 产品功能需求定义
- [数据库设计](./product/database-design-optimized/README.md) - DBA优化版数据库设计
- [架构设计](./product/HITAnalysis_ARCHITECTURE_v1.0.md) - 系统架构设计
- [技术选型](./product/HITAnalysis_TECH_SELECTION_v1.0.md) - 技术栈选择说明

## ✨ 核心功能

### AI智能助手
- 🤖 自然语言对话交互
- 🔍 意图识别和语义解析
- 📊 自动报表生成和预览
- 💾 AI洞察中心管理

### 元数据中心
- 📋 指标体系维护
- 🏷️ 维度管理（组织、时间、业务）
- 📐 计算逻辑配置
- 🔗 SQL配置绑定

### 报表分析
- 📈 标准报表目录
- 📊 多维度数据可视化
- 🔄 同比环比分析
- 🎯 目标值管理
- 🔍 自定义下钻

### 权限控制
- 🏥 多租户隔离
- 👤 行级数据过滤
- 🔐 按钮级功能权限
- 🤖 AI专项权限

## 🛠️ 开发指南

### 后端开发规范
- 使用MyBatis Plus进行数据访问
- 统一使用Result封装返回结果
- 异常处理通过GlobalExceptionHandler
- API文档使用Swagger注解

### 前端开发规范
- 使用Vue 3 Composition API
- TypeScript类型定义
- Element Plus组件库
- Axios请求封装

## 🔒 安全特性

- 本地化AI模型部署，数据不出院
- JWT认证授权
- 行级数据权限过滤
- 完整审计日志
- 患者数据脱敏处理
- 密码BCrypt加密存储

## 📊 性能优化

- 数据库索引优化（P0优先级）
- 日志表分区设计（按月分区）
- Redis缓存集成
- 汇总表预聚合
- 存储过程自动化计算

## 🧪 测试

```bash
# 后端测试
cd hitanalysis-backend
mvn test

# 前端测试
cd hitanalysis-frontend
npm run test
```

## 📦 部署

### 生产环境配置
- 修改application.yml中的数据库、Redis配置
- 更新JWT密钥（使用强密钥）
- 配置HTTPS
- 设置防火墙规则

### 构建部署
```bash
# 后端打包
mvn clean package -Dmaven.test.skip=true
java -jar hitanalysis-app/target/hitanalysis-app-1.0.0-SNAPSHOT.jar

# 前端打包
npm run build
# dist目录部署到Web服务器
```

## 📝 版本历史

- **v1.0.0** (2026-05-11): 初始版本发布
  - 完整的后端架构
  - 前端基础框架
  - DBA优化数据库设计
  - AI模块基础实现

## 👥 贡献指南

本项目借助AI辅助开发，欢迎提出改进建议。

## 📄 许可证

本项目仅供学习和研究使用。

---

**维护团队**：HITAnalysis开发团队
**联系方式**：如有问题请查阅文档或联系开发团队
