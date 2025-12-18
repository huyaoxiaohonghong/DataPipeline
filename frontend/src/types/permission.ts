/**
 * 权限类型枚举
 */
export enum PermissionType {
    MENU = 'MENU',
    BUTTON = 'BUTTON',
    API = 'API'
}

/**
 * 权限实体
 */
export interface Permission {
    id: number
    code: string
    name: string
    type: PermissionType
    parentId: number
    path?: string
    icon?: string
    sort: number
    enabled: boolean
    children?: Permission[]
    createTime: string
    updateTime: string
}

/**
 * 权限创建请求
 */
export interface PermissionCreateRequest {
    code: string
    name: string
    type: PermissionType
    parentId?: number
    path?: string
    icon?: string
    sort?: number
    enabled?: boolean
}

/**
 * 权限更新请求
 */
export interface PermissionUpdateRequest {
    name?: string
    parentId?: number
    path?: string
    icon?: string
    sort?: number
    enabled?: boolean
}

export const PermissionTypeMap: Record<string, string> = {
    [PermissionType.MENU]: '菜单',
    [PermissionType.BUTTON]: '按钮',
    [PermissionType.API]: '接口'
}

export const PermissionTypeColorMap: Record<string, string> = {
    [PermissionType.MENU]: 'blue',
    [PermissionType.BUTTON]: 'green',
    [PermissionType.API]: 'orange'
}
