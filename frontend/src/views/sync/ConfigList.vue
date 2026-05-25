<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { syncApi, type SyncConfigVO } from '@/api/sync'
import { message, Modal } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import {
  SlidersOutlined,
  PlusOutlined,
  PlayCircleOutlined,
  EditOutlined,
  DeleteOutlined,
  ArrowRightOutlined
} from '@ant-design/icons-vue'
import ConfigEditModal from './ConfigEditModal.vue'

const router = useRouter()
const loading = ref(false)
const listData = ref<SyncConfigVO[]>([])

// 搜索条件
const queryParams = ref({
  pageNumber: 1,
  pageSize: 10,
  name: '',
  syncMode: undefined as string | undefined
})
const total = ref(0)

// 模态框状态
const isModalOpen = ref(false)
const selectedConfigId = ref<number | null>(null)

// 获取列表数据
const fetchList = async () => {
  loading.value = true
  try {
    const res = await syncApi.listConfigs(queryParams.value)
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

// 搜索/重置
const handleSearch = () => {
  queryParams.value.pageNumber = 1
  fetchList()
}

const handleReset = () => {
  queryParams.value.name = ''
  queryParams.value.syncMode = undefined
  queryParams.value.pageNumber = 1
  fetchList()
}

// 分页变化
const onPageChange = (page: number, pageSize: number) => {
  queryParams.value.pageNumber = page
  queryParams.value.pageSize = pageSize
  fetchList()
}

// 新增
const handleAdd = () => {
  selectedConfigId.value = null
  isModalOpen.value = true
}

// 编辑
const handleEdit = (record: any) => {
  selectedConfigId.value = record.id
  isModalOpen.value = true
}

// 状态切换 (启用/禁用)
const handleStatusChange = async (checked: boolean, record: any) => {
  try {
    const res = await syncApi.updateConfig(record.id, { enabled: checked })
    if (res.code === 200) {
      message.success(checked ? '数据源同步规则已启用' : '数据源同步规则已禁用')
      record.enabled = checked
    }
  } catch (error) {
    console.error(error)
  }
}

// 立即触发执行
const handleExecute = (record: any) => {
  Modal.confirm({
    title: '确定要立即触发此数据同步任务吗？',
    icon: null,
    content: `这将立即生成一条同步任务并提交至 SeaTunnel 集群。源表 [${record.sourceTable}] -> 目标表 [${record.targetTable}]。`,
    okText: '确定提交',
    cancelText: '取消',
    async onOk() {
      try {
        const res = await syncApi.executeConfig(record.id)
        if (res.code === 200) {
          message.success('SeaTunnel 任务已提交，正在为您跳转到任务历史！')
          router.push('/sync/tasks')
        }
      } catch (error) {
        console.error(error)
      }
    }
  })
}

// 删除配置
const handleDelete = (id: number) => {
  Modal.confirm({
    title: '确定要删除此同步配置吗？',
    icon: null,
    content: '此操作不可恢复，已经生成的任务历史不会受影响，但无法再次执行。',
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      try {
        const res = await syncApi.deleteConfig(id)
        if (res.code === 200) {
          message.success('删除同步配置成功')
          fetchList()
        }
      } catch (error) {
        console.error(error)
      }
    }
  })
}

onMounted(() => {
  fetchList()
})

// 表格列配置
const columns = [
  {
    title: '规则名称',
    dataIndex: 'name',
    key: 'name'
  },
  {
    title: '源表 ➔ 目标表',
    key: 'direction',
    width: 320
  },
  {
    title: '同步模式',
    dataIndex: 'syncMode',
    key: 'syncMode',
    width: 130
  },
  {
    title: '状态',
    dataIndex: 'enabled',
    key: 'enabled',
    width: 100
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    key: 'updateTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 220,
    fixed: 'right' as const
  }
]
</script>

<template>
  <div class="sync-configs-manager">
    <!-- 页面标题 -->
    <div class="page-header flex-between">
      <div>
        <h1 class="page-title"><SlidersOutlined class="mr-8" />同步配置规则</h1>
        <p class="page-desc">设计、编排和管理从源数据库到目标数据库的 SeaTunnel 定向同步规则</p>
      </div>
      <a-button type="primary" @click="handleAdd">
        <template #icon><PlusOutlined /></template>新建规则
      </a-button>
    </div>

    <!-- 搜索 -->
    <div class="search-form flex-between">
      <div class="flex" style="gap: 16px; flex: 1;">
        <a-input
          v-model:value="queryParams.name"
          placeholder="搜索配置规则名称"
          style="width: 260px;"
          @pressEnter="handleSearch"
        />
        <a-select
          v-model:value="queryParams.syncMode"
          placeholder="同步类型筛选"
          style="width: 180px;"
          allow-clear
        >
          <a-select-option value="FULL">FULL (全量同步)</a-select-option>
          <a-select-option value="INCREMENTAL">INCREMENTAL (增量同步)</a-select-option>
        </a-select>
      </div>
      <div>
        <a-button type="primary" class="mr-8" @click="handleSearch">搜索</a-button>
        <a-button type="default" @click="handleReset">重置</a-button>
      </div>
    </div>

    <!-- 表格卡片 -->
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
          <!-- 规则名称 -->
          <template v-if="column.key === 'name'">
            <div class="config-name-cell">
              <span class="name-text">{{ record.name }}</span>
              <span class="desc-text text-ellipsis">{{ record.description || '暂无描述信息' }}</span>
            </div>
          </template>

          <!-- 迁移流向 -->
          <template v-if="column.key === 'direction'">
            <div class="direction-cell flex-center">
              <div class="endpoint">
                <span class="db text-ellipsis">{{ record.sourceDbName || '源库' }}</span>
                <span class="table text-ellipsis">{{ record.sourceTable }}</span>
              </div>
              <ArrowRightOutlined class="direction-arrow" />
              <div class="endpoint">
                <span class="db text-ellipsis">{{ record.targetDbName || '目标库' }}</span>
                <span class="table text-ellipsis">{{ record.targetTable }}</span>
              </div>
            </div>
          </template>

          <!-- 同步模式 -->
          <template v-if="column.key === 'syncMode'">
            <a-tag :color="record.syncMode === 'FULL' ? 'blue' : 'purple'">
              {{ record.syncMode === 'FULL' ? '全量同步' : '增量同步' }}
            </a-tag>
            <div class="incremental-info" v-if="record.syncMode === 'INCREMENTAL'">
              <code class="col-field">{{ record.incrementalField }}</code>
            </div>
          </template>

          <!-- 规则开关 -->
          <template v-if="column.key === 'enabled'">
            <a-switch
              :checked="record.enabled"
              @change="(checked: any) => handleStatusChange(checked, record)"
            />
          </template>

          <!-- 操作按钮列 -->
          <template v-if="column.key === 'action'">
            <div class="action-cell">
              <a-button
                type="link"
                size="small"
                :disabled="!record.enabled"
                @click="handleExecute(record)"
              >
                <template #icon><PlayCircleOutlined /></template>立即执行
              </a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">
                <template #icon><EditOutlined /></template>编辑
              </a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record.id)">
                <template #icon><DeleteOutlined /></template>删除
              </a-button>
            </div>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 弹窗向导 -->
    <ConfigEditModal
      v-model:open="isModalOpen"
      :config-id="selectedConfigId"
      @success="fetchList"
    />
  </div>
</template>

<style scoped lang="less">
.sync-configs-manager {
  perspective: 2000px;
}

.table-card {
  padding: 24px;
}

.config-name-cell {
  display: flex;
  flex-direction: column;

  .name-text {
    font-weight: 700;
    font-size: 15px;
    color: var(--heading-color);
  }

  .desc-text {
    font-size: 12.5px;
    color: var(--text-color-secondary);
    margin-top: 4px;
    max-width: 220px;
  }
}

.direction-cell {
  justify-content: flex-start;
  gap: 12px;

  .endpoint {
    display: flex;
    flex-direction: column;
    width: 120px;
    background: rgba(148, 163, 184, 0.08);
    padding: 6px 12px;
    border-radius: 8px;
    border: 1px solid rgba(148, 163, 184, 0.1);

    .db {
      font-size: 11px;
      font-weight: 700;
      color: var(--text-color-secondary);
      text-transform: uppercase;
    }

    .table {
      font-size: 13px;
      font-weight: 600;
      color: var(--heading-color);
      margin-top: 2px;
    }
  }

  .direction-arrow {
    font-size: 16px;
    opacity: 0.6;
    animation: slideRight 2s infinite ease-in-out;
  }
}

.incremental-info {
  margin-top: 4px;
  .col-field {
    font-family: SFMono-Regular, Consolas, monospace;
    font-size: 11px;
    color: var(--cta-color);
    background: rgba(249, 115, 22, 0.1);
    padding: 1px 4px;
    border-radius: 4px;
  }
}

.action-cell {
  display: flex;
  gap: 8px;
}

@keyframes slideRight {
  0% { transform: translateX(0); opacity: 0.4; }
  50% { transform: translateX(4px); opacity: 0.9; }
  100% { transform: translateX(0); opacity: 0.4; }
}
</style>
