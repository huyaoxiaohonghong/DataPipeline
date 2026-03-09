<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import {
  DashboardOutlined,
  UserOutlined,
  TeamOutlined,
  SafetyOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  LogoutOutlined,
  SettingOutlined,
  BellOutlined,
  SearchOutlined,
  FullscreenOutlined,
  FullscreenExitOutlined,
  SkinOutlined,
  FileProtectOutlined,
  LoginOutlined
} from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

// 全屏状态
const isFullscreen = ref(false)

// 主题状态
const isDarkTheme = ref(false)
const toggleTheme = () => {
  isDarkTheme.value = !isDarkTheme.value
  document.documentElement.setAttribute('data-theme', isDarkTheme.value ? 'dark' : 'light')
}

// 当前选中的菜单
const selectedKeys = computed(() => [route.name as string])

// 展开的菜单
const openKeys = ref<string[]>([])

// 菜单项配置
const menuItems = [
  {
    key: 'Dashboard',
    icon: DashboardOutlined,
    label: '仪表盘',
    path: '/dashboard'
  },
  {
    key: 'UserList',
    icon: UserOutlined,
    label: '用户管理',
    path: '/users'
  },
  {
    key: 'RoleList',
    icon: TeamOutlined,
    label: '角色管理',
    path: '/roles'
  },
  {
    key: 'PermissionList',
    icon: SafetyOutlined,
    label: '权限管理',
    path: '/permissions'
  },
  {
    key: 'LoginLogList',
    icon: LoginOutlined,
    label: '登录日志',
    path: '/logs/login'
  },
  {
    key: 'OperationLogList',
    icon: FileProtectOutlined,
    label: '操作日志',
    path: '/logs/operation'
  }
]

// 面包屑
const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta?.title)
  const result = [{ title: '首页', path: '/' }]
  
  matched.forEach(item => {
    if (item.meta?.title) {
      result.push({
        title: item.meta.title as string,
        path: item.path
      })
    }
  })
  
  return result
})

// 菜单点击
const handleMenuClick = ({ key }: { key: any }) => {
  const item = menuItems.find(m => m.key === key)
  if (item) {
    router.push(item.path)
  }
}

// 退出登录
const handleLogout = () => {
  userStore.logout()
}

// 切换全屏
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    document.exitFullscreen()
    isFullscreen.value = false
  }
}

// 监听全屏变化
document.addEventListener('fullscreenchange', () => {
  isFullscreen.value = !!document.fullscreenElement
})
</script>

<template>
  <a-layout class="layout">
    <!-- 侧边栏 -->
    <a-layout-sider
      v-model:collapsed="appStore.collapsed"
      :trigger="null"
      collapsible
      :width="220"
      :collapsed-width="64"
      class="sider"
      theme="dark"
    >
      <!-- Logo -->
      <div class="logo">
        <img src="/vite.svg" alt="Logo" class="logo-icon" />
        <transition name="fade">
          <span v-if="!appStore.collapsed" class="logo-text">DataPipeline</span>
        </transition>
      </div>

      <!-- 菜单 -->
      <a-menu
        mode="inline"
        theme="dark"
        :selected-keys="selectedKeys"
        :open-keys="openKeys"
        @click="handleMenuClick"
        @update:open-keys="(keys: any[]) => openKeys = keys as string[]"
      >
        <a-menu-item v-for="item in menuItems" :key="item.key">
          <template #icon>
            <component :is="item.icon" />
          </template>
          <span>{{ item.label }}</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>

    <a-layout class="main-layout">
      <!-- 顶部导航 -->
      <a-layout-header class="header">
        <div class="header-left">
          <!-- 折叠按钮 -->
          <div class="trigger" @click="appStore.toggleCollapsed">
            <MenuUnfoldOutlined v-if="appStore.collapsed" />
            <MenuFoldOutlined v-else />
          </div>

          <!-- 面包屑 -->
          <a-breadcrumb class="breadcrumb">
            <a-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="item.path">
              <router-link v-if="index < breadcrumbs.length - 1" :to="item.path">
                {{ item.title }}
              </router-link>
              <span v-else>{{ item.title }}</span>
            </a-breadcrumb-item>
          </a-breadcrumb>
        </div>

        <div class="header-right">
          <!-- 搜索 -->
          <div class="header-action">
            <SearchOutlined />
          </div>

          <!-- 主题切换 -->
          <div class="header-action" @click="toggleTheme">
            <SkinOutlined />
          </div>

          <!-- 全屏 -->
          <div class="header-action" @click="toggleFullscreen">
            <FullscreenExitOutlined v-if="isFullscreen" />
            <FullscreenOutlined v-else />
          </div>

          <!-- 通知 -->
          <a-badge :count="5" class="header-action">
            <BellOutlined />
          </a-badge>

          <!-- 用户信息 -->
          <a-dropdown placement="bottomRight">
            <div class="user-info">
              <a-avatar :size="28" class="user-avatar">
                {{ userStore.username?.charAt(0)?.toUpperCase() || 'U' }}
              </a-avatar>
              <span class="user-name">{{ userStore.username || '未登录' }}</span>
            </div>
            <template #overlay>
              <a-menu>
                <a-menu-item key="profile">
                  <UserOutlined />
                  <span class="ml-8">个人中心</span>
                </a-menu-item>
                <a-menu-item key="settings">
                  <SettingOutlined />
                  <span class="ml-8">系统设置</span>
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout" @click="handleLogout">
                  <LogoutOutlined />
                  <span class="ml-8">退出登录</span>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <!-- 内容区域 -->
      <a-layout-content class="content">
        <router-view v-slot="{ Component }">
          <transition name="slide-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </a-layout-content>

      <!-- 底部 -->
      <a-layout-footer class="footer">
        DataPipeline ©2024 Created by Antigravity Team
      </a-layout-footer>
    </a-layout>
  </a-layout>
</template>

<style scoped lang="less">
.layout {
  min-height: 100vh;
  background: transparent;
  perspective: 2500px;
}

.sider {
  position: fixed;
  left: 20px;
  top: 20px;
  bottom: 20px;
  z-index: 100;
  overflow: auto;
  border-radius: 28px;
  
  /* 3D Glass Sider */
  background: var(--layout-sider-bg);
  backdrop-filter: blur(40px) saturate(200%);
  -webkit-backdrop-filter: blur(40px) saturate(200%);
  border-top: var(--glass-border);
  border-left: var(--glass-border);
  border-right: var(--glass-border-right);
  border-bottom: var(--glass-border-bottom);
  
  /* Hyperrealistic shadow layers */
  box-shadow: var(--3d-shadow-light);
    
  transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  transform-style: preserve-3d;
  
  &:hover {
    transform: translateZ(10px) rotateY(1deg);
  }
  
  .logo {
    height: 80px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 16px;
    background: transparent;
    overflow: hidden;
    margin-bottom: 12px;
    border-bottom: 1px solid rgba(148, 163, 184, 0.1);
    position: relative;
    
    /* 玻璃质感分割线 */
    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 10%;
      right: 10%;
      height: 1px;
      background: linear-gradient(90deg, transparent, rgba(255,255,255,0.8), transparent);
    }
    
    .logo-icon {
      width: 40px;
      height: 40px;
      flex-shrink: 0;
      filter: drop-shadow(2px 4px 6px rgba(37, 99, 235, 0.4)) drop-shadow(-1px -1px 2px rgba(255, 255, 255, 0.8));
      transform-style: preserve-3d;
      animation: float3D 6s ease-in-out infinite;
    }
    
    .logo-text {
      margin-left: 14px;
      font-size: 22px;
      font-weight: 800;
      letter-spacing: -0.03em;
      color: var(--layout-logo-text);
      text-shadow: var(--layout-logo-shadow);
      white-space: nowrap;
      transform: translateZ(5px);
    }
  }
  
  :deep(.ant-menu) {
    background: transparent;
    border-right: none;
    padding: 0 16px;
  }
  
  :deep(.ant-menu-item) {
    border-radius: 16px;
    margin-bottom: 12px;
    transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    background: transparent;
    color: var(--layout-icon);
    font-weight: 500;
    transform-style: preserve-3d;
    position: relative;
    
    /* Neumorphic/3D Idle state */
    box-shadow: var(--3d-shadow-light);
      
    &.ant-menu-item-selected {
      background: rgba(37, 99, 235, 0.15);
      color: var(--primary-color);
      /* Inset shadow for pushed effect */
      box-shadow: 
        inset 4px 4px 8px rgba(0, 0, 0, 0.2),
        inset -4px -4px 8px rgba(255, 255, 255, 0.1);
      border: var(--glass-border);
      font-weight: 600;
    }
    
    &:not(.ant-menu-item-selected):hover {
      background: var(--layout-item-hover);
      transform: translateY(-2px) translateZ(8px);
      box-shadow: var(--3d-shadow-hover);
      color: var(--primary-color);
    }
    
    .anticon {
      font-size: 18px;
    }
  }
}

.main-layout {
  margin-left: 260px;
  transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  background: transparent;
}

.ant-layout-sider-collapsed + .main-layout {
  margin-left: 100px;
}

.header {
  position: sticky;
  top: 20px;
  z-index: 99;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 72px;
  padding: 0 28px;
  margin: 0 28px;
  border-radius: 24px;
  
  /* 3D Glass Header */
  background: var(--layout-header-bg);
  backdrop-filter: blur(32px) saturate(200%);
  -webkit-backdrop-filter: blur(32px) saturate(200%);
  border-top: var(--glass-border);
  border-left: var(--glass-border);
  border-bottom: var(--glass-border-bottom);
  
  box-shadow: var(--glass-shadow);
    
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  transform-style: preserve-3d;
  
  &:hover {
    transform: translateZ(5px);
  }
  
  .header-left {
    display: flex;
    align-items: center;
    
    .trigger {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 44px;
      height: 44px;
      border-radius: 14px;
      font-size: 20px;
      cursor: pointer;
      color: var(--layout-icon);
      background: var(--layout-btn-bg);
      border: var(--glass-border);
      /* 3D Button shadow */
      box-shadow: var(--3d-shadow-light);
      transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
      
      &:hover {
        background: var(--glass-bg);
        color: var(--primary-color);
        transform: translateY(-3px) translateZ(10px);
        box-shadow: var(--3d-shadow-hover);
      }
      
      &:active {
        transform: translateY(1px) scale(0.95);
        box-shadow: 
          inset 3px 3px 6px rgba(148, 163, 184, 0.2),
          inset -3px -3px 6px rgba(255, 255, 255, 0.9);
      }
    }
    
    .breadcrumb {
      margin-left: 20px;
      background: rgba(128, 128, 128, 0.1);
      padding: 8px 20px;
      border-radius: 14px;
      border: var(--glass-border);
      box-shadow: var(--3d-shadow-light);
      transform: translateZ(2px);
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .header-action {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 44px;
      height: 44px;
      border-radius: 14px;
      font-size: 20px;
      cursor: pointer;
      color: var(--layout-icon);
      background: var(--layout-btn-bg);
      border: var(--glass-border);
      box-shadow: var(--3d-shadow-light);
      transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
      
      &:hover {
        background: var(--glass-bg);
        color: var(--primary-color);
        transform: translateY(-3px) translateZ(10px);
        box-shadow: var(--3d-shadow-hover);
      }
      
      &:active {
        transform: translateY(1px) scale(0.95);
        box-shadow: inset 3px 3px 6px rgba(148, 163, 184, 0.2), inset -3px -3px 6px rgba(255, 255, 255, 0.9);
      }
    }
    
    .user-info {
      display: flex;
      align-items: center;
      padding: 6px 20px 6px 8px;
      height: 50px;
      cursor: pointer;
      border-radius: 25px;
      background: var(--layout-user-bg);
      border-top: var(--glass-border);
      border-left: var(--glass-border);
      box-shadow: var(--3d-shadow-light);
      transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
      transform-style: preserve-3d;
      
      &:hover {
        background: var(--glass-bg);
        transform: translateY(-3px) rotateX(5deg);
        box-shadow: var(--3d-shadow-hover);
      }
      
      .user-avatar {
        background: linear-gradient(135deg, #2563EB 0%, #3B82F6 100%);
        border: 2px solid #ffffff;
        box-shadow: 0 4px 10px rgba(37, 99, 235, 0.4);
        transform: translateZ(10px);
      }
      
      .user-name {
        margin-left: 14px;
        font-size: 15px;
        font-weight: 700;
        color: var(--layout-logo-text);
        text-shadow: none;
        transform: translateZ(5px);
      }
    }
  }
}

.content {
  margin: 36px 28px;
  min-height: calc(100vh - 72px - 36px - 70px - 48px);
  position: relative;
  perspective: 2500px;
}

.footer {
  text-align: center;
  color: var(--footer-text);
  background: transparent;
  font-size: 14px;
  font-weight: 600;
  text-shadow: var(--footer-text-shadow);
  letter-spacing: 0.05em;
  padding: 24px;
}

@keyframes float3D {
  0% { transform: translateY(0px) rotateY(0deg); }
  50% { transform: translateY(-8px) rotateY(10deg); }
  100% { transform: translateY(0px) rotateY(0deg); }
}

// 动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-fade-enter-active {
  transition: all 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

.slide-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateY(30px) scale(0.98);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
</style>
