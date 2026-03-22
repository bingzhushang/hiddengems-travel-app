# 秘境探索 (HiddenGems)

> AI驱动的小众旅行目的地发现平台

一款专注于发现和推荐小众、非热门旅游目的地的移动应用。通过AI算法，帮助用户避开人潮，发现独特的旅行体验。

## 项目结构

```
chrome-app/
├── docs/                          # 项目文档
│   ├── PRD-小众目的地发现APP.md    # 产品需求文档
│   ├── 技术架构设计.md             # 技术架构 & API & 数据库设计
│   ├── UI-UX设计规范.md            # UI/UX设计规范
│   ├── AI推荐算法方案.md           # AI推荐系统设计
│   └── 数据采集策略.md             # 数据采集与处理方案
│
├── backend/                       # 后端服务 (Node.js + TypeScript)
│   ├── prisma/
│   │   └── schema.prisma          # 数据库模型
│   ├── src/
│   │   ├── config/                # 配置文件
│   │   ├── controllers/           # 控制器
│   │   ├── services/              # 业务逻辑
│   │   │   ├── auth.service.ts    # 认证服务
│   │   │   ├── spot.service.ts    # 景点服务
│   │   │   └── ai.service.ts      # AI服务
│   │   ├── routes/                # API路由
│   │   ├── middleware/            # 中间件
│   │   ├── types/                 # 类型定义
│   │   └── index.ts               # 入口文件
│   ├── package.json
│   └── tsconfig.json
│
└── frontend/                      # Android客户端 (Kotlin + Jetpack Compose)
    └── HiddenGems/
        ├── app/
        │   ├── src/main/
        │   │   ├── java/com/hiddengems/
        │   │   │   ├── ui/               # UI层
        │   │   │   │   ├── theme/        # 主题配置
        │   │   │   │   ├── navigation/   # 导航
        │   │   │   │   ├── home/         # 首页
        │   │   │   │   ├── discover/     # 发现
        │   │   │   │   ├── itinerary/    # 行程
        │   │   │   │   ├── community/    # 社区
        │   │   │   │   ├── profile/      # 我的
        │   │   │   │   ├── spot/         # 景点详情
        │   │   │   │   └── auth/         # 认证
        │   │   │   ├── data/             # 数据层
        │   │   │   └── di/               # 依赖注入
        │   │   ├── res/                  # 资源文件
        │   │   └── AndroidManifest.xml
        │   └── build.gradle.kts
        └── settings.gradle.kts
```

## 技术栈

### 后端
- **运行时**: Node.js 18+
- **语言**: TypeScript
- **框架**: Express
- **数据库**: PostgreSQL + Prisma ORM
- **缓存**: Redis
- **搜索**: Elasticsearch
- **向量数据库**: Pinecone
- **AI**: OpenAI API (GPT-4)

### Android客户端
- **语言**: Kotlin
- **UI**: Jetpack Compose
- **架构**: MVVM + Clean Architecture
- **依赖注入**: Hilt
- **网络**: Retrofit + OkHttp
- **图片加载**: Coil
- **地图**: Google Maps SDK
- **本地存储**: Room + DataStore

## 快速开始

### 环境要求
- Node.js 18+
- PostgreSQL 15+
- Redis 7+
- Android Studio Hedgehog+
- JDK 17+

### 后端启动

```bash
cd backend

# 安装依赖
npm install

# 配置环境变量
cp .env.example .env
# 编辑 .env 填入实际配置

# 生成Prisma客户端
npx prisma generate

# 运行数据库迁移
npx prisma migrate dev

# 启动开发服务器
npm run dev
```

### Android启动

1. 用 Android Studio 打开 `frontend/HiddenGems`
2. 配置 `local.properties`:
   ```properties
   GOOGLE_MAPS_API_KEY=your_api_key
   ```
3. Sync Project with Gradle Files
4. 运行到模拟器或真机

## 核心功能

### 1. AI智能推荐
- 基于用户画像的个性化推荐
- 协同过滤 + 内容推荐混合算法
- LLM增强推荐理由生成

### 2. 景点发现
- 基于LBS的附近景点发现
- 地图探索模式
- 多维度筛选（分类、标签、人流）

### 3. AI行程规划
- 自然语言需求输入
- 自动路线优化
- 一键生成完整行程

### 4. 社区互动
- UGC内容分享
- 评价与攻略
- 贡献积分系统

## API概览

| 模块 | 端点 | 说明 |
|------|------|------|
| 认证 | `POST /auth/login` | 用户登录 |
| | `POST /auth/register` | 用户注册 |
| 景点 | `GET /spots/recommendations` | AI推荐景点 |
| | `GET /spots/nearby` | 附近景点 |
| | `GET /spots/:id` | 景点详情 |
| 行程 | `POST /itineraries` | 创建行程 |
| | `GET /itineraries/me` | 我的行程 |
| AI | `POST /ai/itinerary/generate` | AI生成行程 |
| | `POST /ai/chat` | AI智能问答 |

详细API文档见 `docs/技术架构设计.md`

## 数据库模型

核心表:
- `users` - 用户信息
- `spots` - 景点数据
- `itineraries` - 行程
- `reviews` - 评价
- `posts` - 社区帖子
- `behaviors` - 用户行为日志

详细设计见 `docs/技术架构设计.md`

## 开发进度

### v1.0 MVP (当前)
- [x] 项目架构搭建
- [x] 后端核心API
- [x] Android基础UI
- [ ] 完整推荐功能
- [ ] AI行程生成
- [ ] 数据采集爬虫

### v1.1 (计划中)
- [ ] 离线地图
- [ ] 社区功能完善
- [ ] 会员体系

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 提交 Pull Request

## 许可证

MIT License

---

**秘境探索** - 发现小众，探索未知 🌿
