<template>
  <!--
    滑块验证码演示页面
    提供 "点击验证" 按钮触发验证码弹窗
  -->
  <div class="app-container">
    <header class="app-header">
      <h1>fn-captcha 滑块验证码</h1>
      <p class="app-desc">基于 Spring Boot 3 + Vue 3 的移动滑块验证码演示</p>
    </header>

    <main class="app-main">
      <div class="demo-card">
        <p class="demo-hint">点击下方按钮进行滑块验证</p>
        <button class="verify-btn" @click="handleVerify">
          点击验证
        </button>

        <!-- 验证结果反馈 -->
        <div v-if="lastResult" class="result-badge" :class="lastResult">
          {{ lastResult === 'success' ? '✓ 验证通过' : '✕ 验证失败' }}
        </div>
      </div>
    </main>

    <!-- 滑块验证码弹窗 -->
    <CaptchaModal
      ref="captchaModalRef"
      @success="onCaptchaSuccess"
      @close="onCaptchaClose"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CaptchaModal from '@/components/CaptchaModal.vue'

// 弹窗组件引用
const captchaModalRef = ref<InstanceType<typeof CaptchaModal> | null>(null)
// 最近一次验证结果，用于在页面上展示
const lastResult = ref<'success' | 'failed' | ''>('')

/** 点击 "点击验证" 按钮，打开验证码弹窗 */
function handleVerify() {
  lastResult.value = ''
  captchaModalRef.value?.open()
}

/** 验证成功回调 */
function onCaptchaSuccess() {
  lastResult.value = 'success'
}

/** 弹窗关闭回调 */
function onCaptchaClose() {
  if (lastResult.value !== 'success') {
    lastResult.value = 'failed'
  }
}
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.app-header {
  text-align: center;
  padding: 60px 20px 40px;
}

.app-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 8px;
}

.app-desc {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.app-main {
  flex: 1;
  display: flex;
  align-items: flex-start;
  justify-content: center;
}

.demo-card {
  background: #fff;
  border-radius: 8px;
  padding: 40px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.demo-hint {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-bottom: 20px;
}

.verify-btn {
  padding: 12px 48px;
  border: none;
  border-radius: 6px;
  background: var(--color-primary);
  color: #fff;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s, transform 0.1s;
}

.verify-btn:hover {
  background: var(--color-primary-light);
}

.verify-btn:active {
  transform: scale(0.97);
}

.result-badge {
  margin-top: 20px;
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
}

.result-badge.success {
  background: #f0fff0;
  color: var(--color-success);
}

.result-badge.failed {
  background: #fff0f0;
  color: var(--color-error);
}
</style>
