import axios, {
    type AxiosInstance,
    type AxiosRequestConfig,
    type AxiosResponse,
    type InternalAxiosRequestConfig
} from 'axios'
import { message } from 'ant-design-vue'
import router from '@/router'

/**
 * API 响应结构
 */
interface ApiResponse<T = any> {
    code: number
    message: string
    data: T
}

/**
 * Token 存储 Key
 */
const TOKEN_KEY = 'data-pipeline-token'

/**
 * 获取 Token
 */
const getToken = (): string | null => {
    return localStorage.getItem(TOKEN_KEY)
}

/**
 * 设置 Token
 */
export const setToken = (token: string): void => {
    localStorage.setItem(TOKEN_KEY, token)
}

/**
 * 移除 Token
 */
export const removeToken = (): void => {
    localStorage.removeItem(TOKEN_KEY)
}

/**
 * 创建 Axios 实例
 */
const instance: AxiosInstance = axios.create({
    baseURL: import.meta.env.VITE_API_URL || '/api',
    timeout: 30000,
    headers: {
        'Content-Type': 'application/json'
    }
})

/**
 * 请求拦截器
 * - 自动在 Header 中携带 Token
 */
instance.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const token = getToken()
        if (token && config.headers) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => {
        console.error('请求错误:', error)
        return Promise.reject(error)
    }
)

/**
 * 响应拦截器
 * - 拦截非 200 状态码，提示错误
 * - 处理 401 状态码跳转登录页
 */
instance.interceptors.response.use(
    (response: AxiosResponse<ApiResponse>) => {
        const { code, message: msg, data } = response.data

        // 业务状态码 200 表示成功
        if (code === 200) {
            return response.data
        }

        // 业务错误，显示错误消息
        message.error(msg || '请求失败')
        return Promise.reject(new Error(msg || '请求失败'))
    },
    (error) => {
        // HTTP 错误处理
        if (error.response) {
            const { status, data } = error.response
            const errorMsg = data?.message || error.message

            switch (status) {
                case 401:
                    message.error('登录已过期，请重新登录')
                    // 清除 Token 并跳转登录页
                    removeToken()
                    router.push({
                        path: '/login',
                        query: { redirect: router.currentRoute.value.fullPath }
                    })
                    break

                case 403:
                    message.error('没有权限访问该资源')
                    break

                case 404:
                    message.error('请求的资源不存在')
                    break

                case 500:
                    message.error(errorMsg || '服务器内部错误')
                    break

                default:
                    message.error(errorMsg || `请求失败 (${status})`)
            }
        } else if (error.request) {
            // 请求已发出但没有收到响应
            message.error('网络连接失败，请检查网络')
        } else {
            // 请求配置错误
            message.error('请求配置错误')
        }

        return Promise.reject(error)
    }
)

/**
 * GET 请求
 */
export const get = <T = any>(
    url: string,
    params?: Record<string, any>,
    config?: AxiosRequestConfig
): Promise<ApiResponse<T>> => {
    return instance.get(url, { params, ...config })
}

/**
 * POST 请求
 */
export const post = <T = any>(
    url: string,
    data?: Record<string, any>,
    config?: AxiosRequestConfig
): Promise<ApiResponse<T>> => {
    return instance.post(url, data, config)
}

/**
 * PUT 请求
 */
export const put = <T = any>(
    url: string,
    data?: Record<string, any>,
    config?: AxiosRequestConfig
): Promise<ApiResponse<T>> => {
    return instance.put(url, data, config)
}

/**
 * DELETE 请求
 */
export const del = <T = any>(
    url: string,
    config?: AxiosRequestConfig
): Promise<ApiResponse<T>> => {
    return instance.delete(url, config)
}

/**
 * PATCH 请求
 */
export const patch = <T = any>(
    url: string,
    data?: Record<string, any>,
    config?: AxiosRequestConfig
): Promise<ApiResponse<T>> => {
    return instance.patch(url, data, config)
}

/**
 * 导出 Axios 实例（用于特殊场景）
 */
export default instance
