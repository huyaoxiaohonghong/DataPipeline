import { get, post, put, del, patch } from '@/utils/request'
import type { Permission, PermissionCreateRequest, PermissionUpdateRequest } from '@/types/permission'

const BASE_URL = '/v1/permissions'

/**
 * 权限 API
 */
export const permissionApi = {
    /**
     * 获取所有权限列表
     * @param type 权限类型过滤
     */
    getList(type?: string) {
        return get<Permission[]>(BASE_URL, { type })
    },

    /**
     * 获取权限树
     */
    getTree() {
        return get<Permission[]>(`${BASE_URL}/tree`)
    },

    /**
     * 获取所有启用的权限
     */
    getEnabled() {
        return get<Permission[]>(`${BASE_URL}/enabled`)
    },

    /**
     * 根据ID获取权限
     */
    getById(id: number) {
        return get<Permission>(`${BASE_URL}/${id}`)
    },

    /**
     * 根据编码获取权限
     */
    getByCode(code: string) {
        return get<Permission>(`${BASE_URL}/code/${code}`)
    },

    /**
     * 创建权限
     */
    create(data: PermissionCreateRequest) {
        return post<Permission>(BASE_URL, data)
    },

    /**
     * 更新权限
     */
    update(id: number, data: PermissionUpdateRequest) {
        return put<Permission>(`${BASE_URL}/${id}`, data)
    },

    /**
     * 更新权限状态
     */
    updateEnabled(id: number, enabled: boolean) {
        return patch<void>(`${BASE_URL}/${id}/enabled`, null, { params: { enabled } })
    },

    /**
     * 删除权限
     */
    delete(id: number) {
        return del<void>(`${BASE_URL}/${id}`)
    },

    /**
     * 批量删除权限
     */
    batchDelete(ids: number[]) {
        return del<void>(`${BASE_URL}/batch`, { data: ids })
    },

    /**
     * 检查权限编码是否可用
     */
    checkCode(code: string) {
        return get<boolean>(`${BASE_URL}/check-code`, { code })
    }
}

/**
 * 角色权限关联 API
 */
export const rolePermissionApi = {
    /**
     * 获取角色的权限列表
     */
    getPermissionsByRoleId(roleId: number) {
        return get<Permission[]>(`/v1/role-permissions/${roleId}`)
    },

    /**
     * 获取角色的权限ID列表
     */
    getPermissionIdsByRoleId(roleId: number) {
        return get<number[]>(`/v1/role-permissions/${roleId}/ids`)
    },

    /**
     * 为角色分配权限
     */
    assignPermissions(roleId: number, permissionIds: number[]) {
        return post<void>(`/v1/role-permissions/${roleId}`, permissionIds)
    }
}
