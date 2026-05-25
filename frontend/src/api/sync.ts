import { get, post, put, del } from '@/utils/request'
import type { PageResult } from '@/types'

// BASE URLs
const CONFIG_BASE_URL = '/v1/sync/configs'
const TASK_BASE_URL = '/v1/sync/tasks'

export interface FieldMappingDTO {
  id?: number
  sourceField: string
  sourceType: string
  targetField: string
  targetType: string
  transformExpr?: string
  sortOrder?: number
}

export interface SyncConfigVO {
  id: number
  name: string
  description?: string
  sourceDbId: number
  sourceDbName?: string
  sourceTable: string
  targetDbId: number
  targetDbName?: string
  targetTable: string
  syncMode: 'FULL' | 'INCREMENTAL'
  incrementalField?: string
  lastSyncValue?: string
  enabled: boolean
  createTime?: string
  updateTime?: string
  fieldMappings?: FieldMappingDTO[]
}

export interface SyncConfigCreateRequest {
  name: string
  description?: string
  sourceDbId: number
  sourceTable: string
  targetDbId: number
  targetTable: string
  syncMode: 'FULL' | 'INCREMENTAL'
  incrementalField?: string
  fieldMappings: FieldMappingDTO[]
}

export interface SyncConfigUpdateRequest {
  name?: string
  description?: string
  sourceDbId?: number
  sourceTable?: string
  targetDbId?: number
  targetTable?: string
  syncMode?: 'FULL' | 'INCREMENTAL'
  incrementalField?: string
  enabled?: boolean
  fieldMappings?: FieldMappingDTO[]
}

export interface SyncTaskVO {
  id: number
  configId: number
  configName: string
  seatunnelJobId?: string
  status: 'SUBMITTING' | 'RUNNING' | 'SUCCESS' | 'FAILED' | 'CANCELED'
  startTime?: string
  endTime?: string
  totalRows?: number
  syncRows?: number
  errorMessage?: string
  createTime?: string
  updateTime?: string
  durationSeconds?: number
}

export interface SyncTaskLogVO {
  id: number
  taskId: number
  logLevel: 'INFO' | 'WARN' | 'ERROR'
  message: string
  createTime: string
}

/**
 * 数据同步模块 API
 */
export const syncApi = {
  // ==================== 同步配置 ====================
  
  /**
   * 分页查询同步配置
   */
  listConfigs(params?: { pageNumber?: number; pageSize?: number; name?: string; syncMode?: string }) {
    return get<PageResult<SyncConfigVO>>(CONFIG_BASE_URL, params)
  },

  /**
   * 根据 ID 查询同步配置
   */
  getConfigById(id: number) {
    return get<SyncConfigVO>(`${CONFIG_BASE_URL}/${id}`)
  },

  /**
   * 创建同步配置
   */
  createConfig(data: SyncConfigCreateRequest) {
    return post<SyncConfigVO>(CONFIG_BASE_URL, data)
  },

  /**
   * 更新同步配置
   */
  updateConfig(id: number, data: SyncConfigUpdateRequest) {
    return put<SyncConfigVO>(`${CONFIG_BASE_URL}/${id}`, data)
  },

  /**
   * 删除同步配置
   */
  deleteConfig(id: number) {
    return del<void>(`${CONFIG_BASE_URL}/${id}`)
  },

  /**
   * 执行同步任务
   */
  executeConfig(id: number) {
    return post<SyncTaskVO>(`${CONFIG_BASE_URL}/${id}/execute`)
  },

  // ==================== 同步任务 ====================

  /**
   * 分页查询同步任务历史
   */
  listTasks(params?: { pageNumber?: number; pageSize?: number; configId?: number; status?: string }) {
    return get<PageResult<SyncTaskVO>>(TASK_BASE_URL, params)
  },

  /**
   * 查看任务详情
   */
  getTaskById(id: number) {
    return get<SyncTaskVO>(`${TASK_BASE_URL}/${id}`)
  },

  /**
   * 取消任务
   */
  cancelTask(id: number) {
    return post<void>(`${TASK_BASE_URL}/${id}/cancel`)
  },

  /**
   * 从 SeaTunnel 刷新并同步任务最新状态
   */
  refreshTask(id: number) {
    return post<SyncTaskVO>(`${TASK_BASE_URL}/${id}/refresh`)
  },

  /**
   * 查看任务日志
   */
  getTaskLogs(id: number) {
    return get<SyncTaskLogVO[]>(`${TASK_BASE_URL}/${id}/logs`)
  }
}
