import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 应用状态管理
 */
export const useAppStore = defineStore('app', () => {
    // 侧边栏折叠状态
    const collapsed = ref(false)

    // 暗黑模式
    const darkMode = ref(false)

    // 全局加载状态
    const loading = ref(false)

    // 切换侧边栏折叠
    function toggleCollapsed() {
        collapsed.value = !collapsed.value
    }

    // 设置折叠状态
    function setCollapsed(value: boolean) {
        collapsed.value = value
    }

    // 切换暗黑模式
    function toggleDarkMode() {
        darkMode.value = !darkMode.value
    }

    // 设置加载状态
    function setLoading(value: boolean) {
        loading.value = value
    }

    return {
        collapsed,
        darkMode,
        loading,
        toggleCollapsed,
        setCollapsed,
        toggleDarkMode,
        setLoading
    }
}, {
    persist: {
        key: 'data-pipeline-app',
        storage: localStorage,
        pick: ['collapsed', 'darkMode']
    }
})
