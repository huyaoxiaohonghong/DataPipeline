<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import {
  ReloadOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  DatabaseOutlined,
  HddOutlined,
  TableOutlined,
  LinkOutlined
} from '@ant-design/icons-vue'
import { dbConnectionApi, getDbTypeInfo } from '@/api/db'
import type { DbConnection, DbMonitorInfo } from '@/api/db'

// ==================== Props & Emits ====================

const props = defineProps<{
  visible: boolean
  connection: DbConnection | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
}>()

// ==================== 状态定义 ====================

const loading = ref(false)
const monitorInfo = ref<DbMonitorInfo | null>(null)

// ==================== 计算属性 ====================

const isVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

// ==================== 监听 ====================

watch(() => props.visible, (val) => {
  if (val && props.connection) {
    loadMonitorInfo()
  } else {
    monitorInfo.value = null
  }
})

// ==================== 方法 ====================

/**
 * 加载监控信息
 */
const loadMonitorInfo = async () => {
  if (!props.connection) return
  
  loading.value = true
  try {
    const { data } = await dbConnectionApi.getMonitor(props.connection.id)
    monitorInfo.value = data as DbMonitorInfo
  } catch (error: any) {
    monitorInfo.value = {
      status: 'ERROR',
      errorMessage: error.message || '获取监控信息失败'
    }
  } finally {
    loading.value = false
  }
}

/**
 * 关闭弹窗
 */
const handleClose = () => {
  isVisible.value = false
}
</script>

<template>
  <a-modal
    v-model:open="isVisible"
    title="数据库监控"
    :width="700"
    :footer="null"
    @cancel="handleClose"
  >
    <a-spin :spinning="loading">
      <!-- 连接信息头部 -->
      <div v-if="connection" class="monitor-header">
        <a-space>
          <a-tag :color="getDbTypeInfo(connection.dbType).color" size="large">
            <DatabaseOutlined />
            {{ getDbTypeInfo(connection.dbType).label }}
          </a-tag>
          <h3 style="margin: 0">{{ connection.name }}</h3>
          <a-button size="small" @click="loadMonitorInfo">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
        </a-space>
        <div class="connection-detail">
          <span><LinkOutlined /> {{ connection.host }}:{{ connection.port }}/{{ connection.databaseName }}</span>
        </div>
      </div>

      <!-- 状态指示 -->
      <div v-if="monitorInfo" class="monitor-status">
        <a-result
          v-if="monitorInfo.status === 'CONNECTED'"
          status="success"
          :title="monitorInfo.version || '已连接'"
          :sub-title="`响应时间：${monitorInfo.responseTimeMs}ms`"
        >
          <template #icon>
            <CheckCircleOutlined style="color: #52c41a" />
          </template>
        </a-result>
        <a-result
          v-else
          status="error"
          title="连接失败"
          :sub-title="monitorInfo.errorMessage"
        >
          <template #icon>
            <CloseCircleOutlined style="color: #ff4d4f" />
          </template>
        </a-result>
      </div>

      <!-- 统计指标 -->
      <div v-if="monitorInfo && monitorInfo.status === 'CONNECTED'" class="monitor-stats">
        <a-row :gutter="16">
          <a-col :span="8">
            <a-statistic title="当前连接数" :value="monitorInfo.activeConnections || 0">
              <template #prefix>
                <LinkOutlined />
              </template>
              <template #suffix v-if="monitorInfo.maxConnections">
                / {{ monitorInfo.maxConnections }}
              </template>
            </a-statistic>
          </a-col>
          <a-col :span="8">
            <a-statistic title="数据库大小" :value="monitorInfo.databaseSizeMB?.toFixed(2) || '-'">
              <template #prefix>
                <HddOutlined />
              </template>
              <template #suffix>MB</template>
            </a-statistic>
          </a-col>
          <a-col :span="8">
            <a-statistic title="表数量" :value="monitorInfo.tableCount || 0">
              <template #prefix>
                <TableOutlined />
              </template>
            </a-statistic>
          </a-col>
        </a-row>
      </div>

      <!-- 表列表 -->
      <div v-if="monitorInfo && monitorInfo.tables && monitorInfo.tables.length > 0" class="monitor-tables">
        <a-divider orientation="left">
          <TableOutlined /> 数据表列表 ({{ monitorInfo.tables.length }})
        </a-divider>
        <div class="table-list">
          <a-tag 
            v-for="table in monitorInfo.tables" 
            :key="table" 
            color="default"
            style="margin: 4px"
          >
            {{ table }}
          </a-tag>
        </div>
      </div>
    </a-spin>
  </a-modal>
</template>

<style scoped lang="less">
.monitor-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
  
  .connection-detail {
    margin-top: 8px;
    color: #666;
    font-size: 13px;
  }
}

.monitor-status {
  margin-bottom: 24px;
  
  :deep(.ant-result) {
    padding: 16px 0;
  }
}

.monitor-stats {
  margin-bottom: 24px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.monitor-tables {
  .table-list {
    max-height: 200px;
    overflow-y: auto;
    padding: 8px 0;
  }
}
</style>
