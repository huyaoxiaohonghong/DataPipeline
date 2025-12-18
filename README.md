# DataPipeline

数据管道管理平台 - 全栈项目

## 技术栈

### Backend (Spring Boot)
- **JDK**: 25 (Preview Features Enabled)
- **Framework**: Spring Boot 4.0.x
- **ORM**: MyBatis-Flex
- **Database**: MySQL 8.x
- **Cache**: Redis 7.x
- **Connection Pool**: Druid
- **Thread Model**: Virtual Threads

### Frontend (Vue + Vite)
- **Framework**: Vue 3.5 + TypeScript
- **Build Tool**: Vite 6
- **UI Library**: Ant Design Vue 4.x
- **State Management**: Pinia
- **Router**: Vue Router 4
- **HTTP Client**: Axios

## 项目结构

```
DataPipeline/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/com/antigravity/
│   │   ├── common/            # 通用类 (Result, Exception)
│   │   ├── config/            # 配置类 (Redis, MyBatis)
│   │   ├── module/            # 业务模块
│   │   │   └── user/          # 用户模块
│   │   │       ├── User.java
│   │   │       ├── UserMapper.java
│   │   │       ├── UserService.java
│   │   │       ├── UserServiceImpl.java
│   │   │       ├── UserController.java
│   │   │       └── dto/
│   │   └── Application.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/init.sql
│   └── pom.xml
│
└── frontend/                   # Vue 前端
    ├── src/
    │   ├── api/               # API 接口
    │   ├── layouts/           # 布局组件
    │   ├── router/            # 路由配置
    │   ├── stores/            # Pinia 状态
    │   ├── styles/            # 全局样式
    │   ├── types/             # TypeScript 类型
    │   ├── utils/             # 工具函数
    │   ├── views/             # 页面组件
    │   ├── App.vue
    │   └── main.ts
    ├── package.json
    └── vite.config.ts
```

## 快速开始

### 后端

```bash
cd backend

# 1. 配置数据库 (修改 application.yml 或设置环境变量)
# 2. 执行数据库初始化脚本
mysql -u root -p < src/main/resources/db/init.sql

# 3. 启动应用
mvn spring-boot:run
```

### 前端

```bash
cd frontend

# 安装依赖
npm install --legacy-peer-deps

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

## API 接口

### 用户管理 `/api/v1/users`

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/v1/users` | 分页查询用户 |
| GET | `/api/v1/users/{id}` | 根据ID查询 |
| POST | `/api/v1/users` | 创建用户 |
| PUT | `/api/v1/users/{id}` | 更新用户 |
| DELETE | `/api/v1/users/{id}` | 删除用户 |

## 开发规范

### 后端
- 使用 QueryWrapper 链式调用，**禁止**使用 XML 编写简单 SQL
- Controller 层同步调用（虚拟线程已开启）
- 统一使用 `Result<T>` 响应结构

### 前端
- 组件按需自动导入
- 使用 TypeScript 严格类型
- Pinia 状态持久化

## License

MIT
