<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { authApi, type CaptchaResponse } from '@/api/auth'
import { message } from 'ant-design-vue'
import { ReloadOutlined, CheckCircleOutlined, CloseCircleOutlined } from '@ant-design/icons-vue'

const emit = defineEmits(['success', 'close'])

const loading = ref(false)
const captchaData = ref<CaptchaResponse | null>(null)
const sliderX = ref(0)
const isDragging = ref(false)
const startX = ref(0)
const status = ref<'default' | 'success' | 'error'>('default')

// 刷新验证码
const refreshCaptcha = async () => {
  loading.value = true
  status.value = 'default'
  sliderX.value = 0
  try {
    const res = await authApi.getCaptcha()
    if (res.code === 200) {
      captchaData.value = res.data
    }
  } catch (error) {
    console.error('获取验证码失败', error)
  } finally {
    loading.value = false
  }
}

// 开始拖动
const handleDragStart = (e: MouseEvent | TouchEvent) => {
  if (status.value === 'success' || loading.value) return
  isDragging.value = true
  startX.value = 'touches' in e ? e.touches[0].clientX : e.clientX
}

// 拖动中
const handleDragMove = (e: MouseEvent | TouchEvent) => {
  if (!isDragging.value) return
  
  const clientX = 'touches' in e ? e.touches[0].clientX : e.clientX
  const moveX = clientX - startX.value
  
  // 限制滑动范围
  if (moveX < 0) {
    sliderX.value = 0
  } else if (moveX > 250) { // 300(bg) - 50(slider)
    sliderX.value = 250
  } else {
    sliderX.value = moveX
  }
}

// 结束拖动
const handleDragEnd = async () => {
  if (!isDragging.value) return
  isDragging.value = false
  
  if (!captchaData.value) return
  
  try {
    const res = await authApi.verifyCaptcha({
      captchaId: captchaData.value.captchaId,
      sliderX: Math.round(sliderX.value)
    })
    
    if (res.code === 200 && res.data.success) {
      status.value = 'success'
      message.success('验证通过')
      setTimeout(() => {
        emit('success', res.data.token)
      }, 500)
    } else {
      status.value = 'error'
      message.error(res.msg || '验证失败，请重试')
      setTimeout(() => {
        refreshCaptcha()
      }, 1000)
    }
  } catch (error) {
    status.value = 'error'
    message.error('验证出错')
    refreshCaptcha()
  }
}

onMounted(() => {
  refreshCaptcha()
})

// 暴露刷新方法给父组件
defineExpose({
  refresh: refreshCaptcha
})
</script>

<template>
  <div class="slide-captcha">
    <!-- 关闭按钮 -->
    <div class="close-btn" @click="$emit('close')">
      <CloseCircleOutlined />
    </div>

    <!-- 图片区域 -->
    <div class="captcha-img-wrapper" v-if="captchaData">
      <img :src="captchaData.backgroundImage" class="bg-img" alt="验证码背景" />
      <img 
        :src="captchaData.sliderImage" 
        class="slider-img" 
        :style="{ top: `${captchaData.sliderY}px`, left: `${sliderX}px` }"
        alt="滑块" 
      />
      <div class="refresh-btn" @click="refreshCaptcha">
        <ReloadOutlined :spin="loading" />
      </div>
    </div>
    <div v-else class="captcha-loading">
      加载中...
    </div>

    <!-- 滑动条区域 -->
    <div class="slider-track" :class="status">
      <div 
        class="slider-mask" 
        :style="{ width: `${sliderX}px` }"
      ></div>
      <div 
        class="slider-knob"
        :style="{ left: `${sliderX}px` }"
        @mousedown="handleDragStart"
        @touchstart.prevent="handleDragStart"
      >
        <span v-if="status === 'default'" class="arrow-icon">→</span>
        <CheckCircleOutlined v-else-if="status === 'success'" style="color: #52c41a" />
        <CloseCircleOutlined v-else-if="status === 'error'" style="color: #ff4d4f" />
      </div>
      <span class="track-text">{{ status === 'success' ? '验证通过' : '向右滑动填充拼图' }}</span>
    </div>

    <!-- 全局事件监听 -->
    <div 
      v-if="isDragging" 
      class="drag-mask"
      @mousemove="handleDragMove"
      @mouseup="handleDragEnd"
      @touchmove="handleDragMove"
      @touchend="handleDragEnd"
    ></div>
  </div>
</template>

<style scoped lang="less">
.slide-captcha {
  width: 340px;
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  position: relative;
  user-select: none;
}

.close-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  cursor: pointer;
  color: #999;
  z-index: 10;
  
  &:hover {
    color: #333;
  }
}

.captcha-img-wrapper {
  position: relative;
  width: 300px;
  height: 150px;
  margin: 0 auto 20px;
  background: #f5f5f5;
  border-radius: 2px;
  overflow: hidden;
  
  .bg-img {
    width: 100%;
    height: 100%;
    display: block;
  }
  
  .slider-img {
    position: absolute;
    width: 50px;
    height: 50px;
    z-index: 2;
  }
  
  .refresh-btn {
    position: absolute;
    top: 5px;
    right: 5px;
    cursor: pointer;
    color: #fff;
    font-size: 18px;
    z-index: 3;
    text-shadow: 0 0 2px rgba(0,0,0,0.5);
    
    &:hover {
      opacity: 0.8;
    }
  }
}

.captcha-loading {
  width: 300px;
  height: 150px;
  margin: 0 auto 20px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
}

.slider-track {
  position: relative;
  width: 300px;
  height: 40px;
  margin: 0 auto;
  background: #f7f9fa;
  border: 1px solid #e4e7eb;
  border-radius: 20px;
  text-align: center;
  line-height: 40px;
  
  &.success {
    background: #f6ffed;
    border-color: #b7eb8f;
  }
  
  &.error {
    background: #fff2f0;
    border-color: #ffccc7;
  }
  
  .track-text {
    color: #666;
    font-size: 14px;
  }
  
  .slider-mask {
    position: absolute;
    top: 0;
    left: 0;
    height: 100%;
    background: #e9f5fe;
    border-radius: 20px 0 0 20px;
    border: 1px solid #1890ff;
    border-right: none;
    box-sizing: border-box;
  }
  
  .slider-knob {
    position: absolute;
    top: 0;
    width: 50px;
    height: 38px; /* 40 - 2 border */
    background: #fff;
    box-shadow: 0 0 3px rgba(0,0,0,0.3);
    border-radius: 20px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background 0.2s;
    z-index: 5;
    
    &:hover {
      background: #1890ff;
      .arrow-icon {
        color: #fff;
      }
    }
    
    .arrow-icon {
      color: #666;
      font-weight: bold;
    }
  }
}

.drag-mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 9999;
  cursor: grabbing;
}
</style>
