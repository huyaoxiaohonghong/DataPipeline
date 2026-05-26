import { get, post, put, del } from '@/utils/request'
import type { PageResult } from '@/types'

const BASE_URL = '/v1/datasources'

export interface DbConnection {
    id: number
    name: string
    dbType: string
    host: string
    port: number
    databaseName: string
    username: string
    password?: string
    description?: string
    enabled?: boolean
    createTime?: string
    updateTime?: string
}

export interface DbConnectionCreateRequest {
    name: string
    dbType: string
    host: string
    port: number
    databaseName: string
    username: string
    password?: string
}

export interface DbConnectionUpdateRequest {
    name?: string
    dbType?: string
    host?: string
    port?: number
    databaseName?: string
    username?: string
    password?: string
}

export interface ConnectionTestRequest {
    dbType: string
    host: string
    port: number
    databaseName: string
    username: string
    password?: string
}

export interface ColumnMetadata {
    columnName: string
    dataType: string
    columnComment: string
    isPrimaryKey: boolean
}

/**
 * 数据库连接管理 API
 */
export const datasourceApi = {
    /**
     * 分页查询数据源列表
     */
    list(params?: { current?: number; size?: number; name?: string; dbType?: string }) {
        return get<PageResult<DbConnection>>(BASE_URL, params)
    },

    /**
     * 根据 ID 查询数据源
     */
    getById(id: number) {
        return get<DbConnection>(`${BASE_URL}/${id}`)
    },

    /**
     * 创建数据源
     */
    create(data: DbConnectionCreateRequest) {
        return post<DbConnection>(BASE_URL, data)
    },

    /**
     * 更新数据源
     */
    update(id: number, data: DbConnectionUpdateRequest) {
        return put<DbConnection>(`${BASE_URL}/${id}`, data)
    },

    /**
     * 删除数据源
     */
    delete(id: number) {
        return del<void>(`${BASE_URL}/${id}`)
    },

    /**
     * 测试数据源连接
     */
    test(data: ConnectionTestRequest) {
        return post<boolean>(`${BASE_URL}/test`, data)
    },

    /**
     * 获取支持的数据库类型列表
     */
    getTypes() {
        return get<string[]>(`${BASE_URL}/types`)
    },

    /**
     * 获取指定数据源下的所有表名
     */
    getTables(id: number) {
        return get<string[]>(`${BASE_URL}/${id}/tables`)
    },

    /**
     * 获取指定表的列元数据信息
     */
    getColumns(id: number, tableName: string) {
        return get<any[]>(`${BASE_URL}/${id}/tables/${tableName}/columns`).then(res => {
            if (res && res.code === 200 && Array.isArray(res.data)) {
                res.data = res.data.map((item: any) => ({
                    columnName: item.columnName || item.name || '',
                    dataType: item.dataType || item.type || '',
                    columnComment: item.columnComment || item.comment || '',
                    isPrimaryKey: item.isPrimaryKey || item.primaryKey || false
                })) as any
            }
            return res as any
        })
    }
}
