import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import router from './router'
import App from './App.vue'

// 全局样式
import './styles/index.less'

// 创建 Vue 应用
const app = createApp(App)

// 创建 Pinia 并配置持久化插件
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

// 挂载插件
app.use(pinia)
app.use(router)

// 挂载应用
app.mount('#app')
