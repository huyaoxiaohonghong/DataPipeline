import { get, post } from '@/utils/request'

const BASE_URL = '/v1/seatunnel'

/**
 * SeaTunnel 集群概览信息
 */
export interface ClusterOverview {
  status?: string
  masterNodeAddress?: string
  nodeCount?: number
  memberCount?: number
  members?: string[]
  cpuUsage?: string
  memoryUsage?: string
  error?: string
  [key: string]: any
}

/**
 * SeaTunnel 运行中的任务
 */
export interface RunningJob {
  jobId: string
  jobName: string
  jobStatus: string
  status: string
  submitTime: string
  envOptions?: Record<string, any>
  createTime?: string
  [key: string]: any
}

/**
 * SeaTunnel 任务详情
 */
export interface JobDetail {
  jobId: string
  jobName: string
  jobStatus: string
  submitTime?: string
  createTime?: string
  finishTime?: string
  envOptions?: Record<string, any>
  metrics?: Record<string, any>
  pluginJarsUrls?: string[]
  jobDag?: {
    vertices?: Array<{
      vertexId: string | number
      type: string
      action: string
      pluginName?: string
      connectorType?: string
      tableFullName?: string
    }>
    edges?: Array<{
      inputVertexId: string | number
      targetVertexId: string | number
    }>
  }
  isStartWithSavePoint?: boolean
  [key: string]: any
}

/**
 * SeaTunnel 集群管理 API
 */
export const seatunnelApi = {
  /**
   * 获取 SeaTunnel 集群概览
   */
  getOverview() {
    return get<ClusterOverview>(`${BASE_URL}/overview`)
  },

  /**
   * 获取 SeaTunnel 运行中的任务
   */
  getRunningJobs() {
    return get<RunningJob[]>(`${BASE_URL}/running-jobs`)
  },

  /**
   * 获取指定任务的详情
   */
  getJobDetail(jobId: string) {
    return get<JobDetail>(`${BASE_URL}/job/${jobId}`)
  },

  /**
   * 停止指定任务
   */
  stopJob(jobId: string) {
    return post<void>(`${BASE_URL}/job/${jobId}/stop`)
  }
}
