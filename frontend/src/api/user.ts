import { get, post, put, del, patch } from '@/utils/request'
import type { User, UserCreateRequest, UserUpdateRequest, UserQueryParams } from '@/types/user'
import type { PageResult } from '@/types'

const BASE_URL = '/v1/users'

/**
 * 用户管理 API
 */
export const userApi = {
    /**
     * 分页查询用户列表
     */
    list(params: Partial<UserQueryParams>) {
        return get<PageResult<User>>(BASE_URL, params)
    },

    /**
     * 根据 ID 查询用户
     */
    getById(id: number) {
        return get<User>(`${BASE_URL}/${id}`)
    },

    /**
     * 根据用户名查询用户
     */
    getByUsername(username: string) {
        return get<User>(`${BASE_URL}/username/${username}`)
    },

    /**
     * 根据角色查询用户列表
     */
    listByRole(role: string) {
        return get<User[]>(`${BASE_URL}/role/${role}`)
    },

    /**
     * 创建用户
     */
    create(data: UserCreateRequest) {
        return post<User>(BASE_URL, data)
    },

    /**
     * 更新用户
     */
    update(id: number, data: UserUpdateRequest) {
        return put<User>(`${BASE_URL}/${id}`, data)
    },

    /**
     * 更新用户角色
     */
    updateRole(id: number, role: string) {
        return patch<void>(`${BASE_URL}/${id}/role`, undefined, { params: { role } })
    },

    /**
     * 修改密码
     */
    changePassword(id: number, oldPassword: string, newPassword: string) {
        return patch<void>(`${BASE_URL}/${id}/password`, { oldPassword, newPassword })
    },

    /**
     * 删除用户
     */
    delete(id: number) {
        return del<void>(`${BASE_URL}/${id}`)
    },

    /**
     * 批量删除用户
     */
    batchDelete(ids: number[]) {
        return del<void>(`${BASE_URL}/batch`, { data: ids })
    },

    /**
     * 检查用户名是否可用
     */
    checkUsername(username: string) {
        return get<boolean>(`${BASE_URL}/check-username`, { username })
    },

    /**
     * 检查邮箱是否可用
     */
    checkEmail(email: string) {
        return get<boolean>(`${BASE_URL}/check-email`, { email })
    }
}
