import { get, del } from '@/utils/request'
import type { PageResult } from '@/types'

export interface OperationLog {
    id: number
    username: string
    module: string
    action: string
    method: string
    params: string
    ipAddress: string
    duration: number
    status: number
    errorMsg: string
    createTime: string
}

export interface OperationLogQuery {
    current?: number
    size?: number
    username?: string
    module?: string
    status?: number
    startTime?: string
    endTime?: string
}

/**
 * 获取操作日志分页列表
 */
export function getOperationLogPage(params: OperationLogQuery) {
    return get<PageResult<OperationLog>>('/log/operation/page', {
        page: params.current || 1,
        size: params.size || 10,
        username: params.username,
        module: params.module,
        status: params.status,
        startTime: params.startTime,
        endTime: params.endTime
    })
}

/**
 * 批量删除操作日志
 */
export function deleteOperationLog(ids: number[]) {
    return del<void>('/log/operation/delete', { data: ids })
}

/**
 * 清空操作日志
 */
export function cleanOperationLog() {
    return del<void>('/log/operation/clean')
}

