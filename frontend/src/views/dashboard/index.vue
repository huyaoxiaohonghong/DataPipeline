<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  UserOutlined,
  TeamOutlined,
  FileTextOutlined,
  RiseOutlined,
  FallOutlined
} from '@ant-design/icons-vue'

// 统计数据
const statistics = ref([
  {
    title: '用户总数',
    value: 1256,
    icon: UserOutlined,
    color: '#1890ff',
    bgColor: 'rgba(24, 144, 255, 0.1)',
    trend: 12.5,
    trendUp: true
  },
  {
    title: '活跃用户',
    value: 856,
    icon: TeamOutlined,
    color: '#52c41a',
    bgColor: 'rgba(82, 196, 26, 0.1)',
    trend: 8.2,
    trendUp: true
  },
  {
    title: '今日新增',
    value: 28,
    icon: RiseOutlined,
    color: '#722ed1',
    bgColor: 'rgba(114, 46, 209, 0.1)',
    trend: 5.3,
    trendUp: true
  },
  {
    title: '待处理',
    value: 12,
    icon: FileTextOutlined,
    color: '#fa8c16',
    bgColor: 'rgba(250, 140, 22, 0.1)',
    trend: 2.1,
    trendUp: false
  }
])

// 最近活动
const activities = ref([
  { user: 'admin', action: '登录系统', time: '2分钟前', type: 'login' },
  { user: 'test', action: '修改了个人信息', time: '10分钟前', type: 'update' },
  { user: 'admin', action: '创建了新用户', time: '30分钟前', type: 'create' },
  { user: 'user1', action: '重置了密码', time: '1小时前', type: 'update' },
  { user: 'admin', action: '删除了用户', time: '2小时前', type: 'delete' }
])

// 快捷操作
const shortcuts = [
  { title: '用户管理', icon: UserOutlined, path: '/users', color: '#1890ff' },
  { title: '系统设置', icon: FileTextOutlined, path: '/settings', color: '#52c41a' },
  { title: '数据统计', icon: RiseOutlined, path: '/statistics', color: '#722ed1' },
  { title: '操作日志', icon: FileTextOutlined, path: '/logs', color: '#fa8c16' }
]

const loading = ref(true)

onMounted(() => {
  setTimeout(() => {
    loading.value = false
  }, 500)
})
</script>

<template>
  <div class="dashboard">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">仪表盘</h1>
      <p class="page-desc">欢迎回来，这是您的工作台概览</p>
    </div>

    <!-- 统计卡片 -->
    <a-row :gutter="16" class="stat-row">
      <a-col :xs="24" :sm="12" :lg="6" v-for="stat in statistics" :key="stat.title">
        <a-card :bordered="false" class="stat-card" :loading="loading">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">{{ stat.title }}</div>
              <div class="stat-value">{{ stat.value.toLocaleString() }}</div>
              <div class="stat-trend" :class="stat.trendUp ? 'up' : 'down'">
                <RiseOutlined v-if="stat.trendUp" />
                <FallOutlined v-else />
                <span>{{ stat.trend }}%</span>
                <span class="trend-text">较上周</span>
              </div>
            </div>
            <div class="stat-icon" :style="{ backgroundColor: stat.bgColor }">
              <component :is="stat.icon" :style="{ color: stat.color }" />
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16">
      <!-- 快捷操作 -->
      <a-col :xs="24" :lg="16">
        <a-card title="快捷操作" :bordered="false" class="shortcuts-card">
          <a-row :gutter="16">
            <a-col :span="6" v-for="item in shortcuts" :key="item.title">
              <router-link :to="item.path" class="shortcut-item">
                <div class="shortcut-icon" :style="{ backgroundColor: item.color }">
                  <component :is="item.icon" />
                </div>
                <span class="shortcut-title">{{ item.title }}</span>
              </router-link>
            </a-col>
          </a-row>
        </a-card>

        <!-- 图表占位 -->
        <a-card title="数据趋势" :bordered="false" class="chart-card">
          <div class="chart-placeholder">
            <a-empty description="暂无数据" />
          </div>
        </a-card>
      </a-col>

      <!-- 最近活动 -->
      <a-col :xs="24" :lg="8">
        <a-card title="最近活动" :bordered="false" class="activity-card">
          <a-timeline>
            <a-timeline-item v-for="(item, index) in activities" :key="index">
              <template #dot>
                <a-avatar :size="24" class="activity-avatar">
                  {{ item.user.charAt(0).toUpperCase() }}
                </a-avatar>
              </template>
              <div class="activity-content">
                <div class="activity-info">
                  <span class="activity-user">{{ item.user }}</span>
                  <span class="activity-action">{{ item.action }}</span>
                </div>
                <div class="activity-time">{{ item.time }}</div>
              </div>
            </a-timeline-item>
          </a-timeline>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<style scoped lang="less">
.dashboard {
  .page-header {
    margin-bottom: 24px;
    
    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: rgba(0, 0, 0, 0.85);
      margin: 0 0 8px 0;
    }
    
    .page-desc {
      color: rgba(0, 0, 0, 0.45);
      margin: 0;
    }
  }
  
  .stat-row {
    margin-bottom: 16px;
  }
  
  .stat-card {
    margin-bottom: 16px;
    border-radius: 8px;
    
    .stat-content {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      
      .stat-info {
        .stat-title {
          font-size: 14px;
          color: rgba(0, 0, 0, 0.45);
          margin-bottom: 8px;
        }
        
        .stat-value {
          font-size: 28px;
          font-weight: 600;
          color: rgba(0, 0, 0, 0.85);
          line-height: 1.2;
        }
        
        .stat-trend {
          display: flex;
          align-items: center;
          gap: 4px;
          margin-top: 8px;
          font-size: 12px;
          
          &.up {
            color: #52c41a;
          }
          
          &.down {
            color: #ff4d4f;
          }
          
          .trend-text {
            color: rgba(0, 0, 0, 0.45);
            margin-left: 4px;
          }
        }
      }
      
      .stat-icon {
        width: 48px;
        height: 48px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;
      }
    }
  }
  
  .shortcuts-card {
    margin-bottom: 16px;
    border-radius: 8px;
    
    .shortcut-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 16px;
      border-radius: 8px;
      transition: all 0.3s;
      cursor: pointer;
      
      &:hover {
        background: rgba(0, 0, 0, 0.02);
      }
      
      .shortcut-icon {
        width: 48px;
        height: 48px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 20px;
        color: #fff;
        margin-bottom: 8px;
      }
      
      .shortcut-title {
        font-size: 14px;
        color: rgba(0, 0, 0, 0.65);
      }
    }
  }
  
  .chart-card {
    margin-bottom: 16px;
    border-radius: 8px;
    
    .chart-placeholder {
      height: 300px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
  
  .activity-card {
    border-radius: 8px;
    
    .activity-avatar {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      font-size: 12px;
    }
    
    .activity-content {
      padding-left: 8px;
      
      .activity-info {
        .activity-user {
          font-weight: 500;
          color: rgba(0, 0, 0, 0.85);
        }
        
        .activity-action {
          color: rgba(0, 0, 0, 0.65);
          margin-left: 4px;
        }
      }
      
      .activity-time {
        font-size: 12px;
        color: rgba(0, 0, 0, 0.45);
        margin-top: 4px;
      }
    }
  }
}
</style>
