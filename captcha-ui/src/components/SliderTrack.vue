<template>
  <!--
    自定义滑块轨道组件
    完全使用 div 实现，不依赖原生 input range，兼容移动端触摸
  -->
  <div class="slider-track-container">
    <div
      ref="trackRef"
      class="slider-track"
      :class="{ disabled: disabled }"
    >
      <!-- 已滑动区域的填充条 -->
      <div
        class="slider-progress"
        :style="{ width: progressPercent + '%' }"
      />

      <!-- 拖拽拇指 -->
      <div
        class="slider-thumb"
        :class="{ dragging: isDragging, shake: shakeState }"
        :style="{ left: thumbLeftPx + 'px' }"
        @mousedown.prevent="onMouseDown"
        @touchstart.prevent="onTouchStart"
      >
        <span class="thumb-icon" v-text="thumbIcon" />
      </div>
    </div>

    <!-- 提示文字（拖动时隐藏） -->
    <p v-if="!isDragging && !disabled" class="slider-hint">
      {{ hintText }}
    </p>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { CANVAS_WIDTH } from '@/types/captcha'

const props = defineProps<{
  /** 滑块当前值（0 ~ maxX） */
  modelValue: number
  /** 是否禁用交互（正在验证中） */
  disabled: boolean
  /** 验证状态：用于切换 thumb 样式 */
  status: 'idle' | 'loading' | 'ready' | 'verifying' | 'success' | 'failed'
  /** 滑块 Y 坐标（用于轨迹记录） */
  gapY: number
}>()

const emit = defineEmits<{
  'update:modelValue': [value: number]
  'dragStart': [x: number, y: number]
  'dragMove': [x: number, y: number]
  'dragEnd': [x: number, y: number]
}>()

// 轨道宽度（与画布一致）
const TRACK_WIDTH = CANVAS_WIDTH
// 拇指宽度
const THUMB_SIZE = 40
// 最大可拖动值 = 轨道宽度 − 拇指宽度
const MAX_X = TRACK_WIDTH - THUMB_SIZE

const trackRef = ref<HTMLDivElement | null>(null)
const isDragging = ref(false)
// 失败时触发抖动动画
const shakeState = ref(false)

// 进度百分比（用于填充条宽度）
const progressPercent = computed(() =>
  Math.round((props.modelValue / MAX_X) * 100)
)

// 拇指的 left 值（px），映射到轨道上的实际位置
const thumbLeftPx = computed(() =>
  (props.modelValue / MAX_X) * (TRACK_WIDTH - THUMB_SIZE)
)

/** 拇指图标：根据状态切换 */
const thumbIcon = computed(() => {
  if (props.status === 'success') return '✓'
  if (props.status === 'failed') return '✕'
  return '⇨'
})

/** 提示文字 */
const hintText = computed(() => {
  if (props.status === 'failed') return '验证失败，请重试'
  return '拖动滑块完成拼图'
})

// 监听 failed 状态触发抖动
watch(() => props.status, (val) => {
  if (val === 'failed') {
    shakeState.value = true
    setTimeout(() => { shakeState.value = false }, 500)
  }
})

// ---------- 坐标计算工具 ----------

/** 根据鼠标/触摸事件计算相对于轨道的 X 偏移（已钳制在有效范围内） */
function calcX(clientX: number): number {
  const rect = trackRef.value!.getBoundingClientRect()
  // 减去拇指自身宽度的一半，让拇指中心跟随光标
  const raw = clientX - rect.left - THUMB_SIZE / 2
  return Math.max(0, Math.min(raw, MAX_X))
}

// ---------- 鼠标事件 ----------

/** 鼠标按下：开始拖拽 */
function onMouseDown(e: MouseEvent) {
  if (props.disabled) return
  isDragging.value = true
  const x = calcX(e.clientX)
  emit('update:modelValue', x)
  emit('dragStart', x, props.gapY)

  /** 在 document 上监听移动和松开，防止拖拽过快时丢失事件 */
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

function onMouseMove(e: MouseEvent) {
  if (!isDragging.value) return
  const x = calcX(e.clientX)
  emit('update:modelValue', x)
  emit('dragMove', x, props.gapY)
}

function onMouseUp(e: MouseEvent) {
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseup', onMouseUp)
  if (!isDragging.value) return
  isDragging.value = false
  const x = calcX(e.clientX)
  emit('dragEnd', x, props.gapY)
}

// ---------- 触摸事件 ----------

function onTouchStart(e: TouchEvent) {
  if (props.disabled) return
  isDragging.value = true
  const touch = e.touches[0]
  const x = calcX(touch.clientX)
  emit('update:modelValue', x)
  emit('dragStart', x, props.gapY)

  document.addEventListener('touchmove', onTouchMove, { passive: false })
  document.addEventListener('touchend', onTouchEnd)
}

function onTouchMove(e: TouchEvent) {
  e.preventDefault() // 防止页面滚动
  if (!isDragging.value) return
  const touch = e.touches[0]
  const x = calcX(touch.clientX)
  emit('update:modelValue', x)
  emit('dragMove', x, props.gapY)
}

function onTouchEnd(e: TouchEvent) {
  document.removeEventListener('touchmove', onTouchMove)
  document.removeEventListener('touchend', onTouchEnd)
  if (!isDragging.value) return
  isDragging.value = false
  const touch = e.changedTouches[0]
  const x = calcX(touch.clientX)
  emit('dragEnd', x, props.gapY)
}
</script>

<style scoped>
.slider-track-container {
  padding: 0;
}

.slider-track {
  position: relative;
  width: 300px;
  height: 40px;
  background: #e8e8e8;
  border-radius: 20px;
  margin: 0 auto;
  cursor: pointer;
}

.slider-track.disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* 已滑动填充条 */
.slider-progress {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: linear-gradient(90deg, var(--color-primary), var(--color-primary-light));
  border-radius: 20px;
  transition: width 0.05s linear;
  pointer-events: none;
}

/* 拖拽拇指 */
.slider-thumb {
  position: absolute;
  top: 0;
  width: 40px;
  height: 40px;
  background: #fff;
  border: 2px solid var(--color-primary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  cursor: grab;
  transition: box-shadow 0.15s, border-color 0.2s;
  z-index: 2;
}

.slider-thumb.dragging {
  cursor: grabbing;
  box-shadow: 0 3px 10px rgba(74, 125, 255, 0.35);
}

/* 状态样式：成功 / 失败由父组件通过 status 控制 */
.slider-thumb.shake {
  animation: shake 0.4s ease-in-out;
}

.thumb-icon {
  font-size: 16px;
  color: var(--color-primary);
  font-weight: bold;
}

/* 提示文字 */
.slider-hint {
  margin-top: 8px;
  text-align: center;
  font-size: 12px;
  color: var(--color-text-secondary);
}

/* 复用全局 shake 关键帧 */
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  20% { transform: translateX(-5px); }
  40% { transform: translateX(5px); }
  60% { transform: translateX(-4px); }
  80% { transform: translateX(4px); }
}
</style>
