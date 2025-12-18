/// <reference types="vite/client" />

declare module '*.vue' {
    import type { DefineComponent } from 'vue'
    const component: DefineComponent<{}, {}, any>
    export default component
}

/**
 * 环境变量类型声明
 */
interface ImportMetaEnv {
    /** 应用标题 */
    readonly VITE_APP_TITLE: string
    /** API 基础路径 */
    readonly VITE_API_URL: string
}

interface ImportMeta {
    readonly env: ImportMetaEnv
}
