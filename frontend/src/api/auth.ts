import { get, post } from '@/utils/request'

/**
 * 登录请求参数
 */
export interface LoginRequest {
  username: string
  password: string
  captchaToken?: string
}

export interface CaptchaResponse {
  captchaId: string
  backgroundImage: string
  sliderImage: string
  sliderY: number
}

export interface CaptchaVerifyRequest {
  captchaId: string
  sliderX: number
}

export interface CaptchaVerifyResponse {
  success: boolean
  token: string
  message: string
}

/**
 * 登录响应
 */
export interface LoginResponse {
  token: string
  tokenType: string
  expiresIn: number
  userId: number
  username: string
  role: string
}

const BASE_URL = '/v1/auth'

/**
 * 认证 API
 */
export const authApi = {
  /**
   * 用户登录
   */
  login(data: LoginRequest) {
    return post<LoginResponse>(`${BASE_URL}/login`, data)
  },

  /**
   * 用户登出
   */
  logout() {
    return post<void>(`${BASE_URL}/logout`)
  },

  /**
   * 获取当前用户信息
   */
  getCurrentUser() {
    return get<LoginResponse>(`${BASE_URL}/me`)
  },

  /**
   * 获取验证码
   */
  getCaptcha() {
    return get<CaptchaResponse>('/v1/captcha/generate')
  },

  /**
   * 校验验证码
   */
  verifyCaptcha(data: CaptchaVerifyRequest) {
    return post<CaptchaVerifyResponse>('/v1/captcha/verify', data)
  }
}
