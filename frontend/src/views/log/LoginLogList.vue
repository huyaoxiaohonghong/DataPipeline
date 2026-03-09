<script setup lang="ts">
import { ref, reactive } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { getLoginLogPage, deleteLoginLog, cleanLoginLog } from '@/api/loginLog'
import type { LoginLog, LoginLogQuery } from '@/api/loginLog'
import { SyncOutlined, SearchOutlined, DeleteOutlined, ExclamationCircleOutlined, ClearOutlined } from '@ant-design/icons-vue'
import type { Dayjs } from 'dayjs'
import { createVNode } from 'vue'

// 查询参数
const queryParams = reactive<LoginLogQuery>({
  current: 1,
  size: 10,
  username: undefined,
  status: undefined,
  startTime: undefined,
  endTime: undefined
})

const dateRange = ref<[Dayjs, Dayjs]>()

// 列表数据
const loading = ref(false)
const dataSource = ref<LoginLog[]>([])
const total = ref(0)
const selectedRowKeys = ref<number[]>([])

// 表格列配置
const columns = [
  {
    title: '日志编号',
    dataIndex: 'id',
    width: 100
  },
  {
    title: '用户账号',
    dataIndex: 'username',
    width: 150
  },
  {
    title: '登录IP',
    dataIndex: 'ipAddress',
    width: 150
  },
  {
    title: '浏览器',
    dataIndex: 'browser',
    width: 130
  },
  {
    title: '操作系统',
    dataIndex: 'os',
    width: 130
  },
  {
    title: '登录状态',
    dataIndex: 'status',
    width: 100
  },
  {
    title: '操作信息',
    dataIndex: 'message',
    ellipsis: true
  },
  {
    title: '登录日期',
    dataIndex: 'loginTime',
    width: 180
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
    
    const res = await getLoginLogPage(queryParams)
    if (res.code === 200) {
      dataSource.value = res.data.records
      total.value = res.data.totalRow
    } else {
      message.error(res.message || '获取列表失败')
    }
  } catch (error) {
    console.error('获取登录日志列表失败:', error)
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
  queryParams.status = undefined
  dateRange.value = undefined
  handleQuery()
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
        await deleteLoginLog(selectedRowKeys.value)
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
    content: '确定要清空所有登录日志吗？此操作不可恢复！',
    okText: '确定',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        await cleanLoginLog()
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
      <h1 class="page-title">登录日志</h1>
      <p class="page-desc">查询并监控系统用户的登录行为与异常记录</p>
    </div>

    <!-- 搜索表单 -->
    <div class="search-form">
      <a-form layout="inline" :model="queryParams">
        <a-form-item label="用户账号">
          <a-input
            v-model:value="queryParams.username"
            placeholder="请输入账号名"
            allow-clear
            @pressEnter="handleQuery"
          />
        </a-form-item>
        <a-form-item label="登录状态">
          <a-select
            v-model:value="queryParams.status"
            placeholder="日志状态"
            allow-clear
            style="width: 120px"
          >
            <a-select-option :value="1">成功</a-select-option>
            <a-select-option :value="0">失败</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="登录时间">
          <a-range-picker
            v-model:value="dateRange"
            :show-time="false"
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
              {{ record.status === 1 ? '成功' : '失败' }}
            </a-tag>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<style scoped lang="less">
/* 复用全局 3D Glassmorphism 样式 (由 global less 控制) */
.page-container {
  height: 100%;
}
</style>
