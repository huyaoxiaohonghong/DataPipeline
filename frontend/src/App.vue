<script setup lang="ts">
import { theme } from 'ant-design-vue'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'

// 设置 dayjs 语言
dayjs.locale('zh-cn')

// Ant Design Vue 主题配置
const themeConfig = {
  token: {
    colorPrimary: '#1890ff',
    borderRadius: 6
  },
  algorithm: theme.defaultAlgorithm
}
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
