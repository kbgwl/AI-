# AI智能客服系统 - 基础版开源

空白格·AI 智能客服系统基础版，支持AI自动回复、知识库、意图管理、工单系统等核心功能。

## 功能特性

- **AI智能对话** - 基于DeepSeek/OpenAI的智能客服机器人
- **知识库管理** - FAQ管理、批量导入、智能检索
- **意图识别** - 自定义意图、自动匹配回复
- **对话流程** - 可视化流程编排
- **工单系统** - 自动创建工单、分配处理
- **多渠道接入** - 网页、微信公众号等
- **数据概览** - 实时数据统计、会话趋势

## 技术栈

### 后端
- Java 17 + Spring Boot 3.2
- MyBatis-Plus
- MySQL 8.0
- WebSocket

### 前端
- Vue 3 + Vite
- Element Plus
- Pinia

## 快速开始

### 1. 环境要求
- JDK 17+
- Maven 3.6+
- Node.js 18+
- MySQL 8.0+

### 2. 数据库初始化
```bash
mysql -u root -p ai_customer_service < sql/database_dump.sql
```

### 3. 后端启动
```bash
cd backend
mvn clean package -DskipTests
java -jar target/ai-customer-service-1.0.0.jar
```

### 4. 前端启动
```bash
cd frontend
npm install
npm run dev
```

### 5. 访问系统
- 前端: http://localhost:5173
- 后端API: http://localhost:8082
- 默认账号: admin / admin123

## 项目结构

```
├── backend/              # 后端代码
│   ├── src/main/java/    # Java源码
│   └── pom.xml          # Maven配置
├── frontend/             # 前端代码
│   ├── src/             # Vue源码
│   └── package.json     # NPM配置
├── sql/                 # 数据库脚本
└── README.md
```

## 配置说明

后端配置文件: `backend/src/main/resources/application.yml`

```yaml
server:
  port: 8082

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_customer_service
    username: root
    password: your_password
```

## 开源协议

MIT License

## 联系方式

济南空白格网络科技有限公司
