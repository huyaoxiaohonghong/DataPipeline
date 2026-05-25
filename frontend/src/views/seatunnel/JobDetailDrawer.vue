<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { seatunnelApi, type JobDetail } from '@/api/seatunnel'
import { message } from 'ant-design-vue'
import {
  InfoCircleOutlined,
  ClockCircleOutlined,
  CodeOutlined,
  ApartmentOutlined,
  SyncOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  PauseCircleOutlined,
  LoadingOutlined,
  ArrowRightOutlined,
  ReloadOutlined,
  DatabaseOutlined,
  FunctionOutlined,
  ExportOutlined
} from '@ant-design/icons-vue'

const props = defineProps<{
  open: boolean
  jobId: string | null
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
}>()

const loading = ref(false)
const jobDetail = ref<JobDetail | null>(null)

// 获取任务详情
const fetchJobDetail = async () => {
  if (!props.jobId) return
  loading.value = true
  try {
    const res = await seatunnelApi.getJobDetail(props.jobId)
    if (res.code === 200) {
      jobDetail.value = res.data
    }
  } catch (error) {
    console.error('获取 SeaTunnel 任务详情失败:', error)
    message.error('获取任务详情失败')
  } finally {
    loading.value = false
  }
}

// 刷新详情
const handleRefresh = () => {
  fetchJobDetail()
}

// 关闭抽屉
const handleClose = () => {
  emit('update:open', false)
  jobDetail.value = null
}

// 当抽屉打开时自动获取数据
watch(() => props.open, (val) => {
  if (val && props.jobId) {
    fetchJobDetail()
  }
})

// 状态颜色映射
const statusConfig = computed(() => {
  const status = jobDetail.value?.jobStatus || jobDetail.value?.status || ''
  const map: Record<string, { color: string; icon: any; label: string }> = {
    RUNNING: { color: '#3B82F6', icon: SyncOutlined, label: 'RUNNING' },
    FINISHED: { color: '#10B981', icon: CheckCircleOutlined, label: 'FINISHED' },
    CANCELED: { color: '#F59E0B', icon: PauseCircleOutlined, label: 'CANCELED' },
    FAILED: { color: '#EF4444', icon: CloseCircleOutlined, label: 'FAILED' },
    CANCELLING: { color: '#F97316', icon: LoadingOutlined, label: 'CANCELLING' }
  }
  return map[status] || { color: '#64748B', icon: InfoCircleOutlined, label: status || 'UNKNOWN' }
})

// DAG 节点映射
const dagVertices = computed(() => {
  return jobDetail.value?.jobDag?.vertices || []
})

// 节点类型图标
const getVertexIcon = (type: string) => {
  const map: Record<string, any> = {
    source: DatabaseOutlined,
    transform: FunctionOutlined,
    sink: ExportOutlined
  }
  return map[type?.toLowerCase()] || CodeOutlined
}

// 节点类型颜色
const getVertexColor = (type: string) => {
  const map: Record<string, string> = {
    source: '#3B82F6',
    transform: '#F97316',
    sink: '#10B981'
  }
  return map[type?.toLowerCase()] || '#64748B'
}

// 格式化 JSON
const formattedConfig = computed(() => {
  if (!jobDetail.value) return '{}'
  // 排除 DAG 相关字段，只展示核心配置
  const { jobDag, metrics, ...rest } = jobDetail.value
  try {
    return JSON.stringify(rest, null, 2)
  } catch {
    return '{}'
  }
})

// 格式化 metrics
const metricsEntries = computed(() => {
  if (!jobDetail.value?.metrics) return []
  return Object.entries(jobDetail.value.metrics).map(([key, value]) => ({
    key,
    value: typeof value === 'object' ? JSON.stringify(value) : String(value)
  }))
})
</script>

<template>
  <a-drawer
    :open="open"
    :width="720"
    placement="right"
    @close="handleClose"
    class="job-detail-drawer"
    :destroy-on-close="true"
  >
    <template #title>
      <div class="drawer-title">
        <ApartmentOutlined class="mr-8" />
        <span>SeaTunnel 任务详情</span>
      </div>
    </template>

    <template #extra>
      <a-button type="text" @click="handleRefresh" :loading="loading">
        <template #icon><ReloadOutlined /></template>
        刷新
      </a-button>
    </template>

    <a-spin :spinning="loading">
      <template v-if="jobDetail">
        <!-- 状态 Banner -->
        <div class="status-banner" :style="{ borderColor: statusConfig.color }">
          <div class="status-icon-wrap" :style="{ background: statusConfig.color }">
            <component :is="statusConfig.icon" :spin="statusConfig.label === 'RUNNING'" />
          </div>
          <div class="status-info">
            <div class="status-label">任务状态</div>
            <div class="status-value" :style="{ color: statusConfig.color }">
              {{ statusConfig.label }}
            </div>
          </div>
          <div class="job-id-display">
            <div class="status-label">Job ID</div>
            <code class="job-id-code">{{ jobDetail.jobId }}</code>
          </div>
        </div>

        <!-- 基本信息 -->
        <div class="section">
          <h3 class="section-title">
            <InfoCircleOutlined class="mr-8" />基本信息
          </h3>
          <a-descriptions bordered :column="2" size="small" class="info-descriptions">
            <a-descriptions-item label="任务名称" :span="2">
              <span class="info-value-bold">{{ jobDetail.jobName || '--' }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="任务ID">
              <code class="mono-code">{{ jobDetail.jobId }}</code>
            </a-descriptions-item>
            <a-descriptions-item label="任务状态">
              <a-tag :color="statusConfig.color" class="status-tag-sm">
                <template #icon>
                  <component :is="statusConfig.icon" :spin="statusConfig.label === 'RUNNING'" />
                </template>
                {{ statusConfig.label }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="提交时间">
              {{ jobDetail.submitTime || jobDetail.createTime || '--' }}
            </a-descriptions-item>
            <a-descriptions-item label="完成时间">
              {{ jobDetail.finishTime || '--' }}
            </a-descriptions-item>
            <a-descriptions-item label="SavePoint 启动" v-if="jobDetail.isStartWithSavePoint !== undefined">
              <a-tag :color="jobDetail.isStartWithSavePoint ? 'blue' : 'default'">
                {{ jobDetail.isStartWithSavePoint ? '是' : '否' }}
              </a-tag>
            </a-descriptions-item>
          </a-descriptions>
        </div>

        <!-- Pipeline DAG 可视化 -->
        <div class="section" v-if="dagVertices.length > 0">
          <h3 class="section-title">
            <ApartmentOutlined class="mr-8" />Pipeline DAG
          </h3>
          <div class="dag-container">
            <div class="dag-flow">
              <template v-for="(vertex, index) in dagVertices" :key="vertex.vertexId">
                <div class="dag-node" :style="{ borderColor: getVertexColor(vertex.type) }">
                  <div class="dag-node-header" :style="{ background: getVertexColor(vertex.type) }">
                    <component :is="getVertexIcon(vertex.type)" />
                    <span>{{ vertex.type?.toUpperCase() }}</span>
                  </div>
                  <div class="dag-node-body">
                    <div class="dag-plugin">{{ vertex.pluginName || vertex.action || 'Plugin' }}</div>
                    <div class="dag-table" v-if="vertex.tableFullName">
                      {{ vertex.tableFullName }}
                    </div>
                    <div class="dag-connector" v-if="vertex.connectorType">
                      {{ vertex.connectorType }}
                    </div>
                  </div>
                </div>
                <!-- 连接箭头 -->
                <div class="dag-arrow" v-if="index < dagVertices.length - 1">
                  <ArrowRightOutlined />
                </div>
              </template>
            </div>
          </div>
        </div>

        <!-- 运行指标 -->
        <div class="section" v-if="metricsEntries.length > 0">
          <h3 class="section-title">
            <ClockCircleOutlined class="mr-8" />运行指标 (Metrics)
          </h3>
          <div class="metrics-terminal">
            <div class="terminal-bar">
              <span class="dot green"></span>
              <span class="dot yellow"></span>
              <span class="dot red"></span>
              <span class="terminal-label">seatunnel@metrics:~$ cat job_metrics.json</span>
            </div>
            <div class="terminal-body">
              <div v-for="entry in metricsEntries" :key="entry.key" class="metric-line">
                <span class="metric-key">{{ entry.key }}</span>
                <span class="metric-sep">:</span>
                <span class="metric-val">{{ entry.value }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 原始 JSON 配置 -->
        <div class="section">
          <h3 class="section-title">
            <CodeOutlined class="mr-8" />原始 JSON 数据
          </h3>
          <div class="json-terminal">
            <div class="terminal-bar">
              <span class="dot green"></span>
              <span class="dot yellow"></span>
              <span class="dot red"></span>
              <span class="terminal-label">seatunnel@engine:~$ cat job_config.json</span>
            </div>
            <pre class="terminal-body json-pre">{{ formattedConfig }}</pre>
          </div>
        </div>
      </template>

      <!-- 空状态 -->
      <a-empty v-else-if="!loading" description="暂无任务详情数据" />
    </a-spin>
  </a-drawer>
</template>

<style scoped lang="less">
.drawer-title {
  display: flex;
  align-items: center;
  font-size: 17px;
  font-weight: 700;
  color: var(--heading-color);
}

/* 状态 Banner */
.status-banner {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px 24px;
  border-radius: 16px;
  background: var(--glass-bg);
  backdrop-filter: blur(20px);
  border: 2px solid;
  margin-bottom: 28px;
  box-shadow: var(--3d-shadow-light);

  .status-icon-wrap {
    width: 48px;
    height: 48px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    color: #fff;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
    flex-shrink: 0;
  }

  .status-info {
    .status-label {
      font-size: 12px;
      font-weight: 600;
      color: var(--text-color-secondary);
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }
    .status-value {
      font-size: 22px;
      font-weight: 800;
      letter-spacing: -0.02em;
    }
  }

  .job-id-display {
    margin-left: auto;
    text-align: right;

    .status-label {
      font-size: 12px;
      font-weight: 600;
      color: var(--text-color-secondary);
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }

    .job-id-code {
      font-family: SFMono-Regular, Consolas, Liberation Mono, Menlo, monospace;
      background: rgba(148, 163, 184, 0.15);
      padding: 4px 10px;
      border-radius: 8px;
      font-size: 13px;
      border: 1px solid rgba(148, 163, 184, 0.2);
      color: var(--primary-color);
      display: inline-block;
      margin-top: 4px;
    }
  }
}

/* Section */
.section {
  margin-bottom: 28px;

  .section-title {
    font-size: 16px;
    font-weight: 700;
    color: var(--heading-color);
    margin: 0 0 16px 0;
    display: flex;
    align-items: center;
  }
}

.info-descriptions {
  border-radius: 12px;
  overflow: hidden;
}

.info-value-bold {
  font-weight: 700;
  color: var(--heading-color);
}

.mono-code {
  font-family: SFMono-Regular, Consolas, Liberation Mono, Menlo, monospace;
  font-size: 13px;
  color: var(--primary-color);
  background: rgba(148, 163, 184, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
}

.status-tag-sm {
  font-weight: 600;
}

/* Pipeline DAG */
.dag-container {
  background: var(--glass-bg);
  backdrop-filter: blur(16px);
  border-radius: 16px;
  padding: 24px;
  border: var(--card-border);
  box-shadow: var(--3d-shadow-light);
  overflow-x: auto;
}

.dag-flow {
  display: flex;
  align-items: center;
  gap: 0;
  min-width: max-content;
}

.dag-node {
  min-width: 160px;
  border-radius: 12px;
  border: 2px solid;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.6);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
  }

  .dag-node-header {
    padding: 8px 14px;
    color: #fff;
    font-size: 12px;
    font-weight: 700;
    display: flex;
    align-items: center;
    gap: 6px;
    text-transform: uppercase;
    letter-spacing: 0.05em;
  }

  .dag-node-body {
    padding: 12px 14px;

    .dag-plugin {
      font-size: 14px;
      font-weight: 700;
      color: var(--heading-color);
    }

    .dag-table, .dag-connector {
      font-size: 12px;
      color: var(--text-color-secondary);
      margin-top: 4px;
      font-family: SFMono-Regular, Consolas, monospace;
    }
  }
}

[data-theme='dark'] .dag-node {
  background: rgba(15, 23, 42, 0.6);
}

.dag-arrow {
  padding: 0 12px;
  font-size: 18px;
  color: var(--text-color-secondary);
  animation: pulseArrow 2s infinite ease-in-out;
}

@keyframes pulseArrow {
  0% { opacity: 0.4; transform: translateX(0); }
  50% { opacity: 1; transform: translateX(4px); }
  100% { opacity: 0.4; transform: translateX(0); }
}

/* Terminal style panels */
.metrics-terminal, .json-terminal {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.08);

  .terminal-bar {
    background: #1e293b;
    padding: 10px 16px;
    display: flex;
    align-items: center;
    gap: 6px;

    .dot {
      width: 12px;
      height: 12px;
      border-radius: 50%;
      &.green { background: #10b981; }
      &.yellow { background: #f59e0b; }
      &.red { background: #ef4444; }
    }

    .terminal-label {
      margin-left: 10px;
      font-family: SFMono-Regular, Consolas, monospace;
      color: #94a3b8;
      font-size: 12.5px;
    }
  }

  .terminal-body {
    background: #0f172a;
    padding: 16px 20px;
    font-family: 'Fira Code', SFMono-Regular, Consolas, Monaco, monospace;
    font-size: 13px;
    line-height: 1.7;
    color: #e2e8f0;
    max-height: 320px;
    overflow-y: auto;
  }
}

.metric-line {
  display: flex;
  gap: 8px;

  .metric-key {
    color: #38bdf8;
    font-weight: 600;
    min-width: 200px;
  }

  .metric-sep {
    color: #475569;
  }

  .metric-val {
    color: #10b981;
  }
}

.json-pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  color: #a5f3fc;
}
</style>
