/**
 * 用户接口（与后端 Entity 对应）
 */
export interface User {
    id: number
    username: string
    email: string
    role: string
    createTime: string
    updateTime: string
}

/**
 * 用户角色枚举
 */
export enum UserRole {
    ADMIN = 'ADMIN',
    USER = 'USER',
    GUEST = 'GUEST'
}

/**
 * 角色选项
 */
export const RoleOptions = [
    { label: '管理员', value: UserRole.ADMIN, color: 'red' },
    { label: '普通用户', value: UserRole.USER, color: 'blue' },
    { label: '访客', value: UserRole.GUEST, color: 'default' }
]

/**
 * 获取角色标签信息
 */
export const getRoleInfo = (role: string) => {
    return RoleOptions.find(item => item.value === role) || { label: role, color: 'default' }
}

/**
 * 用户创建请求
 */
export interface UserCreateRequest {
    username: string
    password: string
    email?: string
    role?: string
}

/**
 * 用户更新请求
 */
export interface UserUpdateRequest {
    username?: string
    email?: string
    role?: string
}

/**
 * 修改密码请求
 */
export interface ChangePasswordRequest {
    oldPassword: string
    newPassword: string
}

/**
 * 用户查询参数
 */
export interface UserQueryParams {
    pageNumber: number
    pageSize: number
    username?: string
    role?: string
}
