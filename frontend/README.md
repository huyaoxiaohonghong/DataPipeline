# DataPipeline Frontend

Vue 3 + TypeScript + Vite + Ant Design Vue 前端项目。

## 技术栈

- **框架**: Vue 3.5 + TypeScript
- **构建工具**: Vite 6
- **UI 组件库**: Ant Design Vue 4.x
- **状态管理**: Pinia + pinia-plugin-persistedstate
- **路由**: Vue Router 4
- **HTTP 请求**: Axios
- **CSS 预处理器**: Less

## 目录结构

```
frontend/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API 接口封装
│   ├── components/        # 公共组件
│   ├── layouts/           # 布局组件
│   ├── router/            # 路由配置
│   ├── stores/            # Pinia 状态管理
│   ├── styles/            # 全局样式
│   ├── types/             # TypeScript 类型定义
│   ├── utils/             # 工具函数
│   ├── views/             # 页面组件
│   ├── App.vue            # 根组件
│   └── main.ts            # 入口文件
├── index.html             # HTML 模板
├── package.json           # 项目配置
├── tsconfig.json          # TypeScript 配置
└── vite.config.ts         # Vite 配置
```

## 开发

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview
```

## 特性

- ✅ 组件按需自动导入 (unplugin-vue-components)
- ✅ API 按需自动导入 (unplugin-auto-import)
- ✅ Pinia 状态持久化 (pinia-plugin-persistedstate)
- ✅ 后端 API 代理 (/api -> http://localhost:8080)
- ✅ Less 预处理器支持
- ✅ 路径别名 (@/ -> src/)
