<template>
  <!--
    验证码画布组件
    在 Canvas 上绘制背景图 + 滑块图，滑块位置由 sliderX 控制
  -->
  <div class="canvas-wrapper">
    <canvas
      ref="canvasRef"
      :width="CANVAS_WIDTH"
      :height="CANVAS_HEIGHT"
      class="captcha-canvas"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, nextTick } from 'vue'
import { CANVAS_WIDTH, CANVAS_HEIGHT, SLIDER_SIZE } from '@/types/captcha'

const props = defineProps<{
  /** 背景图 Image 对象（已加载完成） */
  backgroundImage: HTMLImageElement | null
  /** 滑块图 Image 对象（已加载完成） */
  sliderImage: HTMLImageElement | null
  /** 滑块当前 X 坐标（由拖拽驱动） */
  sliderX: number
  /** 滑块 Y 轴固定位置（后端返回） */
  gapY: number
}>()

const canvasRef = ref<HTMLCanvasElement | null>(null)

/** 重新绘制整个画布 */
function draw() {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  // 1. 清空画布
  ctx.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT)

  // 2. 绘制背景图（不含滑块缺口，因为我们拿到的是原图）
  if (props.backgroundImage) {
    ctx.drawImage(props.backgroundImage, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT)
  }

  // 3. 在滑块位置绘制滑块图（PNG 透明背景，直接叠加即可填补缺口）
  if (props.sliderImage) {
    // 优化：绘制前添加轻微阴影，让滑块看起来浮在背景上，视觉更真实
    ctx.save()
    ctx.shadowColor = 'rgba(0, 0, 0, 0.35)'
    ctx.shadowBlur = 6
    ctx.shadowOffsetX = 2
    ctx.drawImage(props.sliderImage, props.sliderX, props.gapY, SLIDER_SIZE, SLIDER_SIZE)
    ctx.restore()
  }
}

// 监听 sliderX 变化，实时重绘
watch(
  () => [props.sliderX, props.backgroundImage, props.sliderImage, props.gapY],
  () => { nextTick(draw) },
  { deep: false }
)

onMounted(() => { nextTick(draw) })
</script>

<style scoped>
.canvas-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 12px;
}

.captcha-canvas {
  width: 300px;
  height: 150px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  /* 禁止用户选中画布内容 */
  user-select: none;
  -webkit-user-select: none;
}
</style>
