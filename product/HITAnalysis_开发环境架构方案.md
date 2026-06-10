# HITAnalysis 开发环境架构方案

**版本**：v1.0-dev  
**用途**：开发、测试、功能验证  
**目标**：低硬件成本、快速启动、功能完整  
**创建日期**：2026-05-07

---

## 目录

1. [设计原则](#一设计原则)
2. [开发环境 vs 生产环境对比](#二开发环境-vs-生产环境对比)
3. [硬件需求](#三硬件需求)
4. [技术选型](#四技术选型)
5. [Docker-Compose配置](#五docker-compose配置)
6. [AI模型选型与部署](#六ai模型选型与部署)
7. [启动与使用](#七启动与使用)
8. [开发工作流](#八开发工作流)
9. [与生产环境的差异](#九与生产环境的差异)
10. [常见问题](#十常见问题)
11. [成本估算](#十一成本估算)

---

## 一、设计原则

| 原则 | 说明 |
|------|------|
| **最小可行** | 只保留核心组件，去掉生产环境的高可用冗余 |
| **一键启动** | Docker Compose 单命令启动全部服务 |
| **功能完整** | 开发环境覆盖所有核心功能，确保可测试 |
| **成本可控** | 单机或2台机器即可运行 |

---

## 二、开发环境 vs 生产环境对比

| 组件 | 生产环境 | 开发环境 | 说明 |
|------|---------|---------|------|
| **应用服务** | 2实例 + Nginx负载均衡 | 1实例 | 单实例够用 |
| **API网关** | Spring Cloud Gateway | 直连应用 | 开发阶段不需要网关 |
| **服务注册** | Nacos | 去掉 | 用配置文件替代 |
| **MySQL** | 主从复制 | 单实例 | 数据量小，无需主从 |
| **ClickHouse** | 3节点集群 | 单节点 | 开发数据量小 |
| **Redis** | 哨兵模式(3节点) | 单实例 | 无高可用需求 |
| **向量数据库** | Milvus(etcd+MinIO) | Chroma 或 Milvus Lite | 轻量级替代 |
| **消息队列** | RocketMQ | 巍掉 | 用同步调用替代 |
| **AI模型** | Qwen2.5-7B(A100 40GB) | gemma4-e4b / qwen3.5-9b | 轻量模型 |
| **推理框架** | vLLM | Ollama 或 llama.cpp | 更简单易用 |
| **监控** | Prometheus+Grafana | Spring Boot Actuator | 基础监控即可 |
| **日志** | ELK Stack | 文件日志 + 控制台 | 开发阶段够用 |

---

## 三、硬件需求

### 3.1 最低配置（单机开发）

| 组件 | 配置 | 说明 |
|------|------|------|
| **CPU** | 8核 | i7/R7 级别 |
| **内存** | 32GB | 16GB会很紧张 |
| **存储** | 512GB SSD | 模型文件+数据库 |
| **GPU** | RTX 4060 Ti 16GB | 仅跑 e4b 模型 |

### 3.2 推荐配置（单机开发，支持9B模型）

| 组件 | 配置 | 估算成本 |
|------|------|---------|
| **CPU** | 16核 | - |
| **内存** | 64GB | - |
| **存储** | 1TB NVMe SSD | - |
| **GPU** | RTX 4090 24GB | 可跑 qwen3.5-9b |

### 3.3 两台机器方案（推荐）

| 机器 | 配置 | 用途 | 估算成本 |
|------|------|------|---------|
| **开发机** | 16核/64GB/512GB SSD | 应用+数据库+Redis | 已有笔记本/台式机即可 |
| **AI推理机** | 8核/32GB/512GB SSD + RTX 4090 | 模型推理 | 约1.5-2万(含GPU) |

**总硬件成本**：0-2万元（如已有开发机，仅需GPU机器）

---

## 四、技术选型

### 4.1 选型总览

```
┌─────────────────────────────────────────────────────────────┐
│                        【用户层】                            │
│              Vue 3 + Element Plus + ECharts                 │
│              (本地 dev server，端口 5173)                    │
└────────────────────┬────────────────────────────────────────┘
                     │ HTTP
┌────────────────────▼────────────────────────────────────────┐
│                      【应用层】                              │
│            Spring Boot 3.x 单实例 (端口 8080)               │
│            内置：权限、报表、AI调用、元数据管理               │
└────────────────────┬────────────────────────────────────────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
┌───────▼──────┐ ┌───▼────┐ ┌────▼─────┐
│   MySQL 8.0  │ │ClickHouse│ │  Redis   │
│  (元数据)    │ │(业务数据)│ │  (缓存)  │
│   端口3306   │ │ 端口8123 │ │ 端口6379 │
└──────────────┘ └─────────┘ └──────────┘
                     │
              ┌──────▼──────┐
              │  AI推理服务  │
              │   Ollama     │
              │  端口11434   │
              │              │
              │ gemma4-e4b   │
              │ 或 qwen3.5-9b│
              └──────────────┘
```

### 4.2 各组件选型理由

| 组件 | 选型 | 理由 |
|------|------|------|
| **后端** | Spring Boot 3.x | 与生产环境一致，无需额外学习 |
| **ORM** | MyBatis-Plus | 与生产环境一致 |
| **前端** | Vue 3 + Vite | 本地dev server，热更新 |
| **UI库** | Element Plus | 与生产环境一致 |
| **图表** | ECharts | 与生产环境一致 |
| **MySQL** | 8.0 单实例 | 元数据存储，开发够用 |
| **ClickHouse** | 单节点 | OLAP查询，单节点够用 |
| **Redis** | 7.x 单实例 | 缓存+会话，单实例够用 |
| **向量数据库** | Chroma (内嵌) | Python原生，无需额外部署 |
| **AI推理** | Ollama | 一键部署，支持多种模型 |
| **容器化** | Docker Compose | 一键启动所有服务 |

---

## 五、Docker Compose配置

### 5.1 目录结构

```
hitanalysis-dev/
├── docker-compose.yml          # 主编排文件
├── .env                        # 环境变量
├── config/
│   ├── mysql/
│   │   └── init.sql            # MySQL初始化脚本
│   ├── clickhouse/
│   │   └── config.xml          # ClickHouse配置
│   └── redis/
│       └── redis.conf          # Redis配置
├── app/                        # Spring Boot应用
│   └── Dockerfile
├── ai-service/                 # AI服务(Python)
│   ├── Dockerfile
│   ├── requirements.txt
│   └── app.py
└── frontend/                   # Vue前端(可选容器化)
    └── Dockerfile
```

### 5.2 docker-compose.yml

```yaml
version: '3.8'

services:
  # ============================================
  # 数据层
  # ============================================
  
  mysql:
    image: mysql:8.0
    container_name: hit-mysql
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-dev123456}
      MYSQL_DATABASE: hitanalysis
      MYSQL_CHARACTER_SET_SERVER: utf8mb4
      MYSQL_COLLATION_SERVER: utf8mb4_unicode_ci
    volumes:
      - mysql-data:/var/lib/mysql
      - ./config/mysql/init.sql:/docker-entrypoint-initdb.d/init.sql
    command: >
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_unicode_ci
      --default-time-zone=+08:00
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 3
    networks:
      - hit-network

  clickhouse:
    image: clickhouse/clickhouse-server:latest
    container_name: hit-clickhouse
    ports:
      - "8123:8123"   # HTTP接口
      - "9000:9000"   # Native接口
    environment:
      CLICKHOUSE_DB: hitanalysis
      CLICKHOUSE_USER: default
      CLICKHOUSE_PASSWORD: ${CLICKHOUSE_PASSWORD:-dev123456}
    volumes:
      - clickhouse-data:/var/lib/clickhouse
      - ./config/clickhouse/config.xml:/etc/clickhouse-server/config.d/custom.xml
    healthcheck:
      test: ["CMD", "clickhouse-client", "--query", "SELECT 1"]
      interval: 10s
      timeout: 5s
      retries: 3
    networks:
      - hit-network

  redis:
    image: redis:7-alpine
    container_name: hit-redis
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes --requirepass ${REDIS_PASSWORD:-dev123456}
    volumes:
      - redis-data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "-a", "${REDIS_PASSWORD:-dev123456}", "ping"]
      interval: 10s
      timeout: 5s
      retries: 3
    networks:
      - hit-network

  # ============================================
  # AI推理层
  # ============================================
  
  ollama:
    image: ollama/ollama:latest
    container_name: hit-ollama
    ports:
      - "11434:11434"
    environment:
      - OLLAMA_HOST=0.0.0.0
      - OLLAMA_MODELS=/root/.ollama/models
    volumes:
      - ollama-data:/root/.ollama
      - ./models:/models
    deploy:
      resources:
        reservations:
          devices:
            - driver: nvidia
              count: all
              capabilities: [gpu]
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:11434/api/tags"]
      interval: 30s
      timeout: 10s
      retries: 3
    networks:
      - hit-network

  # ============================================
  # 应用层
  # ============================================
  
  app:
    build:
      context: ./app
      dockerfile: Dockerfile
    container_name: hit-app
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: dev
      
      # MySQL
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: hitanalysis
      DB_USER: root
      DB_PASSWORD: ${MYSQL_ROOT_PASSWORD:-dev123456}
      
      # ClickHouse
      CLICKHOUSE_HOST: clickhouse
      CLICKHOUSE_PORT: 8123
      CLICKHOUSE_DB: hitanalysis
      CLICKHOUSE_USER: default
      CLICKHOUSE_PASSWORD: ${CLICKHOUSE_PASSWORD:-dev123456}
      
      # Redis
      REDIS_HOST: redis
      REDIS_PORT: 6379
      REDIS_PASSWORD: ${REDIS_PASSWORD:-dev123456}
      
      # AI服务
      AI_SERVICE_URL: http://ollama:11434
      AI_MODEL_NAME: ${AI_MODEL_NAME:-gemma3:4b}
      
      # JVM参数
      JAVA_OPTS: "-Xms512m -Xmx2g"
    depends_on:
      mysql:
        condition: service_healthy
      clickhouse:
        condition: service_healthy
      redis:
        condition: service_healthy
      ollama:
        condition: service_healthy
    volumes:
      - ./logs:/app/logs
    networks:
      - hit-network

volumes:
  mysql-data:
  clickhouse-data:
  redis-data:
  ollama-data:

networks:
  hit-network:
    driver: bridge
```

### 5.3 .env 文件

```bash
# ============================================
# HITAnalysis 开发环境配置
# ============================================

# 数据库密码（开发环境使用简单密码）
MYSQL_ROOT_PASSWORD=dev123456
CLICKHOUSE_PASSWORD=dev123456
REDIS_PASSWORD=dev123456

# AI模型选择（二选一）
# AI_MODEL_NAME=gemma3:4b
AI_MODEL_NAME=qwen3:8b

# 时区
TZ=Asia/Shanghai
```

---

## 六、AI模型选型与部署

### 6.1 模型对比

| 模型 | 参数量 | 显存需求 | 中文能力 | 推理速度 | 推荐场景 |
|------|--------|---------|---------|---------|---------|
| **gemma3:4b** | 4B | ~8GB | ⭐⭐⭐ | 快 | 快速原型验证 |
| **qwen3:8b** | 8B | ~16GB | ⭐⭐⭐⭐⭐ | 中 | 中文场景首选 |
| **qwen3:9b** | 9B | ~18GB | ⭐⭐⭐⭐⭐ | 中 | 更强理解能力 |

### 6.2 模型部署（Ollama方式）

```bash
# 启动Ollama后，拉取模型
docker exec -it hit-ollama ollama pull gemma3:4b
# 或
docker exec -it hit-ollama ollama pull qwen3:8b

# 测试模型
curl http://localhost:11434/api/generate -d '{
  "model": "qwen3:8b",
  "prompt": "你好，请介绍一下自己",
  "stream": false
}'
```

### 6.3 AI服务封装（Python）

```python
# ai-service/app.py
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import httpx
import json

app = FastAPI(title="HITAnalysis AI Service")

OLLAMA_URL = "http://ollama:11434"
MODEL_NAME = "qwen3:8b"  # 从环境变量读取

class ParseRequest(BaseModel):
    query: str
    user_permissions: dict = None

class ParseResponse(BaseModel):
    intent: str
    metrics: list
    dimensions: list
    time_range: dict
    filters: list
    confidence: float

@app.post("/api/ai/parse", response_model=ParseResponse)
async def parse_query(request: ParseRequest):
    """解析用户自然语言查询"""
    
    prompt = f"""你是一个医疗数据分析助手。请解析用户的查询意图，返回JSON格式的分析配置。

用户查询：{request.query}

请返回以下JSON格式：
{{
  "intent": "trend_analysis|comparison|anomaly|target_check",
  "metrics": [{{"id": "metric_id", "name": "指标名称"}}],
  "dimensions": [{{"id": "dim_id", "name": "维度名称", "level": "day|month|year"}}],
  "time_range": {{"start": "2026-01-01", "end": "2026-01-31"}},
  "filters": [{{"dimension": "dim_id", "value": "filter_value"}}],
  "confidence": 0.95
}}

只返回JSON，不要其他内容。"""

    async with httpx.AsyncClient(timeout=30.0) as client:
        response = await client.post(
            f"{OLLAMA_URL}/api/generate",
            json={
                "model": MODEL_NAME,
                "prompt": prompt,
                "stream": False,
                "format": "json"
            }
        )
        
        if response.status_code != 200:
            raise HTTPException(status_code=500, detail="AI服务调用失败")
        
        result = response.json()
        try:
            parsed = json.loads(result["response"])
            return ParseResponse(**parsed)
        except json.JSONDecodeError:
            raise HTTPException(status_code=500, detail="AI返回格式错误")

@app.get("/health")
async def health_check():
    """健康检查"""
    return {"status": "ok", "model": MODEL_NAME}
```

---

## 七、启动与使用

### 7.1 一键启动

```bash
# 1. 克隆项目
git clone <repo-url> hitanalysis-dev
cd hitanalysis-dev

# 2. 拉取AI模型（首次需要）
docker-compose up -d ollama
sleep 5
docker exec -it hit-ollama ollama pull qwen3:8b

# 3. 启动所有服务
docker-compose up -d

# 4. 查看服务状态
docker-compose ps

# 5. 查看日志
docker-compose logs -f app
```

### 7.2 服务访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| **前端** | http://localhost:5173 | 本地npm run dev |
| **后端API** | http://localhost:8080 | Spring Boot |
| **MySQL** | localhost:3306 | 数据库连接 |
| **ClickHouse** | http://localhost:8123 | HTTP查询 |
| **Redis** | localhost:6379 | 缓存 |
| **Ollama** | http://localhost:11434 | AI推理 |

### 7.3 停止服务

```bash
# 停止所有服务
docker-compose down

# 停止并删除数据（慎用）
docker-compose down -v
```

---

## 八、开发工作流

### 8.1 前端开发

```bash
cd frontend
npm install
npm run dev  # 启动本地开发服务器，端口5173
```

### 8.2 后端开发

```bash
# IDE中直接运行 SpringBootApplication
# 或使用Maven
cd app
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 8.3 AI服务开发

```bash
cd ai-service
pip install -r requirements.txt
uvicorn app:app --reload --port 8000  # 本地开发
```

---

## 九、与生产环境的差异

| 维度 | 开发环境 | 生产环境 | 注意事项 |
|------|---------|---------|---------|
| **高可用** | 无 | 有 | 生产需要主从、集群 |
| **性能** | 一般 | 高 | 开发不做性能测试 |
| **安全** | 基础 | 完整 | 生产需要WAF、审计 |
| **监控** | Actuator | Prometheus+Grafana | 生产需要完整监控 |
| **日志** | 文件 | ELK | 生产需要集中日志 |
| **AI模型** | 4B-9B | 7B-72B | 生产可能需要更大模型 |
| **数据量** | 测试数据 | 真实数据 | 开发用模拟数据 |

---

## 十、常见问题

### Q1: 没有GPU怎么办？

**方案A：使用CPU推理（慢但可用）**

```yaml
# docker-compose.yml 中去掉 ollama 的 GPU 配置
ollama:
  image: ollama/ollama:latest
  # 去掉 deploy.resources.reservations 配置
```

**方案B：使用云端API**

```bash
# .env
AI_PROVIDER=openai  # 或 dashscope
AI_API_KEY=sk-xxx
AI_MODEL_NAME=gpt-4o-mini
```

### Q2: 内存不足32GB怎么办？

1. 减小JVM堆内存：`JAVA_OPTS: "-Xms256m -Xmx1g"`
2. 只启动必要服务：`docker-compose up -d mysql redis app`
3. 使用更小的AI模型：`gemma3:1b`

### Q3: 如何切换AI模型？

```bash
# 修改 .env
AI_MODEL_NAME=qwen3:8b

# 重启服务
docker-compose restart app

# 或拉取新模型
docker exec -it hit-ollama ollama pull qwen3:8b
```

---

## 十一、成本估算

### 方案A：已有开发机 + 外购GPU

| 项目 | 成本 | 说明 |
|------|------|------|
| RTX 4090 24GB | ~12,000元 | 可跑qwen3:9b |
| 32GB内存升级 | ~500元 | 如需升级 |
| **总计** | **~12,500元** | 一次性投入 |

### 方案B：云服务器（按需）

| 配置 | 月成本 | 说明 |
|------|--------|------|
| 8核32GB + RTX 4090 | ~2,000元/月 | 阿里云/AutoDL |
| 4核16GB + T4 16GB | ~800元/月 | 仅跑4B模型 |

### 方案C：纯CPU开发（零成本）

| 配置 | 成本 | 说明 |
|------|------|------|
| 现有电脑(16GB+内存) | 0元 | 使用gemma3:1b或云端API |

---

**文档结束**

> 本方案可在30分钟内完成部署，开始功能开发和测试。如需进一步调整，可以联系架构团队。
