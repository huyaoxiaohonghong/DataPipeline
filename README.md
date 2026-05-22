# DataPipeline

数据管道管理平台 - 全栈项目

## 技术栈

### Backend (Spring Boot)
- **JDK**: 25 (Preview Features Enabled)
- **Framework**: Spring Boot 4.0.x
- **ORM**: MyBatis-Plus
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

## Docker 一键部署 (推荐)

项目提供了基于 Docker 和 Docker Compose 的一键部署脚本 `install.sh`。该脚本会自动检测您的 Linux 环境是否已安装 Docker 及 Docker Compose。若未安装，它会提示并自动安装相关环境，然后自动构建并启动本系统。

### 部署步骤

1. **克隆项目到 Linux 服务器**：
   ```bash
   git clone https://github.com/huyaoxiaohonghong/DataPipeline.git
   cd DataPipeline
   ```

2. **赋予一键脚本执行权限**：
   ```bash
   chmod +x install.sh
   ```

3. **执行部署脚本（自动完成 Docker 安装与项目部署）**：
   ```bash
   ./install.sh
   ```

   *该脚本将自动为您处理以下过程：*
   *   检查并提示安装 `Docker` 环境（若缺失则自动下载并安装）
   *   检查并激活 `Docker` 系统服务
   *   检查并提示安装 `Docker Compose` 环境（若缺失则自动下载并配置）
   *   一键构建并启动后端（JDK 25 + Spring Boot 4）、前端（Nginx + Vue 3）及数据库（MySQL 8 + Redis 7）

4. **系统访问地址**：
   *   **前端网页入口**：`http://服务器IP:80`
   *   **后端 API 服务**：`http://服务器IP:8080`
   *   **默认内置账号**：
       *   超级管理员：`admin / admin@123`
       *   普通用户：`test / test123`

### 常用运维命令

*   **查看容器运行状态**：
    ```bash
    docker compose ps  # 或 docker-compose ps
    ```
*   **查看容器实时运行日志**：
    ```bash
    docker compose logs -f  # 或 docker-compose logs -f
    ```
*   **停止并清理容器**：
    ```bash
    docker compose down  # 或 docker-compose down
    ```

---

## 本地手动开发与启动

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
