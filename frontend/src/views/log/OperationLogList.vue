<script setup lang="ts">
import { ref, reactive } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { getOperationLogPage, deleteOperationLog, cleanOperationLog } from '@/api/operationLog'
import type { OperationLog, OperationLogQuery } from '@/api/operationLog'
import { SyncOutlined, SearchOutlined, EyeOutlined, DeleteOutlined, ExclamationCircleOutlined, ClearOutlined } from '@ant-design/icons-vue'
import type { Dayjs } from 'dayjs'
import type { TableColumnsType } from 'ant-design-vue'
import { createVNode } from 'vue'

// 查询参数
const queryParams = reactive<OperationLogQuery>({
  current: 1,
  size: 10,
  username: undefined,
  module: undefined,
  status: undefined,
  startTime: undefined,
  endTime: undefined
})

const dateRange = ref<[Dayjs, Dayjs]>()

// 列表数据
const loading = ref(false)
const dataSource = ref<OperationLog[]>([])
const total = ref(0)
const detailVisible = ref(false)
const currentDetail = ref<OperationLog | null>(null)
const selectedRowKeys = ref<number[]>([])

// 表格列配置
const columns: TableColumnsType = [
  {
    title: '日志编号',
    dataIndex: 'id',
    width: 100
  },
  {
    title: '系统模块',
    dataIndex: 'module',
    width: 150
  },
  {
    title: '操作类型',
    dataIndex: 'action',
    width: 150
  },
  {
    title: '操作人员',
    dataIndex: 'username',
    width: 120
  },
  {
    title: '主机IP',
    dataIndex: 'ipAddress',
    width: 140
  },
  {
    title: '操作状态',
    dataIndex: 'status',
    width: 100
  },
  {
    title: '耗时(ms)',
    dataIndex: 'duration',
    width: 100
  },
  {
    title: '操作日期',
    dataIndex: 'createTime',
    width: 180
  },
  {
    title: '操作',
    key: 'actionTool',
    width: 100,
    fixed: 'right'
  }
]

// 获取列表数据
const fetchList = async () => {
  try {
    loading.value = true
    
    // 处理日期参数
    if (dateRange.value && dateRange.value.length === 2) {
      queryParams.startTime = dateRange.value[0].format('YYYY-MM-DD 00:00:00')
      queryParams.endTime = dateRange.value[1].format('YYYY-MM-DD 23:59:59')
    } else {
      queryParams.startTime = undefined
      queryParams.endTime = undefined
    }
    
    const res = await getOperationLogPage(queryParams)
    if (res.code === 200) {
      dataSource.value = res.data.records
      total.value = res.data.totalRow
    } else {
      message.error(res.message || '获取列表失败')
    }
  } catch (error) {
    console.error('获取操作日志列表失败:', error)
    message.error('获取列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  queryParams.current = 1
  fetchList()
}

// 重置查询
const resetQuery = () => {
  queryParams.username = undefined
  queryParams.module = undefined
  queryParams.status = undefined
  dateRange.value = undefined
  handleQuery()
}

// 查看详情
const handleDetail = (record: any) => {
  currentDetail.value = record
  detailVisible.value = true
}

// 分页变化
const handleTableChange = (pag: any) => {
  queryParams.current = pag.current
  queryParams.size = pag.pageSize
  fetchList()
}

// 选择变化
const onSelectChange = (keys: any[]) => {
  selectedRowKeys.value = keys
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要删除的记录')
    return
  }
  Modal.confirm({
    title: '确认删除',
    icon: createVNode(ExclamationCircleOutlined),
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 条记录吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        await deleteOperationLog(selectedRowKeys.value)
        message.success('删除成功')
        selectedRowKeys.value = []
        fetchList()
      } catch (error) {
        message.error('删除失败')
      }
    }
  })
}

// 清空
const handleClean = () => {
  Modal.confirm({
    title: '确认清空',
    icon: createVNode(ExclamationCircleOutlined),
    content: '确定要清空所有操作日志吗？此操作不可恢复！',
    okText: '确定',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        await cleanOperationLog()
        message.success('清空成功')
        selectedRowKeys.value = []
        fetchList()
      } catch (error) {
        message.error('清空失败')
      }
    }
  })
}

// 初始化
fetchList()
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">操作日志</h1>
      <p class="page-desc">查看系统业务相关的后台操作与API调用记录</p>
    </div>

    <!-- 搜索表单 -->
    <div class="search-form">
      <a-form layout="inline" :model="queryParams">
        <a-form-item label="操作人员">
          <a-input
            v-model:value="queryParams.username"
            placeholder="请输入操作人员"
            allow-clear
            @pressEnter="handleQuery"
          />
        </a-form-item>
        <a-form-item label="系统模块">
          <a-input
            v-model:value="queryParams.module"
            placeholder="请输入系统模块"
            allow-clear
            @pressEnter="handleQuery"
          />
        </a-form-item>
        <a-form-item label="操作状态">
          <a-select
            v-model:value="queryParams.status"
            placeholder="日志状态"
            allow-clear
            style="width: 120px"
          >
            <a-select-option :value="1">成功</a-select-option>
            <a-select-option :value="0">异常</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="操作时间">
          <a-range-picker
            v-model:value="dateRange"
            :show-time="false"
            style="width: 240px"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleQuery">
              <template #icon><SearchOutlined /></template>
              搜索
            </a-button>
            <a-button @click="resetQuery">
              <template #icon><SyncOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>

    <!-- 列表数据 -->
    <div class="page-card p-24">
      <div class="table-toolbar">
        <h3>数据列表</h3>
        <a-space>
          <a-button danger :disabled="selectedRowKeys.length === 0" @click="handleBatchDelete">
            <template #icon><DeleteOutlined /></template>
            批量删除
          </a-button>
          <a-button danger type="primary" @click="handleClean">
            <template #icon><ClearOutlined /></template>
            清空
          </a-button>
          <a-button @click="fetchList">
            <template #icon><SyncOutlined /></template>
            刷新
          </a-button>
        </a-space>
      </div>
      
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :row-key="record => record.id"
        :loading="loading"
        :row-selection="{
          selectedRowKeys: selectedRowKeys,
          onChange: onSelectChange
        }"
        :pagination="{
          current: queryParams.current,
          pageSize: queryParams.size,
          total: total,
          showSizeChanger: true,
          showTotal: (total: number) => `共 ${total} 条`
        }"
        @change="handleTableChange"
        size="middle"
        bordered
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'status'">
            <a-tag :color="record.status === 1 ? 'success' : 'error'">
              {{ record.status === 1 ? '成功' : '异常' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'actionTool'">
            <a-button type="link" size="small" @click="handleDetail(record)">
              <template #icon><EyeOutlined /></template>详情
            </a-button>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="操作日志详情"
      width="800px"
      :footer="null"
      destroyOnClose
    >
      <a-descriptions bordered :column="2" size="small" v-if="currentDetail">
        <a-descriptions-item label="操作模块">{{ currentDetail.module }} / {{ currentDetail.action }}</a-descriptions-item>
        <a-descriptions-item label="请求方式">{{ currentDetail.method }}</a-descriptions-item>
        <a-descriptions-item label="操作人员">{{ currentDetail.username }}</a-descriptions-item>
        <a-descriptions-item label="操作主机">{{ currentDetail.ipAddress }}</a-descriptions-item>
        <a-descriptions-item label="操作状态">
          <a-tag :color="currentDetail.status === 1 ? 'success' : 'error'">
            {{ currentDetail.status === 1 ? '成功' : '异常' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="消耗时间">{{ currentDetail.duration }} ms</a-descriptions-item>
        <a-descriptions-item label="操作时间" :span="2">{{ currentDetail.createTime }}</a-descriptions-item>
        <!-- 跨行的数据 -->
        <a-descriptions-item label="请求参数" :span="2">
          <div class="code-block">{{ currentDetail.params || '无' }}</div>
        </a-descriptions-item>
        <a-descriptions-item label="异常信息" :span="2" v-if="currentDetail.status === 0">
          <div class="code-block error-block">{{ currentDetail.errorMsg || '无' }}</div>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<style scoped lang="less">
.page-container {
  height: 100%;
}

.code-block {
  background: var(--layout-btn-bg);
  padding: 12px;
  border-radius: 8px;
  font-family: 'Fira Code', monospace;
  font-size: 13px;
  word-break: break-all;
  white-space: pre-wrap;
  max-height: 200px;
  overflow-y: auto;
  border: var(--glass-border);
}

.error-block {
  color: var(--error-color);
}
</style>
