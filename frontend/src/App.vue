<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { theme } from 'ant-design-vue'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'

// 设置 dayjs 语言
dayjs.locale('zh-cn')

// 响应式暗黑模式状态
const isDark = ref(document.documentElement.getAttribute('data-theme') === 'dark')

// 监听 data-theme 属性变化
const observer = new MutationObserver((mutations) => {
  for (const mutation of mutations) {
    if (mutation.attributeName === 'data-theme') {
      isDark.value = document.documentElement.getAttribute('data-theme') === 'dark'
    }
  }
})

onMounted(() => {
  observer.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['data-theme']
  })
})

onUnmounted(() => {
  observer.disconnect()
})

// Ant Design Vue 主题配置 — 响应式切换明暗算法
const themeConfig = computed(() => ({
  token: {
    colorPrimary: '#2563EB',
    borderRadius: 8,
    colorBgContainer: isDark.value ? '#0F172A' : '#ffffff',
    colorBgElevated: isDark.value ? '#1E293B' : '#ffffff',
    colorBgLayout: isDark.value ? '#020617' : '#F8FAFC',
    colorText: isDark.value ? '#E2E8F0' : '#1E293B',
    colorTextSecondary: isDark.value ? '#94A3B8' : '#64748B',
    colorTextTertiary: isDark.value ? '#64748B' : '#94A3B8',
    colorTextQuaternary: isDark.value ? '#475569' : '#CBD5E1',
    colorBorder: isDark.value ? '#334155' : '#E2E8F0',
    colorBorderSecondary: isDark.value ? '#1E293B' : '#F1F5F9',
    colorFill: isDark.value ? '#1E293B' : '#F1F5F9',
    colorFillSecondary: isDark.value ? '#334155' : '#E2E8F0',
    colorFillTertiary: isDark.value ? '#1E293B' : '#F8FAFC',
    colorBgTextHover: isDark.value ? 'rgba(255,255,255,0.06)' : 'rgba(0,0,0,0.04)',
    colorBgTextActive: isDark.value ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.06)',
  },
  algorithm: isDark.value ? theme.darkAlgorithm : theme.defaultAlgorithm
}))
</script>

<template>
  <a-config-provider :locale="zhCN" :theme="themeConfig">
    <router-view />
  </a-config-provider>
</template>

<style>
#app {
  width: 100%;
  height: 100%;
}
</style>
