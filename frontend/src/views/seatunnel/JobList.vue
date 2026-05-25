<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { seatunnelApi, type RunningJob } from '@/api/seatunnel'
import { message, Modal } from 'ant-design-vue'
import {
  UnorderedListOutlined,
  ReloadOutlined,
  SyncOutlined,
  EyeOutlined,
  StopOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  PauseCircleOutlined,
  ClusterOutlined,
  ThunderboltOutlined
} from '@ant-design/icons-vue'
import JobDetailDrawer from './JobDetailDrawer.vue'

const loading = ref(false)
const runningJobs = ref<RunningJob[]>([])
const refreshTimer = ref<any>(null)
const autoRefresh = ref(true)
const refreshInterval = ref(5)

// 抽屉
const isDrawerOpen = ref(false)
const selectedJobId = ref<string | null>(null)

// 获取列表
const fetchList = async () => {
  loading.value = true
  try {
    const res = await seatunnelApi.getRunningJobs()
    if (res.code === 200) {
      runningJobs.value = res.data || []
    }
  } catch (error) {
    console.error('获取 SeaTunnel 任务列表失败:', error)
    message.error('获取任务列表失败，请检查集群连接')
  } finally {
    loading.value = false
  }
}

// 自动刷新
const startTimer = () => {
  if (refreshTimer.value) clearInterval(refreshTimer.value)
  if (autoRefresh.value) {
    refreshTimer.value = setInterval(() => {
      fetchList()
    }, refreshInterval.value * 1000)
  }
}

const toggleAutoRefresh = () => {
  autoRefresh.value = !autoRefresh.value
  startTimer()
  message.success(autoRefresh.value ? `已开启 ${refreshInterval.value}s 自动刷新` : '已关闭自动刷新')
}

// 查看详情
const handleViewDetail = (record: RunningJob) => {
  selectedJobId.value = record.jobId
  isDrawerOpen.value = true
}

// 停止任务
const handleStopJob = (record: RunningJob) => {
  Modal.confirm({
    title: '确定要停止此 SeaTunnel 任务吗？',
    icon: null,
    content: `即将停止 Job [${record.jobName || record.jobId}]，数据同步将中断。`,
    okText: '确认停止',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      try {
        const res = await seatunnelApi.stopJob(record.jobId)
        if (res.code === 200) {
          message.success('停止请求已发送')
          setTimeout(() => fetchList(), 2000)
        }
      } catch (error) {
        console.error('停止任务失败:', error)
      }
    }
  })
}

// 获取状态相关信息
const getStatusColor = (status: string) => {
  const map: Record<string, string> = {
    RUNNING: 'processing',
    FINISHED: 'success',
    FAILED: 'error',
    CANCELED: 'warning',
    CANCELLING: 'warning'
  }
  return map[status] || 'default'
}

const getStatusIcon = (status: string) => {
  const map: Record<string, any> = {
    RUNNING: SyncOutlined,
    FINISHED: CheckCircleOutlined,
    FAILED: CloseCircleOutlined,
    CANCELED: PauseCircleOutlined,
    CANCELLING: SyncOutlined
  }
  return map[status] || SyncOutlined
}

onMounted(() => {
  fetchList()
  startTimer()
})

onUnmounted(() => {
  if (refreshTimer.value) clearInterval(refreshTimer.value)
})

// 表格列定义
const columns = [
  {
    title: 'Job ID',
    dataIndex: 'jobId',
    key: 'jobId',
    width: 220,
    ellipsis: true
  },
  {
    title: '任务名称',
    dataIndex: 'jobName',
    key: 'jobName',
    ellipsis: true
  },
  {
    title: '任务状态',
    key: 'jobStatus',
    width: 150
  },
  {
    title: '提交时间',
    dataIndex: 'submitTime',
    key: 'submitTime',
    width: 200
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right' as const
  }
]
</script>

<template>
  <div class="seatunnel-jobs">
    <!-- 页面标题 -->
    <div class="page-header flex-between">
      <div>
        <h1 class="page-title">
          <UnorderedListOutlined class="mr-8" />SeaTunnel 任务管理
        </h1>
        <p class="page-desc">查看、监控和管理 SeaTunnel 引擎集群上运行的所有数据同步任务</p>
      </div>
      <div class="actions">
        <a-button
          type="default"
          :class="['mr-8', { 'active-btn': autoRefresh }]"
          @click="toggleAutoRefresh"
        >
          <template #icon><SyncOutlined :spin="autoRefresh && loading" /></template>
          {{ autoRefresh ? `自动刷新 (${refreshInterval}s)` : '开启自动刷新' }}
        </a-button>
        <a-button type="primary" :loading="loading" @click="fetchList">
          <template #icon><ReloadOutlined /></template>
          刷新列表
        </a-button>
      </div>
    </div>

    <!-- 统计指标条 -->
    <div class="stats-bar">
      <div class="stat-item">
        <ThunderboltOutlined class="stat-icon running" />
        <div class="stat-content">
          <span class="stat-number">{{ runningJobs.filter(j => (j.jobStatus || j.status) === 'RUNNING').length }}</span>
          <span class="stat-label">运行中</span>
        </div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <ClusterOutlined class="stat-icon total" />
        <div class="stat-content">
          <span class="stat-number">{{ runningJobs.length }}</span>
          <span class="stat-label">全部任务</span>
        </div>
      </div>
    </div>

    <!-- 任务列表 -->
    <div class="page-card table-card">
      <a-table
        :columns="columns"
        :data-source="runningJobs"
        :loading="loading"
        :pagination="false"
        row-key="jobId"
        class="custom-table"
      >
        <template #bodyCell="{ column, record }">
          <!-- Job ID -->
          <template v-if="column.key === 'jobId'">
            <code class="job-id-code">{{ record.jobId }}</code>
          </template>

          <!-- 任务名称 -->
          <template v-if="column.key === 'jobName'">
            <div class="job-name-cell">
              <span class="name-text">{{ record.jobName || '--' }}</span>
            </div>
          </template>

          <!-- 状态 -->
          <template v-if="column.key === 'jobStatus'">
            <a-tag :color="getStatusColor(record.jobStatus || record.status)">
              <template #icon>
                <component
                  :is="getStatusIcon(record.jobStatus || record.status)"
                  :spin="(record.jobStatus || record.status) === 'RUNNING' || (record.jobStatus || record.status) === 'CANCELLING'"
                />
              </template>
              {{ record.jobStatus || record.status }}
            </a-tag>
          </template>

          <!-- 操作 -->
          <template v-if="column.key === 'action'">
            <div class="action-cell">
              <a-button type="link" size="small" @click="handleViewDetail(record as RunningJob)">
                <template #icon><EyeOutlined /></template>详情
              </a-button>
              <a-button
                type="link"
                size="small"
                danger
                v-if="(record.jobStatus || record.status) === 'RUNNING'"
                @click="handleStopJob(record as RunningJob)"
              >
                <template #icon><StopOutlined /></template>停止
              </a-button>
            </div>
          </template>
        </template>

        <!-- 空状态 -->
        <template #emptyText>
          <div class="empty-state">
            <ClusterOutlined class="empty-icon" />
            <p class="empty-title">暂无 SeaTunnel 任务</p>
            <p class="empty-desc">
              当前集群没有运行中的任务，前往「同步配置」页面创建并执行数据同步规则
            </p>
          </div>
        </template>
      </a-table>
    </div>

    <!-- 任务详情抽屉 -->
    <JobDetailDrawer
      v-model:open="isDrawerOpen"
      :job-id="selectedJobId"
    />
  </div>
</template>

<style scoped lang="less">
.seatunnel-jobs {
  perspective: 2000px;
}

.actions {
  display: flex;
  align-items: center;

  .active-btn {
    border-color: var(--primary-color) !important;
    color: var(--primary-color) !important;
    background: rgba(37, 99, 235, 0.1) !important;
  }
}

/* 统计指标条 */
.stats-bar {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 16px 28px;
  border-radius: 16px;
  background: var(--glass-bg);
  backdrop-filter: blur(20px) saturate(180%);
  border-top: var(--glass-border);
  border-left: var(--glass-border);
  border-right: var(--glass-border-right);
  border-bottom: var(--glass-border-bottom);
  box-shadow: var(--3d-shadow-light);
  margin-bottom: 24px;

  .stat-item {
    display: flex;
    align-items: center;
    gap: 12px;

    .stat-icon {
      font-size: 24px;

      &.running {
        color: #3B82F6;
        animation: pulseIcon 2s ease-in-out infinite;
      }

      &.total {
        color: #F97316;
      }
    }

    .stat-content {
      display: flex;
      flex-direction: column;

      .stat-number {
        font-size: 22px;
        font-weight: 800;
        color: var(--heading-color);
        letter-spacing: -0.02em;
        line-height: 1.1;
      }

      .stat-label {
        font-size: 12px;
        font-weight: 600;
        color: var(--text-color-secondary);
        margin-top: 2px;
      }
    }
  }

  .stat-divider {
    width: 1px;
    height: 36px;
    background: rgba(148, 163, 184, 0.2);
  }
}

@keyframes pulseIcon {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(1.1); }
}

.table-card {
  padding: 24px;
}

.job-id-code {
  font-family: SFMono-Regular, Consolas, Liberation Mono, Menlo, monospace;
  background: rgba(148, 163, 184, 0.15);
  padding: 3px 8px;
  border-radius: 6px;
  font-size: 13px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  color: var(--primary-color);
}

.job-name-cell {
  .name-text {
    font-weight: 600;
    color: var(--heading-color);
  }
}

.action-cell {
  display: flex;
  gap: 4px;
}

.custom-table {
  margin-top: 0;
}

/* 空状态 */
.empty-state {
  padding: 48px 24px;
  text-align: center;

  .empty-icon {
    font-size: 48px;
    color: var(--text-color-secondary);
    opacity: 0.4;
    margin-bottom: 16px;
  }

  .empty-title {
    font-size: 16px;
    font-weight: 700;
    color: var(--heading-color);
    margin: 0 0 8px 0;
  }

  .empty-desc {
    font-size: 14px;
    color: var(--text-color-secondary);
    margin: 0;
    max-width: 400px;
    margin-left: auto;
    margin-right: auto;
  }
}
</style>
