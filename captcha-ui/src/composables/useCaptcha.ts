import { reactive } from 'vue'
import { generateCaptcha, verifyCaptcha } from '@/api/captcha'
import {
  type CaptchaState,
  MAX_SLIDER_X,
  TRAJECTORY_SAMPLE_INTERVAL,
  AUTO_CLOSE_DELAY
} from '@/types/captcha'

/**
 * 滑块验证码核心业务逻辑
 * 管理验证码的加载、拖拽、轨迹记录、验证、刷新全生命周期
 */
export function useCaptcha() {
  // ===== 响应式状态 =====
  const state = reactive<CaptchaState>({
    visible: false,
    status: 'idle',
    captchaId: '',
    clientId: getOrCreateClientId(), // 优先从 localStorage 恢复
    backgroundImage: null,
    sliderImage: null,
    gapY: 0,
    sliderX: 0,
    trajectory: [],
    errorMessage: ''
  })

  /** 上一次轨迹采样的时间戳，用于节流 */
  let lastSampleTime = 0

  // ===== clientId 管理 =====

  /** 生成简易 UUID 作为客户端标识 */
  function generateClientId(): string {
    const ts = Date.now().toString(36)
    const rand = Math.random().toString(36).substring(2, 10)
    return `fe_${ts}_${rand}`
  }

  /** 获取或创建 clientId，优先复用 localStorage 中的值 */
  function getOrCreateClientId(): string {
    const KEY = 'fn_captcha_client_id'
    let id = localStorage.getItem(KEY)
    if (!id) {
      id = generateClientId()
      localStorage.setItem(KEY, id)
    }
    return id
  }

  // ===== 图片加载 =====

  /** Promise 包装的图片加载，确保图片完全解码后再进行 Canvas 绘制 */
  function loadImage(base64: string): Promise<HTMLImageElement> {
    return new Promise((resolve, reject) => {
      const img = new Image()
      img.onload = () => resolve(img)
      img.onerror = () => reject(new Error('图片加载失败，请检查网络'))
      img.src = base64
    })
  }

  // ===== 验证码生命周期 =====

  /** 打开弹窗并加载验证码 */
  async function show(): Promise<void> {
    state.visible = true
    state.status = 'loading'
    state.errorMessage = ''
    state.sliderX = 0
    state.trajectory = []
    lastSampleTime = 0

    try {
      // 1. 调用后端生成验证码
      const data = await generateCaptcha(state.clientId)
      state.captchaId = data.captchaId
      state.gapY = data.gapY

      // 2. 并行加载背景图和滑块图
      const [bgImg, sliderImg] = await Promise.all([
        loadImage(data.backgroundBase64),
        loadImage(data.sliderBase64)
      ])
      state.backgroundImage = bgImg
      state.sliderImage = sliderImg

      state.status = 'ready'
    } catch (e) {
      state.status = 'failed'
      state.errorMessage = e instanceof Error ? e.message : '验证码加载失败'
    }
  }

  /** 关闭弹窗并清理资源 */
  function close(): void {
    state.visible = false
    state.status = 'idle'
    // 释放 Image 对象，避免内存泄漏
    state.backgroundImage = null
    state.sliderImage = null
    state.trajectory = []
    state.sliderX = 0
    state.errorMessage = ''
  }

  /** 刷新验证码（失败后重试或主动刷新） */
  async function refresh(): Promise<void> {
    state.status = 'loading'
    state.errorMessage = ''
    state.sliderX = 0
    state.trajectory = []
    lastSampleTime = 0

    try {
      const data = await generateCaptcha(state.clientId)
      state.captchaId = data.captchaId
      state.gapY = data.gapY

      const [bgImg, sliderImg] = await Promise.all([
        loadImage(data.backgroundBase64),
        loadImage(data.sliderBase64)
      ])
      state.backgroundImage = bgImg
      state.sliderImage = sliderImg

      state.status = 'ready'
    } catch (e) {
      state.status = 'failed'
      state.errorMessage = e instanceof Error ? e.message : '验证码加载失败'
    }
  }

  // ===== 拖拽事件处理 =====

  /**
   * 拖拽开始：记录轨迹起点
   * 以当前鼠标 X 和 gapY 作为第一个轨迹点
   */
  function onDragStart(x: number): void {
    const now = Date.now()
    state.trajectory = [{ x, y: state.gapY, timestamp: now }]
    lastSampleTime = now
  }

  /**
   * 拖拽中：更新滑块位置 + 采样轨迹点
   * 采样频率限制在 TRAJECTORY_SAMPLE_INTERVAL 以上，避免产生过多冗余点
   */
  function onDragMove(sliderX: number): void {
    // 将 sliderX 钳制在有效范围 [0, MAX_SLIDER_X]
    state.sliderX = Math.max(0, Math.min(sliderX, MAX_SLIDER_X))

    const now = Date.now()
    // 节流：间隔不足时不采样，减少数据传输量
    if (now - lastSampleTime < TRAJECTORY_SAMPLE_INTERVAL) return

    state.trajectory.push({ x: state.sliderX, y: state.gapY, timestamp: now })
    lastSampleTime = now
  }

  /**
   * 拖拽结束：
   * 1. 记录最后一个轨迹点
   * 2. 校验轨迹数据合法性（点数、时长）
   * 3. 调用后端验证
   */
  async function onDragEnd(sliderX: number): Promise<void> {
    // 确保最终值在有效范围
    state.sliderX = Math.max(0, Math.min(sliderX, MAX_SLIDER_X))

    const now = Date.now()
    state.trajectory.push({ x: state.sliderX, y: state.gapY, timestamp: now })

    // ----- 前端前置校验 -----

    const duration = now - state.trajectory[0].timestamp

    // 轨迹点数不足（后端要求至少 5 个点）
    if (state.trajectory.length < 5) {
      state.status = 'failed'
      state.errorMessage = '滑动速度过快，请重试'
      return
    }

    // 滑动时间过短（后端要求至少 200ms）
    if (duration < 200) {
      state.status = 'failed'
      state.errorMessage = '滑动速度过快，请重试'
      return
    }

    // 滑动时间过长（后端上限 10000ms）
    if (duration > 10000) {
      state.status = 'failed'
      state.errorMessage = '验证已超时，请重试'
      return
    }

    // ----- 调用后端验证 -----
    state.status = 'verifying'

    try {
      await verifyCaptcha({
        captchaId: state.captchaId,
        clientId: state.clientId,
        sliderX: state.sliderX,
        trajectory: state.trajectory
      })
      // 验证通过
      state.status = 'success'
      // 短暂延迟后自动关闭，让用户看到成功提示
      setTimeout(() => { close() }, AUTO_CLOSE_DELAY)
    } catch (e) {
      // 验证失败（轨迹异常、距离偏差等）
      state.status = 'failed'
      state.errorMessage = e instanceof Error ? e.message : '验证失败，请重试'
    }
  }

  return {
    state,
    show,
    close,
    refresh,
    onDragStart,
    onDragMove,
    onDragEnd
  }
}
