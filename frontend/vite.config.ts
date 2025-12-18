import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite'
import AutoImport from 'unplugin-auto-import/vite'
import { AntDesignVueResolver } from 'unplugin-vue-components/resolvers'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig({
    plugins: [
        vue(),
        // 自动导入 API
        AutoImport({
            imports: [
                'vue',
                'vue-router',
                'pinia',
                {
                    'ant-design-vue': [
                        'message',
                        'notification',
                        'Modal'
                    ]
                }
            ],
            dts: 'auto-imports.d.ts',
            dirs: [
                'src/composables',
                'src/stores'
            ],
            vueTemplate: true
        }),
        // 自动导入组件
        Components({
            resolvers: [
                AntDesignVueResolver({
                    importStyle: false // 使用 CSS-in-JS，无需单独导入样式
                })
            ],
            dts: 'components.d.ts',
            dirs: ['src/components']
        })
    ],
    resolve: {
        alias: {
            '@': resolve(__dirname, 'src')
        }
    },
    css: {
        preprocessorOptions: {
            less: {
                javascriptEnabled: true,
                modifyVars: {
                    // Ant Design Vue 主题定制
                    'primary-color': '#1890ff',
                    'link-color': '#1890ff',
                    'border-radius-base': '4px'
                }
            }
        }
    },
    server: {
        port: 3000,
        open: true,
        proxy: {
            // 代理后端 API
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true
            }
        }
    },
    build: {
        rollupOptions: {
            output: {
                manualChunks: {
                    'ant-design-vue': ['ant-design-vue', '@ant-design/icons-vue'],
                    'vue-vendor': ['vue', 'vue-router', 'pinia']
                }
            }
        }
    }
})
