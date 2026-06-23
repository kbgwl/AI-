# AI智能客服系统 v2.0.0

空白格·AI 智能客服系统，基于AI大模型的全渠道智能客服平台。

## 功能特性

### 核心功能
- **AI智能对话** - 基于DeepSeek/OpenAI的智能客服，支持多轮对话
- **知识库管理** - FAQ管理、批量导入、智能检索、行业分类
- **意图识别** - 自定义意图、自动匹配、置信度评分
- **对话流程** - 可视化流程编排、多轮引导、槽位填充
- **工单系统** - 自动创建、分配、流转、关闭
- **智能路由** - 按技能组、负载均衡、VIP优先分配
- **质量抽查** - 对话质量评分、问题检测

### 渠道管理
- **网页客服** - 在线客服组件、自定义主题
- **微信公众号** - 公众号接入、自动回复
- **微信小程序** - 小程序客服
- **抖音企业号** - 抖音渠道接入

### 管理后台
- **数据概览** - 实时数据、会话趋势、解决率、满意度
- **客服管理** - 坐席管理、技能组、在线状态
- **操作日志** - 全操作审计追踪
- **行业分类** - 多行业模板支持

### 增值功能（专业版/企业版）
- **多租户管理** - 租户隔离、独立配置
- **套餐管理** - 灵活套餐配置、功能开关
- **支付系统** - 支付宝/微信支付接入、订单管理、退款
- **系统配置** - AI模型、邮件、短信、存储等灵活配置
- **系统授权** - 机器码绑定、授权码激活、到期控制

## 技术栈

### 后端
| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | LTS版本 |
| Spring Boot | 3.2.5 | 核心框架 |
| MyBatis-Plus | 3.5.5 | ORM框架 |
| MySQL | 8.0+ | 数据库 |
| WebSocket | - | 实时通信 |
| JWT | 0.12.5 | Token认证 |
| OkHttp | 4.12.0 | HTTP客户端 |

### 前端
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5 | 前端框架 |
| Vite | 5.4 | 构建工具 |
| Element Plus | 2.14 | UI组件库 |
| Pinia | 3.0 | 状态管理 |
| Vue Router | 4.6 | 路由管理 |
| Axios | 1.16 | HTTP客户端 |

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- Node.js 18+
- MySQL 8.0+

### 1. 数据库初始化
```bash
mysql -u root -p ai_customer_service < sql/database_dump.sql
```

### 2. 后端启动
```bash
cd backend

# 修改配置
cp src/main/resources/application.yml.example src/main/resources/application.yml
# 编辑 application.yml 填入数据库密码和API Key

# 编译运行
mvn clean package -DskipTests
java -jar target/ai-customer-service-1.0.0.jar
```

### 3. 前端启动
```bash
cd frontend
npm install
npm run dev
```

### 4. 访问系统
- **前端**: http://localhost:5173
- **后端API**: http://localhost:8082
- **默认账号**: admin / admin123

## 项目结构

```
├── backend/                    # 后端代码
│   ├── src/main/java/
│   │   └── com/jnysx/aics/
│   │       ├── controller/     # 控制器
│   │       ├── service/        # 业务逻辑
│   │       ├── entity/         # 实体类
│   │       ├── mapper/         # 数据访问
│   │       ├── config/         # 配置类
│   │       ├── interceptor/    # 拦截器
│   │       └── websocket/      # WebSocket
│   └── pom.xml
├── frontend/                   # 前端代码
│   ├── src/
│   │   ├── views/              # 页面组件
│   │   ├── api/                # API接口
│   │   ├── stores/             # 状态管理
│   │   ├── router/             # 路由配置
│   │   └── composables/        # 组合式函数
│   └── package.json
├── sql/                        # 数据库脚本
├── build-prod.sh               # 生产构建脚本
└── README.md
```

## 配置说明

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_customer_service
    username: root
    password: your_password
```

### AI模型配置
```yaml
deepseek:
  api-key: your_api_key
  api-url: https://api.deepseek.com/chat/completions
  model: deepseek-chat
```

### 支付配置
在管理后台「系统管理 → 支付配置」中配置支付宝/微信支付。

## API接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/admin/login | POST | 管理员登录 |
| /api/chat/ai | POST | AI对话 |
| /api/kb/items | GET | 知识库列表 |
| /api/admin/intents | GET | 意图列表 |
| /api/admin/dashboard | GET | 数据概览 |
| /api/admin/tickets | GET | 工单列表 |
| /api/license/validate | GET | 授权验证 |

## 套餐说明

| 功能 | 试用版 | 基础版 | 专业版 | 企业版 |
|------|--------|--------|--------|--------|
| AI对话 | ✓ | ✓ | ✓ | ✓ |
| 知识库 | 20条 | 200条 | 1000条 | 无限 |
| 客服坐席 | 2人 | 5人 | 20人 | 无限 |
| 智能路由 | ✗ | ✗ | ✓ | ✓ |
| 质量抽查 | ✗ | ✗ | ✓ | ✓ |
| 多租户 | ✗ | ✗ | ✗ | ✓ |
| API接入 | ✗ | ✗ | ✗ | ✓ |

## 更新日志

### v2.0.0 (2026-06-23)
- 新增套餐管理系统
- 新增订单支付系统（支付宝/微信）
- 新增系统配置中心（AI/邮件/短信/安全/客服/存储）
- 新增多租户管理
- 新增系统授权（机器码绑定）
- 优化菜单分类
- 修复数据概览仪表盘
- 修复工单状态同步

### v1.0.0 (2026-05-19)
- 初始版本发布
- AI智能对话
- 知识库管理
- 意图识别
- 工单系统
- 多渠道接入

## 开源协议

MIT License

## 联系方式

济南空白格网络科技有限公司
