import { get, post, put, del, patch } from '@/utils/request'
import type { PageResult } from '@/types'
import type { Role, RoleCreateRequest, RoleUpdateRequest, RoleQueryParams } from '@/types/role'

const BASE_URL = '/v1/roles'

/**
 * 角色 API
 */
export const roleApi = {
    /**
     * 分页查询角色列表
     */
    getPage(params: RoleQueryParams) {
        return get<PageResult<Role>>(BASE_URL, params)
    },

    /**
     * 获取所有角色
     */
    getAll() {
        return get<Role[]>(`${BASE_URL}/all`)
    },

    /**
     * 获取所有启用的角色
     */
    getEnabled() {
        return get<Role[]>(`${BASE_URL}/enabled`)
    },

    /**
     * 根据ID获取角色
     */
    getById(id: number) {
        return get<Role>(`${BASE_URL}/${id}`)
    },

    /**
     * 根据编码获取角色
     */
    getByCode(code: string) {
        return get<Role>(`${BASE_URL}/code/${code}`)
    },

    /**
     * 创建角色
     */
    create(data: RoleCreateRequest) {
        return post<Role>(BASE_URL, data)
    },

    /**
     * 更新角色
     */
    update(id: number, data: RoleUpdateRequest) {
        return put<Role>(`${BASE_URL}/${id}`, data)
    },

    /**
     * 更新角色状态
     */
    updateEnabled(id: number, enabled: boolean) {
        return patch<void>(`${BASE_URL}/${id}/enabled`, null, { enabled })
    },

    /**
     * 删除角色
     */
    delete(id: number) {
        return del<void>(`${BASE_URL}/${id}`)
    },

    /**
     * 批量删除角色
     */
    batchDelete(ids: number[]) {
        return del<void>(`${BASE_URL}/batch`, ids)
    },

    /**
     * 检查角色编码是否可用
     */
    checkCode(code: string) {
        return get<boolean>(`${BASE_URL}/check-code`, { code })
    }
}
