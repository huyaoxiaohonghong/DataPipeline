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
  FullscreenExitOutlined
} from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

// 全屏状态
const isFullscreen = ref(false)

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
}

.sider {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
  overflow: auto;
  
  .logo {
    height: 56px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 16px;
    background: rgba(255, 255, 255, 0.05);
    overflow: hidden;
    
    .logo-icon {
      width: 32px;
      height: 32px;
      flex-shrink: 0;
    }
    
    .logo-text {
      margin-left: 12px;
      font-size: 18px;
      font-weight: 600;
      color: #fff;
      white-space: nowrap;
    }
  }
  
  :deep(.ant-menu) {
    border-right: none;
  }
}

.main-layout {
  margin-left: 220px;
  transition: margin-left 0.2s;
}

.ant-layout-sider-collapsed + .main-layout {
  margin-left: 64px;
}

.header {
  position: sticky;
  top: 0;
  z-index: 99;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 24px 0 0;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  
  .header-left {
    display: flex;
    align-items: center;
    
    .trigger {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 56px;
      height: 56px;
      font-size: 18px;
      cursor: pointer;
      transition: background 0.3s;
      
      &:hover {
        background: rgba(0, 0, 0, 0.025);
      }
    }
    
    .breadcrumb {
      margin-left: 8px;
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: 4px;
    
    .header-action {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      font-size: 16px;
      cursor: pointer;
      border-radius: 4px;
      transition: background 0.3s;
      
      &:hover {
        background: rgba(0, 0, 0, 0.025);
      }
    }
    
    .user-info {
      display: flex;
      align-items: center;
      padding: 0 12px;
      height: 40px;
      cursor: pointer;
      border-radius: 4px;
      transition: background 0.3s;
      
      &:hover {
        background: rgba(0, 0, 0, 0.025);
      }
      
      .user-avatar {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }
      
      .user-name {
        margin-left: 8px;
        font-size: 14px;
        color: rgba(0, 0, 0, 0.65);
      }
    }
  }
}

.content {
  margin: 24px;
  min-height: calc(100vh - 56px - 70px - 48px);
}

.footer {
  text-align: center;
  color: rgba(0, 0, 0, 0.45);
  background: transparent;
}

// 动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.15s ease-in;
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.slide-fade-leave-to {
  opacity: 0;
}
</style>
