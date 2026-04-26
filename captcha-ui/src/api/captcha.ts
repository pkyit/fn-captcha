import http from '@/api/index'
import type { CaptchaGenerateData, TrajectoryPoint } from '@/types/captcha'

/**
 * 生成滑块验证码
 * @param clientId 客户端唯一标识
 * @returns 验证码数据（含图片 base64 和 Y 轴位置）
 */
export async function generateCaptcha(clientId: string): Promise<CaptchaGenerateData> {
  const res = await http.post('/captcha/generate', { clientId })
  return res.data.data as CaptchaGenerateData
}

/**
 * 验证滑块验证码
 * @param params 验证参数
 * @param params.captchaId 验证码 ID
 * @param params.clientId 客户端 ID
 * @param params.sliderX 滑块最终 X 坐标
 * @param params.trajectory 滑动轨迹点集合
 */
export async function verifyCaptcha(params: {
  captchaId: string
  clientId: string
  sliderX: number
  trajectory: TrajectoryPoint[]
}): Promise<void> {
  await http.post('/captcha/verify', params)
}
