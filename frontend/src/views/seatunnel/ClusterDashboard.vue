<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { seatunnelApi, type RunningJob } from '@/api/seatunnel'
import { message, Modal } from 'ant-design-vue'
import {
  ClusterOutlined,
  PlayCircleOutlined,
  DashboardOutlined,
  ReloadOutlined,
  CheckCircleOutlined,
  SyncOutlined,
  EyeOutlined,
  StopOutlined,
  WarningOutlined,
  DisconnectOutlined,
  WifiOutlined
} from '@ant-design/icons-vue'
import JobDetailDrawer from './JobDetailDrawer.vue'

const loading = ref(false)
const clusterOverview = ref<any>({})
const runningJobs = ref<RunningJob[]>([])
const refreshTimer = ref<any>(null)
const autoRefresh = ref(true)
const clusterConnected = ref(true)

// 任务详情抽屉
const isDrawerOpen = ref(false)
const selectedJobId = ref<string | null>(null)

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const [overviewRes, jobsRes] = await Promise.all([
      seatunnelApi.getOverview(),
      seatunnelApi.getRunningJobs()
    ])
    if (overviewRes.code === 200) {
      clusterOverview.value = overviewRes.data || {}
      // 检测集群连接状态
      clusterConnected.value = !overviewRes.data?.error
    }
    if (jobsRes.code === 200) {
      runningJobs.value = jobsRes.data || []
    }
  } catch (error) {
    console.error('获取 SeaTunnel 数据失败:', error)
    clusterConnected.value = false
    message.error('无法连接到 SeaTunnel 集群或获取状态失败')
  } finally {
    loading.value = false
  }
}

// 自动刷新
const startTimer = () => {
  if (refreshTimer.value) clearInterval(refreshTimer.value)
  if (autoRefresh.value) {
    refreshTimer.value = setInterval(() => {
      fetchData()
    }, 5000)
  }
}

const toggleAutoRefresh = () => {
  autoRefresh.value = !autoRefresh.value
  startTimer()
  message.success(autoRefresh.value ? '已开启 5s 自动刷新' : '已关闭自动刷新')
}

// 查看任务详情
const handleViewDetail = (record: RunningJob) => {
  selectedJobId.value = record.jobId
  isDrawerOpen.value = true
}

// 停止任务
const handleStopJob = (record: RunningJob) => {
  Modal.confirm({
    title: '确定要停止此 SeaTunnel 任务吗？',
    icon: null,
    content: `即将停止运行中的 Job [${record.jobName || record.jobId}]，该操作将向 SeaTunnel 集群发送停止指令。`,
    okText: '确认停止',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      try {
        const res = await seatunnelApi.stopJob(record.jobId)
        if (res.code === 200) {
          message.success('停止请求已发送，等待集群处理...')
          // 延迟刷新
          setTimeout(() => fetchData(), 2000)
        }
      } catch (error) {
        console.error('停止任务失败:', error)
      }
    }
  })
}

// 连接状态文字
const connectionStatusText = computed(() => {
  return clusterConnected.value ? '集群连接正常' : '集群连接异常'
})

onMounted(() => {
  fetchData()
  startTimer()
})

onUnmounted(() => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
  }
})

// 表格列定义
const columns = [
  {
    title: '任务 ID (Job ID)',
    dataIndex: 'jobId',
    key: 'jobId',
    ellipsis: true,
    width: 220
  },
  {
    title: '任务名称',
    dataIndex: 'jobName',
    key: 'jobName'
  },
  {
    title: '状态',
    dataIndex: 'jobStatus',
    key: 'jobStatus',
    width: 140
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
  <div class="seatunnel-dashboard">
    <!-- 页面标题 -->
    <div class="page-header flex-between">
      <div>
        <h1 class="page-title">
          <ClusterOutlined class="mr-8" />SeaTunnel 集群看板
        </h1>
        <p class="page-desc">实时监控 SeaTunnel 引擎状态及正在运行的数据同步任务</p>
      </div>
      <div class="actions">
        <!-- 集群连接状态指示器 -->
        <div :class="['connection-indicator', { connected: clusterConnected, disconnected: !clusterConnected }]">
          <WifiOutlined v-if="clusterConnected" />
          <DisconnectOutlined v-else />
          <span>{{ connectionStatusText }}</span>
        </div>
        <a-button
          type="default"
          :class="['mr-8', { 'active-btn': autoRefresh }]"
          @click="toggleAutoRefresh"
        >
          <template #icon><SyncOutlined :spin="autoRefresh && loading" /></template>
          {{ autoRefresh ? '自动刷新中 (5s)' : '开启自动刷新' }}
        </a-button>
        <a-button type="primary" :loading="loading" @click="fetchData">
          <template #icon><ReloadOutlined /></template>
          手动刷新
        </a-button>
      </div>
    </div>

    <!-- 数据指标看板 -->
    <div class="metrics-grid">
      <!-- 集群状态 -->
      <div class="page-card metric-card">
        <div class="metric-header">
          <span class="metric-title">集群状态</span>
          <div :class="['metric-icon', clusterConnected ? 'bg-success' : 'bg-error']">
            <CheckCircleOutlined v-if="clusterConnected" />
            <WarningOutlined v-else />
          </div>
        </div>
        <div class="metric-body">
          <h2 class="metric-value">
            {{ clusterConnected ? (clusterOverview.status || 'RUNNING') : 'OFFLINE' }}
          </h2>
          <p class="metric-footer">
            集群主节点: <span>{{ clusterOverview.masterNodeAddress || '127.0.0.1:5801' }}</span>
          </p>
        </div>
      </div>

      <!-- 活跃节点数量 -->
      <div class="page-card metric-card">
        <div class="metric-header">
          <span class="metric-title">活跃节点数</span>
          <div class="metric-icon bg-primary">
            <ClusterOutlined />
          </div>
        </div>
        <div class="metric-body">
          <h2 class="metric-value">
            {{ clusterOverview.nodeCount || clusterOverview.memberCount || 1 }} <span class="unit">Nodes</span>
          </h2>
          <p class="metric-footer text-ellipsis">
            节点列表: {{ clusterOverview.members ? clusterOverview.members.join(', ') : '127.0.0.1:5801' }}
          </p>
        </div>
      </div>

      <!-- 运行中任务 -->
      <div :class="['page-card metric-card', { 'animate-pulse': runningJobs.length > 0 }]">
        <div class="metric-header">
          <span class="metric-title">正在运行的任务</span>
          <div class="metric-icon bg-warning">
            <PlayCircleOutlined />
          </div>
        </div>
        <div class="metric-body">
          <h2 class="metric-value">
            {{ runningJobs.length }} <span class="unit">Jobs</span>
          </h2>
          <p class="metric-footer">
            已成功部署同步管道: <span>{{ runningJobs.length }}</span> 个
          </p>
        </div>
      </div>

      <!-- 物理资源负载 -->
      <div class="page-card metric-card">
        <div class="metric-header">
          <span class="metric-title">引擎资源负载</span>
          <div class="metric-icon bg-cta">
            <DashboardOutlined />
          </div>
        </div>
        <div class="metric-body">
          <div class="progress-box">
            <div class="progress-label">
              <span>CPU 使用率</span>
              <span>{{ clusterOverview.cpuUsage || '12%' }}</span>
            </div>
            <a-progress :percent="parseInt(clusterOverview.cpuUsage) || 12" size="small" :show-info="false" stroke-color="#F97316" />
          </div>
          <div class="progress-box mt-8">
            <div class="progress-label">
              <span>内存 使用率</span>
              <span>{{ clusterOverview.memoryUsage || '34%' }}</span>
            </div>
            <a-progress :percent="parseInt(clusterOverview.memoryUsage) || 34" size="small" :show-info="false" stroke-color="#2563EB" />
          </div>
        </div>
      </div>
    </div>

    <!-- 运行中任务列表 -->
    <div class="page-card table-card">
      <div class="table-toolbar">
        <h3><PlayCircleOutlined class="mr-8 text-primary" />正在运行的 SeaTunnel 任务 (Hazelcast Jobs)</h3>
        <a-tag color="blue">当前活动任务: {{ runningJobs.length }}</a-tag>
      </div>

      <a-table
        :columns="columns"
        :data-source="runningJobs"
        :loading="loading"
        :pagination="false"
        row-key="jobId"
        class="custom-table"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'jobId'">
            <code class="job-id-code">{{ record.jobId }}</code>
          </template>
          <template v-if="column.key === 'jobStatus'">
            <a-tag :color="
              (record.jobStatus || record.status) === 'RUNNING' ? 'processing' :
              (record.jobStatus || record.status) === 'FINISHED' ? 'success' :
              (record.jobStatus || record.status) === 'FAILED' ? 'error' :
              (record.jobStatus || record.status) === 'CANCELED' ? 'warning' : 'default'
            ">
              <template #icon><SyncOutlined :spin="(record.jobStatus || record.status) === 'RUNNING'" /></template>
              {{ record.jobStatus || record.status }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <div class="action-cell">
              <a-button type="link" size="small" @click="handleViewDetail(record as RunningJob)">
                <template #icon><EyeOutlined /></template>详情
              </a-button>
              <a-button
                type="link"
                size="small"
                danger
                @click="handleStopJob(record as RunningJob)"
                v-if="(record.jobStatus || record.status) === 'RUNNING'"
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
            <p class="empty-title">{{ clusterConnected ? '当前没有运行中的任务' : '无法连接 SeaTunnel 集群' }}</p>
            <p class="empty-desc">
              {{ clusterConnected
                ? '前往「同步配置」页面创建并执行数据同步任务'
                : '请检查 SeaTunnel 集群是否正常运行，以及网络连接状态' }}
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
.seatunnel-dashboard {
  perspective: 2000px;
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;

  .active-btn {
    border-color: var(--primary-color) !important;
    color: var(--primary-color) !important;
    background: rgba(37, 99, 235, 0.1) !important;
  }
}

/* 连接状态指示器 */
.connection-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.3s ease;

  &.connected {
    color: #10B981;
    background: rgba(16, 185, 129, 0.1);
    border: 1px solid rgba(16, 185, 129, 0.3);

    .anticon {
      animation: breathe 2s ease-in-out infinite;
    }
  }

  &.disconnected {
    color: #EF4444;
    background: rgba(239, 68, 68, 0.1);
    border: 1px solid rgba(239, 68, 68, 0.3);
    animation: shake 0.5s ease-in-out;
  }
}

@keyframes breathe {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-3px); }
  75% { transform: translateX(3px); }
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 24px;
  margin-bottom: 30px;
}

.metric-card {
  padding: 24px;
  min-height: 160px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;

  .metric-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .metric-title {
      font-size: 16px;
      font-weight: 600;
      color: var(--text-color-secondary);
    }

    .metric-icon {
      width: 36px;
      height: 36px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18px;
      color: #fff;
      box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);

      &.bg-success { background: linear-gradient(135deg, #10B981, #059669); }
      &.bg-primary { background: linear-gradient(135deg, #3B82F6, #2563EB); }
      &.bg-warning { background: linear-gradient(135deg, #F59E0B, #D97706); }
      &.bg-cta { background: linear-gradient(135deg, #F97316, #EA580C); }
      &.bg-error { background: linear-gradient(135deg, #EF4444, #DC2626); }
    }
  }

  .metric-body {
    margin-top: 16px;

    .metric-value {
      font-size: 28px;
      font-weight: 800;
      color: var(--heading-color);
      margin: 0;
      letter-spacing: -0.02em;

      .unit {
        font-size: 14px;
        font-weight: 500;
        color: var(--text-color-secondary);
        margin-left: 4px;
      }
    }

    .metric-footer {
      margin-top: 8px;
      margin-bottom: 0;
      font-size: 13px;
      color: var(--text-color-secondary);
      font-weight: 500;

      span {
        color: var(--heading-color);
        font-weight: 600;
      }
    }

    .progress-box {
      .progress-label {
        display: flex;
        justify-content: space-between;
        font-size: 12px;
        font-weight: 600;
        color: var(--text-color-secondary);
        margin-bottom: 4px;
      }
    }
  }
}

.mt-8 {
  margin-top: 8px;
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

.custom-table {
  margin-top: 12px;
}

.action-cell {
  display: flex;
  gap: 4px;
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
  }
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.02); box-shadow: 0 10px 25px rgba(245, 158, 11, 0.15); }
  100% { transform: scale(1); }
}

.animate-pulse {
  animation: pulse 3s infinite ease-in-out;
}
</style>
