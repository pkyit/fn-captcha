/**
 * 滑块验证码前端类型定义
 * 与后端 Java 对象（CaptchaGenerateResponse、CaptchaVerifyDTO、TrajectoryPoint 等）对应
 */

/** 滑动轨迹点 —— 对应后端 TrajectoryPoint.java */
export interface TrajectoryPoint {
  /** 鼠标 X 坐标 */
  x: number
  /** 鼠标 Y 坐标 */
  y: number
  /** 采样时间戳（毫秒） */
  timestamp: number
}

/** 生成验证码接口响应数据 —— 对应后端 CaptchaGenerateResponse.java */
export interface CaptchaGenerateData {
  /** 验证码唯一标识（32 位无横杠 UUID） */
  captchaId: string
  /** 背景图 base64（JPEG 格式，含 data:image/jpeg;base64, 前缀） */
  backgroundBase64: string
  /** 滑块图 base64（PNG 格式，透明背景，含 data:image/png;base64, 前缀） */
  sliderBase64: string
  /** 滑块 Y 轴起始位置（后端返回，只告知纵向位置，隐藏缺口 X 坐标防作弊） */
  gapY: number
}

/** 统一 API 响应格式 —— 对应后端 Result.java */
export interface ApiResult<T> {
  code: number
  message: string
  data: T | null
}

/** 滑块验证码组件状态枚举 */
export type CaptchaStatus =
  | 'idle'        // 初始空闲
  | 'loading'     // 加载验证码中
  | 'ready'       // 就绪，等待用户滑动
  | 'verifying'   // 正在验证
  | 'success'     // 验证通过
  | 'failed'      // 验证失败

/** 验证码组件内部状态 */
export interface CaptchaState {
  /** 弹窗是否可见 */
  visible: boolean
  /** 当前状态 */
  status: CaptchaStatus
  /** 验证码唯一标识 */
  captchaId: string
  /** 客户端唯一标识 */
  clientId: string
  /** 背景图 Image 对象（已加载，可直接用于 Canvas 绘制） */
  backgroundImage: HTMLImageElement | null
  /** 滑块图 Image 对象 */
  sliderImage: HTMLImageElement | null
  /** 滑块 Y 轴位置（后端返回） */
  gapY: number
  /** 滑块当前 X 坐标（用户拖动控制，范围 0~264） */
  sliderX: number
  /** 当前滑动轨迹点集合 */
  trajectory: TrajectoryPoint[]
  /** 失败时的错误消息 */
  errorMessage: string
}

/** Canvas 画布宽度（与背景图宽度一致） */
export const CANVAS_WIDTH = 300
/** Canvas 画布高度（与背景图高度一致） */
export const CANVAS_HEIGHT = 150
/** 滑块尺寸（后端生成 36×36 圆形） */
export const SLIDER_SIZE = 36
/** 滑块最大可拖动 X 坐标（贴紧右边缘时） */
export const MAX_SLIDER_X = CANVAS_WIDTH - SLIDER_SIZE // = 264
/** 轨迹采样最小间隔（毫秒），避免产生过多冗余点 */
export const TRAJECTORY_SAMPLE_INTERVAL = 30
/** 验证通过后自动关闭弹窗的延迟时间（毫秒） */
export const AUTO_CLOSE_DELAY = 1500
