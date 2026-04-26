import axios from 'axios'
import type { ApiResult } from '@/types/captcha'

/**
 * Axios 实例
 * baseURL 设为 /fnCaptcha，开发时由 Vite proxy 转发到后端 18080 端口
 */
const http = axios.create({
  baseURL: '/fnCaptcha',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

/** 响应拦截器：提取 data 层，按业务 code 统一处理 */
http.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResult<unknown>
    // 后端统一返回格式：{ code, message, data }，code=200 视为成功
    if (body.code === 200) {
      return response
    }
    // 业务错误（如验证码过期、轨迹异常等），将 message 作为错误抛出
    return Promise.reject(new Error(body.message || '请求失败'))
  },
  (error) => {
    // 网络错误 / 超时
    return Promise.reject(new Error(error.message || '网络异常，请稍后重试'))
  }
)

export default http
