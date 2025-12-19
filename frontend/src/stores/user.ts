import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import { setToken, removeToken } from '@/utils/request'
import router from '@/router'

/**
 * 用户状态管理
 */
export const useUserStore = defineStore('user', () => {
    // ==================== 状态 ====================

    const token = ref<string | null>(localStorage.getItem('data-pipeline-token'))
    const userId = ref<number | null>(null)
    const username = ref<string>('')
    const role = ref<string>('')

    // ==================== 计算属性 ====================

    const isLoggedIn = computed(() => !!token.value)
    const isAdmin = computed(() => role.value === 'ADMIN')

    // ==================== 操作 ====================

    /**
     * 登录
     */
    async function login(loginUsername: string, password: string, captchaToken?: string) {
        const { data } = await authApi.login({ username: loginUsername, password, captchaToken })

        // 保存 Token
        token.value = data.token
        setToken(data.token)

        // 保存用户信息
        userId.value = data.userId
        username.value = data.username
        role.value = data.role

        return data
    }

    /**
     * 登出
     */
    async function logout() {
        try {
            await authApi.logout()
        } catch (error) {
            // 忽略登出错误
            console.warn('登出请求失败:', error)
        } finally {
            // 清除本地状态
            clearUserInfo()
            // 跳转登录页
            router.push('/login')
        }
    }

    /**
     * 获取当前用户信息
     */
    async function fetchCurrentUser() {
        if (!token.value) {
            return null
        }

        try {
            const { data } = await authApi.getCurrentUser()
            userId.value = data.userId
            username.value = data.username
            role.value = data.role
            return data
        } catch (error) {
            // Token 无效，清除登录状态
            clearUserInfo()
            return null
        }
    }

    /**
     * 清除用户信息
     */
    function clearUserInfo() {
        token.value = null
        userId.value = null
        username.value = ''
        role.value = ''
        removeToken()
    }

    return {
        // 状态
        token,
        userId,
        username,
        role,
        // 计算属性
        isLoggedIn,
        isAdmin,
        // 操作
        login,
        logout,
        fetchCurrentUser,
        clearUserInfo
    }
}, {
    persist: {
        key: 'data-pipeline-user',
        storage: localStorage,
        paths: ['token', 'userId', 'username', 'role']
    }
})
