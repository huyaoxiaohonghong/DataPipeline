/**
 * 角色实体
 */
export interface Role {
    id: number
    code: string
    name: string
    description?: string
    sort: number
    enabled: boolean
    createTime: string
    updateTime: string
}

/**
 * 角色创建请求
 */
export interface RoleCreateRequest {
    code: string
    name: string
    description?: string
    sort?: number
    enabled?: boolean
}

/**
 * 角色更新请求
 */
export interface RoleUpdateRequest {
    name?: string
    description?: string
    sort?: number
    enabled?: boolean
}

/**
 * 角色查询参数
 */
export interface RoleQueryParams {
    pageNumber?: number
    pageSize?: number
    name?: string
    code?: string
    enabled?: boolean
}
