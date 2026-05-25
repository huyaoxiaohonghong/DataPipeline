<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { syncApi, type SyncTaskVO, type SyncTaskLogVO } from '@/api/sync'
import { message, Modal } from 'ant-design-vue'
import {
  HistoryOutlined,
  ReloadOutlined,
  StopOutlined,
  CodeOutlined,
  SyncOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  LoadingOutlined,
  DoubleRightOutlined,
  CaretRightOutlined
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'

const loading = ref(false)
const listData = ref<SyncTaskVO[]>([])

// 搜索过滤
const queryParams = ref({
  pageNumber: 1,
  pageSize: 10,
  status: undefined as string | undefined,
  configId: undefined as number | undefined
})
const total = ref(0)

// 控制台终端日志模态框
const isLogModalOpen = ref(false)
const activeTaskId = ref<number | null>(null)
const activeTaskName = ref('')
const logs = ref<SyncTaskLogVO[]>([])
const loadingLogs = ref(false)
const terminalRef = ref<HTMLDivElement | null>(null)
const logPollTimer = ref<any>(null)
const logAutoPoll = ref(true)

// 获取任务列表
const fetchList = async () => {
  loading.value = true
  try {
    const res = await syncApi.listTasks(queryParams.value)
    if (res.code === 200) {
      listData.value = res.data.records || []
      total.value = res.data.totalRow || 0
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 刷新单个任务状态 (拉取 SeaTunnel 最新的状态)
const handleRefreshTask = async (record: any) => {
  try {
    const res = await syncApi.refreshTask(record.id)
    if (res.code === 200) {
      message.success('SeaTunnel 任务状态刷新成功')
      // 更新本地数组
      const index = listData.value.findIndex(item => item.id === record.id)
      if (index !== -1) {
        listData.value[index] = res.data
      }
    }
  } catch (error) {
    console.error(error)
  }
}

// 取消停止运行中的任务
const handleCancelTask = (record: any) => {
  Modal.confirm({
    title: '确定要停止运行此 SeaTunnel 任务吗？',
    icon: null,
    content: `这将停止提交至集群的 Job: [${record.seatunnelJobId || '未生成ID'}]。`,
    okText: '强制停止',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      try {
        await syncApi.cancelTask(record.id)
        message.success('终止请求已发送')
        // 自动刷新
        setTimeout(() => fetchList(), 1500)
      } catch (error) {
        console.error(error)
      }
    }
  })
}

// 过滤和重置
const handleSearch = () => {
  queryParams.value.pageNumber = 1
  fetchList()
}

const handleReset = () => {
  queryParams.value.status = undefined
  queryParams.value.pageNumber = 1
  fetchList()
}

const onPageChange = (page: number, pageSize: number) => {
  queryParams.value.pageNumber = page
  queryParams.value.pageSize = pageSize
  fetchList()
}

// 获取日志列表
const fetchLogs = async (taskId: number) => {
  loadingLogs.value = true
  try {
    const res = await syncApi.getTaskLogs(taskId)
    if (res.code === 200) {
      logs.value = res.data || []
      // 滚动到底部
      scrollToBottom()
    }
  } catch (error) {
    console.error(error)
  } finally {
    loadingLogs.value = false
  }
}

// 开启终端日志显示
const handleOpenLogs = (record: any) => {
  activeTaskId.value = record.id
  activeTaskName.value = record.configName
  logs.value = []
  isLogModalOpen.value = true
  fetchLogs(record.id)
  
  // 建立轮询
  startLogPolling(record.id, record.status)
}

// 轮询日志
const startLogPolling = (taskId: number, status: string) => {
  if (logPollTimer.value) clearInterval(logPollTimer.value)
  
  // 仅在任务正在运行、提交中时启动自动轮询
  if (logAutoPoll.value && (status === 'RUNNING' || status === 'SUBMITTING')) {
    logPollTimer.value = setInterval(async () => {
      try {
        const res = await syncApi.getTaskLogs(taskId)
        if (res.code === 200) {
          logs.value = res.data || []
          scrollToBottom()
        }
        
        // 可选：如果刷新到状态已经完成，清空轮询
        const statusRes = await syncApi.getTaskById(taskId)
        if (statusRes.code === 200) {
          const latestStatus = statusRes.data.status
          if (latestStatus !== 'RUNNING' && latestStatus !== 'SUBMITTING') {
            clearInterval(logPollTimer.value)
          }
        }
      } catch (err) {
        console.error(err)
      }
    }, 3000)
  }
}

const toggleLogPoll = () => {
  logAutoPoll.value = !logAutoPoll.value
  if (activeTaskId.value) {
    const record = listData.value.find(item => item.id === activeTaskId.value)
    if (record) startLogPolling(activeTaskId.value, record.status)
  }
  message.success(logAutoPoll.value ? '已开启日志自动刷新 (3s)' : '已暂停日志自动刷新')
}

// 滚动到底部
const scrollToBottom = () => {
  setTimeout(() => {
    if (terminalRef.value) {
      terminalRef.value.scrollTop = terminalRef.value.scrollHeight
    }
  }, 100)
}

// 关闭日志
const handleCloseLogs = () => {
  if (logPollTimer.value) {
    clearInterval(logPollTimer.value)
  }
  isLogModalOpen.value = false
  activeTaskId.value = null
}

// 计算时间差
const formatDuration = (seconds: number | undefined) => {
  if (seconds === undefined || seconds === null) return '--'
  if (seconds < 60) return `${seconds} 秒`
  const minutes = Math.floor(seconds / 60)
  const remSeconds = seconds % 60
  return `${minutes} 分 ${remSeconds} 秒`
}

onMounted(() => {
  fetchList()
})

onUnmounted(() => {
  if (logPollTimer.value) clearInterval(logPollTimer.value)
})

// 表格配置
const columns = [
  {
    title: '任务 ID',
    dataIndex: 'id',
    key: 'id',
    width: 80
  },
  {
    title: '规则配置名称',
    dataIndex: 'configName',
    key: 'configName'
  },
  {
    title: 'SeaTunnel Job ID',
    dataIndex: 'seatunnelJobId',
    key: 'seatunnelJobId',
    width: 200
  },
  {
    title: '任务状态',
    dataIndex: 'status',
    key: 'status',
    width: 130
  },
  {
    title: '已同步行数 / 总行数',
    key: 'rows',
    width: 200
  },
  {
    title: '任务耗时',
    dataIndex: 'durationSeconds',
    key: 'durationSeconds',
    width: 130
  },
  {
    title: '启动时间',
    dataIndex: 'startTime',
    key: 'startTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 260,
    fixed: 'right' as const
  }
]
</script>

<template>
  <div class="sync-tasks-manager">
    <!-- 页面标题 -->
    <div class="page-header flex-between">
      <div>
        <h1 class="page-title"><HistoryOutlined class="mr-8" />同步任务实例</h1>
        <p class="page-desc">追踪 SeaTunnel 所有下发的数据同步实例，抓取实时运行指标与控制台日志</p>
      </div>
      <a-button type="primary" :loading="loading" @click="fetchList">
        <template #icon><ReloadOutlined /></template>刷新列表
      </a-button>
    </div>

    <!-- 搜索筛选 -->
    <div class="search-form flex-between">
      <div class="flex" style="gap: 16px; flex: 1;">
        <a-select
          v-model:value="queryParams.status"
          placeholder="按照任务状态筛选"
          style="width: 200px;"
          allow-clear
        >
          <a-select-option value="SUBMITTING">SUBMITTING (提交中)</a-select-option>
          <a-select-option value="RUNNING">RUNNING (运行中)</a-select-option>
          <a-select-option value="SUCCESS">SUCCESS (同步成功)</a-select-option>
          <a-select-option value="FAILED">FAILED (同步失败)</a-select-option>
          <a-select-option value="CANCELED">CANCELED (已取消)</a-select-option>
        </a-select>
      </div>
      <div>
        <a-button type="primary" class="mr-8" @click="handleSearch">筛选</a-button>
        <a-button type="default" @click="handleReset">重置</a-button>
      </div>
    </div>

    <!-- 列表卡片 -->
    <div class="page-card table-card">
      <a-table
        :columns="columns"
        :data-source="listData"
        :loading="loading"
        :pagination="{
          current: queryParams.pageNumber,
          pageSize: queryParams.pageSize,
          total: total,
          showSizeChanger: true,
          onChange: onPageChange
        }"
        row-key="id"
        class="custom-table"
      >
        <template #bodyCell="{ column, record }">
          <!-- Job ID -->
          <template v-if="column.key === 'seatunnelJobId'">
            <span v-if="!record.seatunnelJobId" class="text-color-secondary">--</span>
            <code v-else class="job-id-code">{{ record.seatunnelJobId }}</code>
          </template>

          <!-- 状态 -->
          <template v-if="column.key === 'status'">
            <a-tag :color="
              record.status === 'SUCCESS' ? 'success' :
              record.status === 'RUNNING' ? 'processing' :
              record.status === 'FAILED' ? 'error' :
              record.status === 'CANCELED' ? 'warning' : 'default'
            ">
              <template #icon>
                <SyncOutlined :spin="record.status === 'RUNNING' || record.status === 'SUBMITTING'" v-if="record.status === 'RUNNING' || record.status === 'SUBMITTING'" />
                <CheckCircleOutlined v-else-if="record.status === 'SUCCESS'" />
                <CloseCircleOutlined v-else-if="record.status === 'FAILED'" />
              </template>
              {{ record.status }}
            </a-tag>
          </template>

          <!-- 行数 -->
          <template v-if="column.key === 'rows'">
            <div class="rows-metric" v-if="record.status !== 'SUBMITTING'">
              <span class="synced">{{ record.syncRows || 0 }}</span>
              <span class="divider">/</span>
              <span class="total">{{ record.totalRows || 0 }}</span>
              <span class="unit">行</span>
            </div>
            <span v-else class="text-color-secondary">计算中...</span>
          </template>

          <!-- 耗时 -->
          <template v-if="column.key === 'durationSeconds'">
            <span>{{ formatDuration(record.durationSeconds) }}</span>
          </template>

          <!-- 启动时间 -->
          <template v-if="column.key === 'startTime'">
            <span>{{ record.startTime ? dayjs(record.startTime).format('YYYY-MM-DD HH:mm:ss') : '--' }}</span>
          </template>

          <!-- 操作 -->
          <template v-if="column.key === 'action'">
            <div class="action-cell">
              <a-button type="link" size="small" @click="handleOpenLogs(record)">
                <template #icon><CodeOutlined /></template>控制台日志
              </a-button>
              
              <a-button type="link" size="small" @click="handleRefreshTask(record)">
                <template #icon><ReloadOutlined /></template>刷新状态
              </a-button>

              <a-button
                type="link"
                size="small"
                danger
                v-if="record.status === 'RUNNING' || record.status === 'SUBMITTING'"
                @click="handleCancelTask(record)"
              >
                <template #icon><StopOutlined /></template>停止
              </a-button>
            </div>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 拟物化 Console 终端弹窗 -->
    <a-modal
      v-model:open="isLogModalOpen"
      :title="`数据管道控制台 Terminal - [ ${activeTaskName} ]`"
      width="900px"
      :footer="null"
      @cancel="handleCloseLogs"
      class="terminal-modal"
    >
      <div class="terminal-container">
        <!-- 终端控制排 -->
        <div class="terminal-controls flex-between">
          <div class="terminal-title">
            <span class="green-dot"></span>
            <span class="yellow-dot"></span>
            <span class="red-dot"></span>
            <span class="terminal-path ml-8">seatunnel@engine-instance:~# tail -f job_{{ activeTaskId }}.log</span>
          </div>
          <div class="terminal-actions">
            <a-button
              type="text"
              class="terminal-btn mr-8"
              @click="toggleLogPoll"
            >
              <template #icon>
                <LoadingOutlined v-if="logAutoPoll" />
                <SyncOutlined v-else />
              </template>
              {{ logAutoPoll ? '自动尾部追加 (ON)' : '启动自动追踪' }}
            </a-button>
            <a-button type="text" class="terminal-btn" @click="fetchLogs(activeTaskId!)">
              <template #icon><ReloadOutlined /></template>刷新控制台
            </a-button>
          </div>
        </div>

        <!-- 终端黑匣主体 -->
        <div ref="terminalRef" class="terminal-console">
          <div v-if="loadingLogs && logs.length === 0" class="terminal-loading">
            <LoadingOutlined class="terminal-spinner" />正在初始化管道流控制台...
          </div>
          <div v-else-if="logs.length === 0" class="terminal-empty">
            [SeaTunnel Engine] 暂无当前任务生成的流日志...
          </div>
          <div v-else class="log-lines">
            <div
              v-for="log in logs"
              :key="log.id"
              :class="['log-line', `log-${log.logLevel.toLowerCase()}`]"
            >
              <span class="log-time">{{ dayjs(log.createTime).format('HH:mm:ss.SSS') }}</span>
              <span class="log-badge">{{ log.logLevel }}</span>
              <span class="log-msg"><DoubleRightOutlined class="arrow" />{{ log.message }}</span>
            </div>
            <!-- 终端光标闪烁 -->
            <div class="terminal-cursor-line">
              <CaretRightOutlined class="terminal-prompt" />
              <span class="cursor"></span>
            </div>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<style scoped lang="less">
.sync-tasks-manager {
  perspective: 2000px;
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

.rows-metric {
  font-family: SFMono-Regular, Consolas, monospace;
  font-weight: 600;

  .synced {
    color: var(--success-color);
    font-size: 14.5px;
  }

  .divider {
    margin: 0 4px;
    color: var(--text-color-secondary);
  }

  .total {
    color: var(--heading-color);
  }

  .unit {
    margin-left: 4px;
    font-size: 12px;
    font-weight: 500;
    color: var(--text-color-secondary);
  }
}

.action-cell {
  display: flex;
  gap: 8px;
}

/* ========================================================================
   拟物化 3D Terminal Console
   ======================================================================== */
.terminal-modal {
  :deep(.ant-modal-content) {
    padding: 0;
    overflow: hidden;
    border-radius: 16px;
  }
}

.terminal-container {
  background: #0f172a;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.1);

  .terminal-controls {
    background: #1e293b;
    padding: 10px 16px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);

    .green-dot, .yellow-dot, .red-dot {
      display: inline-block;
      width: 12px;
      height: 12px;
      border-radius: 50%;
      margin-right: 6px;
    }
    .red-dot { background: #ef4444; }
    .yellow-dot { background: #f59e0b; }
    .green-dot { background: #10b981; }

    .terminal-path {
      font-family: SFMono-Regular, Consolas, monospace;
      color: #94a3b8;
      font-size: 13px;
    }

    .terminal-btn {
      color: #cbd5e1;
      font-size: 13px;
      font-weight: 500;
      border-radius: 6px;
      
      &:hover {
        background: rgba(255, 255, 255, 0.08);
        color: #fff;
      }
    }
  }

  .terminal-console {
    height: 400px;
    overflow-y: auto;
    background: #090d16;
    padding: 20px;
    font-family: 'Fira Code', SFMono-Regular, Consolas, Monaco, monospace;
    font-size: 13px;
    line-height: 1.6;
    color: #e2e8f0;

    .terminal-loading, .terminal-empty {
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #64748b;
      gap: 10px;
      font-size: 14.5px;
    }

    .terminal-spinner {
      font-size: 20px;
    }

    .log-lines {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .log-line {
      display: flex;
      align-items: flex-start;
      word-break: break-all;

      .log-time {
        color: #475569;
        margin-right: 8px;
        flex-shrink: 0;
      }

      .log-badge {
        font-size: 10px;
        padding: 0 4px;
        border-radius: 3px;
        font-weight: 700;
        margin-right: 10px;
        flex-shrink: 0;
      }

      .log-msg {
        .arrow {
          font-size: 9px;
          margin-right: 6px;
          opacity: 0.5;
        }
      }

      /* 日志等级颜色与发光特效 */
      &.log-info {
        color: #10b981;
        .log-badge { background: rgba(16, 185, 129, 0.2); border: 1px solid rgba(16, 185, 129, 0.4); }
        text-shadow: 0 0 4px rgba(16, 185, 129, 0.2);
      }

      &.log-warn {
        color: #f59e0b;
        .log-badge { background: rgba(245, 158, 11, 0.2); border: 1px solid rgba(245, 158, 11, 0.4); }
        text-shadow: 0 0 4px rgba(245, 158, 11, 0.2);
      }

      &.log-error {
        color: #ef4444;
        .log-badge { background: rgba(239, 68, 68, 0.2); border: 1px solid rgba(239, 68, 68, 0.4); }
        text-shadow: 0 0 4px rgba(239, 68, 68, 0.2);
      }
    }

    .terminal-cursor-line {
      display: flex;
      align-items: center;
      margin-top: 6px;

      .terminal-prompt {
        color: #3b82f6;
        margin-right: 4px;
      }

      .cursor {
        display: inline-block;
        width: 8px;
        height: 15px;
        background: #3b82f6;
        animation: blink 1s infinite steps(2, start);
      }
    }
  }
}

@keyframes blink {
  to { visibility: hidden; }
}
</style>
