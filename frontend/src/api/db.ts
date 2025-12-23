import { get, post, put, del, patch } from '@/utils/request'
import type { PageResult } from '@/types'

const BASE_URL = '/v1/db-connections'

/**
 * 数据库连接类型
 */
export interface DbConnection {
    id: number
    name: string
    dbType: string
    host: string
    port: number
    databaseName: string
    username: string
    description?: string
    enabled: boolean
    createTime: string
    updateTime: string
}

/**
 * 创建数据库连接请求
 */
export interface DbConnectionCreateRequest {
    name: string
    dbType: string
    host: string
    port: number
    databaseName: string
    username: string
    password: string
    description?: string
}

/**
 * 更新数据库连接请求
 */
export interface DbConnectionUpdateRequest {
    name?: string
    dbType?: string
    host?: string
    port?: number
    databaseName?: string
    username?: string
    password?: string
    description?: string
    enabled?: boolean
}

/**
 * 测试连接请求
 */
export interface DbConnectionTestRequest {
    dbType: string
    host: string
    port: number
    databaseName: string
    username: string
    password: string
}

/**
 * 数据库监控信息
 */
export interface DbMonitorInfo {
    status: string
    version?: string
    activeConnections?: number
    maxConnections?: number
    databaseSizeMB?: number
    tableCount?: number
    tables?: string[]
    errorMessage?: string
    responseTimeMs?: number
}

/**
 * 查询参数
 */
export interface DbConnectionQueryParams {
    pageNumber: number
    pageSize: number
    name?: string
    dbType?: string
}

/**
 * 数据库类型选项
 */
export const DbTypeOptions = [
    { label: 'MySQL', value: 'MYSQL' },
    { label: 'PostgreSQL', value: 'POSTGRESQL' },
    { label: 'Oracle', value: 'ORACLE' },
    { label: 'SQL Server', value: 'SQLSERVER' }
]

/**
 * 获取数据库类型信息
 */
export const getDbTypeInfo = (type: string) => {
    const typeMap: Record<string, { label: string; color: string }> = {
        MYSQL: { label: 'MySQL', color: 'blue' },
        POSTGRESQL: { label: 'PostgreSQL', color: 'cyan' },
        ORACLE: { label: 'Oracle', color: 'red' },
        SQLSERVER: { label: 'SQL Server', color: 'purple' }
    }
    return typeMap[type] || { label: type, color: 'default' }
}

/**
 * 数据库连接管理 API
 */
export const dbConnectionApi = {
    /**
     * 分页查询连接列表
     */
    list(params: Partial<DbConnectionQueryParams>) {
        return get<PageResult<DbConnection>>(BASE_URL, params)
    },

    /**
     * 根据 ID 查询连接
     */
    getById(id: number) {
        return get<DbConnection>(`${BASE_URL}/${id}`)
    },

    /**
     * 创建连接
     */
    create(data: DbConnectionCreateRequest) {
        return post<DbConnection>(BASE_URL, data)
    },

    /**
     * 更新连接
     */
    update(id: number, data: DbConnectionUpdateRequest) {
        return put<DbConnection>(`${BASE_URL}/${id}`, data)
    },

    /**
     * 删除连接
     */
    delete(id: number) {
        return del<void>(`${BASE_URL}/${id}`)
    },

    /**
     * 批量删除连接
     */
    batchDelete(ids: number[]) {
        return del<void>(`${BASE_URL}/batch`, { data: ids })
    },

    /**
     * 测试已保存的连接
     */
    testById(id: number) {
        return post<DbMonitorInfo>(`${BASE_URL}/${id}/test`)
    },

    /**
     * 测试临时连接参数
     */
    test(data: DbConnectionTestRequest) {
        return post<DbMonitorInfo>(`${BASE_URL}/test`, data)
    },

    /**
     * 获取监控信息
     */
    getMonitor(id: number) {
        return get<DbMonitorInfo>(`${BASE_URL}/${id}/monitor`)
    },

    /**
     * 获取表列表
     */
    getTables(id: number) {
        return get<string[]>(`${BASE_URL}/${id}/tables`)
    },

    /**
     * 更新状态
     */
    updateEnabled(id: number, enabled: boolean) {
        return patch<void>(`${BASE_URL}/${id}/enabled`, undefined, { params: { enabled } })
    }
}
