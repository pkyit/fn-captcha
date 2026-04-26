<template>
  <!--
    滑块验证码弹窗
    使用 Teleport 渲染到 body，避免被父级 overflow/层级 干扰
  -->
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="state.visible" class="captcha-overlay" @click.self="onClose">
        <div class="captcha-dialog">
          <!-- 弹窗头部：标题 + 关闭/刷新按钮 -->
          <div class="captcha-header">
            <span class="captcha-title">安全验证</span>
            <div class="header-actions">
              <!-- 加载失败时显示刷新按钮，让用户可以重新加载 -->
              <button
                v-if="state.status === 'failed'"
                class="header-btn"
                title="刷新验证码"
                @click="refreshCaptcha"
              >
                ↻
              </button>
              <button class="header-btn" title="关闭" @click="onClose">✕</button>
            </div>
          </div>

          <!-- 弹窗主体：根据状态显示不同内容 -->
          <div class="captcha-body">
            <!-- 加载中 -->
            <LoadingSpinner
              v-if="state.status === 'loading'"
              text="加载验证码中..."
            />

            <!-- 验证码已就绪 / 正在验证 -->
            <template v-if="state.status === 'ready' || state.status === 'verifying'">
              <!-- Canvas 画布：绘制背景 + 滑块 -->
              <CaptchaCanvas
                :background-image="state.backgroundImage"
                :slider-image="state.sliderImage"
                :slider-x="state.sliderX"
                :gap-y="state.gapY"
              />
              <!-- 滑块轨道（拖动控制） -->
              <SliderTrack
                :model-value="state.sliderX"
                :disabled="state.status === 'verifying'"
                :status="state.status"
                :gap-y="state.gapY"
                @update:model-value="onUpdateSlider"
                @drag-start="onDragStart"
                @drag-move="onDragMove"
                @drag-end="onDragEnd"
              />
            </template>

            <!-- 验证成功 -->
            <div v-if="state.status === 'success'" class="result result-success">
              <span class="result-icon">✓</span>
              <p class="result-text">验证通过</p>
              <p class="result-hint">即将自动关闭</p>
            </div>

            <!-- 验证失败 -->
            <div v-if="state.status === 'failed'" class="result result-failed">
              <span class="result-icon">✕</span>
              <p class="result-text">验证失败</p>
              <p class="result-hint">{{ state.errorMessage }}</p>
              <button class="retry-btn" @click="refreshCaptcha">重新验证</button>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { watch } from 'vue'
import { useCaptcha } from '@/composables/useCaptcha'
import CaptchaCanvas from './CaptchaCanvas.vue'
import SliderTrack from './SliderTrack.vue'
import LoadingSpinner from './LoadingSpinner.vue'

const emit = defineEmits<{
  /** 验证通过事件，父组件可监听此事件执行后续逻辑 */
  success: []
  /** 弹窗关闭事件 */
  close: []
}>()

// 内部使用 useCaptcha 管理所有状态
const {
  state,
  show,
  close,
  refresh,
  onDragStart,
  onDragMove,
  onDragEnd
} = useCaptcha()

// 暴露 open 方法，供父组件调用
function open() {
  show()
}

function onClose() {
  close()
  emit('close')
}

function refreshCaptcha() {
  refresh()
}

// 监听验证成功状态，通知父组件
watch(() => state.status, (status) => {
  if (status === 'success') {
    emit('success')
  }
})

/**
 * SliderTrack 发出 update:modelValue 时更新 state.sliderX
 * 注意：这里只更新数值，不记录轨迹（轨迹由 onDragMove 记录）
 */
function onUpdateSlider(value: number) {
  state.sliderX = value
}

defineExpose({ open })
</script>

<style scoped>
/* ===== 遮罩层 ===== */
.captcha-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: var(--color-bg-overlay);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(2px);
  -webkit-backdrop-filter: blur(2px);
}

/* ===== 弹窗主体 ===== */
.captcha-dialog {
  width: 340px;
  background: var(--color-bg-modal);
  border-radius: var(--radius-modal);
  box-shadow: var(--shadow-modal);
  overflow: hidden;
}

/* ===== 头部 ===== */
.captcha-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 16px 12px;
  border-bottom: 1px solid var(--color-border);
}

.captcha-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
}

.header-actions {
  display: flex;
  gap: 8px;
}

.header-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  color: var(--color-text-secondary);
  font-size: 16px;
  cursor: pointer;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s, color 0.15s;
}

.header-btn:hover {
  background: #f0f0f0;
  color: var(--color-text);
}

/* ===== 主体 ===== */
.captcha-body {
  padding: 16px;
  min-height: 200px;
}

/* ===== 结果提示 ===== */
.result {
  text-align: center;
  padding: 24px 0;
}

.result-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 12px;
}

.result-success .result-icon {
  background: #f0fff0;
  color: var(--color-success);
}

.result-failed .result-icon {
  background: #fff0f0;
  color: var(--color-error);
}

.result-text {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: 4px;
}

.result-hint {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-bottom: 16px;
}

.retry-btn {
  display: inline-block;
  padding: 8px 24px;
  border: none;
  border-radius: 4px;
  background: var(--color-primary);
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.15s;
}

.retry-btn:hover {
  background: var(--color-primary-light);
}
</style>
