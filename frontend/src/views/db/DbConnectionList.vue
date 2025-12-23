<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  SearchOutlined,
  ReloadOutlined,
  EditOutlined,
  DeleteOutlined,
  ApiOutlined,
  DashboardOutlined,
  DatabaseOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined
} from '@ant-design/icons-vue'
import { dbConnectionApi, DbTypeOptions, getDbTypeInfo } from '@/api/db'
import type { DbConnection, DbConnectionQueryParams, DbMonitorInfo } from '@/api/db'
import type { PageResult } from '@/types'
import DbConnectionFormModal from './components/DbConnectionFormModal.vue'
import DbMonitorModal from './components/DbMonitorModal.vue'

// ==================== 表格配置 ====================

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 70,
    fixed: 'left' as const
  },
  {
    title: '连接名称',
    dataIndex: 'name',
    key: 'name',
    width: 150
  },
  {
    title: '数据库类型',
    dataIndex: 'dbType',
    key: 'dbType',
    width: 120
  },
  {
    title: '主机',
    dataIndex: 'host',
    key: 'host',
    width: 150
  },
  {
    title: '端口',
    dataIndex: 'port',
    key: 'port',
    width: 80
  },
  {
    title: '数据库名',
    dataIndex: 'databaseName',
    key: 'databaseName',
    width: 120
  },
  {
    title: '状态',
    dataIndex: 'enabled',
    key: 'enabled',
    width: 80
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 240,
    fixed: 'right' as const
  }
]

// ==================== 状态定义 ====================

// 查询参数
const queryParams = reactive<DbConnectionQueryParams>({
  pageNumber: 1,
  pageSize: 10,
  name: '',
  dbType: undefined
})

// 表格数据
const tableData = ref<DbConnection[]>([])
const loading = ref(false)
const total = ref(0)

// 选中行
const selectedRowKeys = ref<number[]>([])
const selectedRows = ref<DbConnection[]>([])

// 弹窗状态
const formModalVisible = ref(false)
const formModalMode = ref<'create' | 'edit'>('create')
const editingConnection = ref<DbConnection | null>(null)

// 监控弹窗
const monitorModalVisible = ref(false)
const monitorConnection = ref<DbConnection | null>(null)

// 测试连接状态
const testingIds = ref<Set<number>>(new Set())

// ==================== 计算属性 ====================

// 分页配置
const pagination = computed(() => ({
  current: queryParams.pageNumber,
  pageSize: queryParams.pageSize,
  total: total.value,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`,
  pageSizeOptions: ['10', '20', '50', '100']
}))

// 行选择配置
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: number[], rows: DbConnection[]) => {
    selectedRowKeys.value = keys
    selectedRows.value = rows
  }
}))

// ==================== 数据加载 ====================

/**
 * 加载连接列表
 */
const loadData = async () => {
  loading.value = true
  try {
    const { data } = await dbConnectionApi.list({
      pageNumber: queryParams.pageNumber,
      pageSize: queryParams.pageSize,
      name: queryParams.name || undefined,
      dbType: queryParams.dbType || undefined
    })
    
    const result = data as PageResult<DbConnection>
    tableData.value = result.records
    total.value = result.totalRow
  } catch (error) {
    console.error('加载连接列表失败:', error)
    message.error('加载连接列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  queryParams.pageNumber = 1
  loadData()
}

/**
 * 重置搜索
 */
const handleReset = () => {
  queryParams.pageNumber = 1
  queryParams.name = ''
  queryParams.dbType = undefined
  loadData()
}

/**
 * 表格变化（分页、排序）
 */
const handleTableChange = (pag: any) => {
  queryParams.pageNumber = pag.current
  queryParams.pageSize = pag.pageSize
  loadData()
}

// ==================== 连接操作 ====================

/**
 * 新增连接
 */
const handleCreate = () => {
  formModalMode.value = 'create'
  editingConnection.value = null
  formModalVisible.value = true
}

/**
 * 编辑连接
 */
const handleEdit = (record: DbConnection) => {
  formModalMode.value = 'edit'
  editingConnection.value = { ...record }
  formModalVisible.value = true
}

/**
 * 删除连接
 */
const handleDelete = async (record: DbConnection) => {
  try {
    await dbConnectionApi.delete(record.id)
    message.success('删除成功')
    loadData()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

/**
 * 批量删除
 */
const handleBatchDelete = async () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要删除的连接')
    return
  }
  
  try {
    await dbConnectionApi.batchDelete(selectedRowKeys.value)
    message.success('批量删除成功')
    selectedRowKeys.value = []
    selectedRows.value = []
    loadData()
  } catch (error) {
    console.error('批量删除失败:', error)
  }
}

/**
 * 测试连接
 */
const handleTest = async (record: DbConnection) => {
  testingIds.value.add(record.id)
  try {
    const { data } = await dbConnectionApi.testById(record.id)
    const info = data as DbMonitorInfo
    if (info.status === 'CONNECTED') {
      message.success(`连接成功！版本：${info.version}，响应时间：${info.responseTimeMs}ms`)
    } else {
      message.error(`连接失败：${info.errorMessage}`)
    }
  } catch (error: any) {
    message.error(`测试失败：${error.message || '未知错误'}`)
  } finally {
    testingIds.value.delete(record.id)
  }
}

/**
 * 查看监控
 */
const handleMonitor = (record: DbConnection) => {
  monitorConnection.value = record
  monitorModalVisible.value = true
}

/**
 * 切换启用状态
 */
const handleToggleEnabled = async (record: DbConnection) => {
  try {
    await dbConnectionApi.updateEnabled(record.id, !record.enabled)
    message.success(`${record.enabled ? '禁用' : '启用'}成功`)
    loadData()
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

/**
 * 弹窗提交成功
 */
const handleFormModalSuccess = () => {
  formModalVisible.value = false
  loadData()
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadData()
})
</script>

<template>
  <a-layout class="db-connection-list">
    <a-layout-content>
      <!-- 搜索区域 -->
      <a-card :bordered="false" class="search-card">
        <a-form layout="inline" :model="queryParams">
          <a-form-item label="连接名称">
            <a-input
              v-model:value="queryParams.name"
              placeholder="请输入连接名称"
              allow-clear
              style="width: 200px"
              @press-enter="handleSearch"
            />
          </a-form-item>
          
          <a-form-item label="数据库类型">
            <a-select
              v-model:value="queryParams.dbType"
              placeholder="请选择类型"
              allow-clear
              style="width: 150px"
              :options="DbTypeOptions"
            />
          </a-form-item>
          
          <a-form-item>
            <a-space>
              <a-button type="primary" @click="handleSearch">
                <template #icon><SearchOutlined /></template>
                搜索
              </a-button>
              <a-button @click="handleReset">
                <template #icon><ReloadOutlined /></template>
                重置
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </a-card>

      <!-- 表格区域 -->
      <a-card :bordered="false" class="table-card">
        <!-- 工具栏 -->
        <div class="table-toolbar">
          <a-space>
            <a-button type="primary" @click="handleCreate">
              <template #icon><PlusOutlined /></template>
              新增连接
            </a-button>
            <a-popconfirm
              title="确定要删除选中的连接吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleBatchDelete"
            >
              <a-button 
                danger 
                :disabled="selectedRowKeys.length === 0"
              >
                <template #icon><DeleteOutlined /></template>
                批量删除
              </a-button>
            </a-popconfirm>
          </a-space>
          
          <a-button @click="loadData">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
        </div>

        <!-- 表格 -->
        <a-table
          :columns="columns"
          :data-source="tableData"
          :loading="loading"
          :pagination="pagination"
          :row-selection="rowSelection"
          :row-key="(record: DbConnection) => record.id"
          :scroll="{ x: 1400 }"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <!-- 数据库类型列 -->
            <template v-if="column.key === 'dbType'">
              <a-tag :color="getDbTypeInfo(record.dbType).color">
                <DatabaseOutlined style="margin-right: 4px" />
                {{ getDbTypeInfo(record.dbType).label }}
              </a-tag>
            </template>

            <!-- 状态列 -->
            <template v-if="column.key === 'enabled'">
              <a-tag v-if="record.enabled" color="success">
                <CheckCircleOutlined /> 启用
              </a-tag>
              <a-tag v-else color="error">
                <CloseCircleOutlined /> 禁用
              </a-tag>
            </template>

            <!-- 操作列 -->
            <template v-if="column.key === 'action'">
              <a-space>
                <a-button 
                  type="link" 
                  size="small" 
                  :loading="testingIds.has(record.id)"
                  @click="handleTest(record)"
                >
                  <template #icon><ApiOutlined /></template>
                  测试
                </a-button>
                <a-button type="link" size="small" @click="handleMonitor(record)">
                  <template #icon><DashboardOutlined /></template>
                  监控
                </a-button>
                <a-button type="link" size="small" @click="handleEdit(record)">
                  <template #icon><EditOutlined /></template>
                  编辑
                </a-button>
                <a-popconfirm
                  :title="`确定要删除连接 '${record.name}' 吗？`"
                  ok-text="确定"
                  cancel-text="取消"
                  @confirm="handleDelete(record)"
                >
                  <a-button type="link" size="small" danger>
                    <template #icon><DeleteOutlined /></template>
                    删除
                  </a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-card>

      <!-- 表单弹窗 -->
      <DbConnectionFormModal
        v-model:visible="formModalVisible"
        :mode="formModalMode"
        :connection="editingConnection"
        @success="handleFormModalSuccess"
      />

      <!-- 监控弹窗 -->
      <DbMonitorModal
        v-model:visible="monitorModalVisible"
        :connection="monitorConnection"
      />
    </a-layout-content>
  </a-layout>
</template>

<style scoped lang="less">
.db-connection-list {
  background: transparent;
  
  .search-card {
    margin-bottom: 16px;
  }

  .table-card {
    .table-toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
    }
  }
}
</style>
