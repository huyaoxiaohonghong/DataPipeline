import { get, del } from '@/utils/request'
import type { PageResult } from '@/types'

export interface LoginLog {
    id: number
    username: string
    ipAddress: string
    browser: string
    os: string
    status: number
    message: string
    loginTime: string
}

export interface LoginLogQuery {
    current?: number
    size?: number
    username?: string
    status?: number
    startTime?: string
    endTime?: string
}

/**
 * 获取登录日志分页列表
 */
export function getLoginLogPage(params: LoginLogQuery) {
    return get<PageResult<LoginLog>>('/log/login/page', {
        page: params.current || 1,
        size: params.size || 10,
        username: params.username,
        status: params.status,
        startTime: params.startTime,
        endTime: params.endTime
    })
}

/**
 * 批量删除登录日志
 */
export function deleteLoginLog(ids: number[]) {
    return del<void>('/log/login/delete', { data: ids })
}

/**
 * 清空登录日志
 */
export function cleanLoginLog() {
    return del<void>('/log/login/clean')
}

