export * from './user'

/**
 * 分页结果
 */
export interface PageResult<T> {
    records: T[]
    pageNumber: number
    pageSize: number
    totalRow: number
    totalPage: number
}

/**
 * API 响应结构
 */
export interface ApiResponse<T = any> {
    code: number
    message: string
    data: T
}
