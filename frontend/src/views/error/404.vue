<script setup lang="ts">
import { useRouter } from 'vue-router'
import { HomeOutlined, ArrowLeftOutlined } from '@ant-design/icons-vue'

const router = useRouter()

const goBack = () => {
  router.back()
}

const goHome = () => {
  router.push('/')
}
</script>

<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-code">404</div>
      <div class="error-title">页面不存在</div>
      <div class="error-desc">抱歉，您访问的页面不存在或已被移除</div>
      <div class="error-actions">
        <a-button type="primary" size="large" @click="goHome">
          <template #icon><HomeOutlined /></template>
          返回首页
        </a-button>
        <a-button size="large" @click="goBack">
          <template #icon><ArrowLeftOutlined /></template>
          返回上一页
        </a-button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  .error-content {
    text-align: center;
    padding: 40px;
    
    .error-code {
      font-size: 160px;
      font-weight: 700;
      color: rgba(255, 255, 255, 0.9);
      line-height: 1;
      text-shadow: 4px 4px 0 rgba(0, 0, 0, 0.1);
    }
    
    .error-title {
      font-size: 32px;
      font-weight: 600;
      color: #fff;
      margin-top: 24px;
    }
    
    .error-desc {
      font-size: 16px;
      color: rgba(255, 255, 255, 0.7);
      margin-top: 16px;
    }
    
    .error-actions {
      margin-top: 40px;
      display: flex;
      gap: 16px;
      justify-content: center;
    }
  }
}
</style>
