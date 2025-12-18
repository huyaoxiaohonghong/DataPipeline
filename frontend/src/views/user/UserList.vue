<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  SearchOutlined,
  ReloadOutlined,
  EditOutlined,
  DeleteOutlined,
  ExclamationCircleOutlined
} from '@ant-design/icons-vue'
import { userApi } from '@/api/user'
import type { User, UserQueryParams } from '@/types/user'
import { RoleOptions, getRoleInfo } from '@/types/user'
import type { PageResult } from '@/types'
import UserFormModal from './components/UserFormModal.vue'

// ==================== 表格配置 ====================

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80,
    fixed: 'left' as const
  },
  {
    title: '用户名',
    dataIndex: 'username',
    key: 'username',
    width: 150
  },
  {
    title: '邮箱',
    dataIndex: 'email',
    key: 'email',
    width: 220,
    ellipsis: true
  },
  {
    title: '角色',
    dataIndex: 'role',
    key: 'role',
    width: 120
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
    width: 160,
    fixed: 'right' as const
  }
]

// ==================== 状态定义 ====================

// 查询参数
const queryParams = reactive<UserQueryParams>({
  pageNumber: 1,
  pageSize: 10,
  username: '',
  role: undefined
})

// 表格数据
const tableData = ref<User[]>([])
const loading = ref(false)
const total = ref(0)

// 选中行
const selectedRowKeys = ref<number[]>([])
const selectedRows = ref<User[]>([])

// 弹窗状态
const modalVisible = ref(false)
const modalMode = ref<'create' | 'edit'>('create')
const editingUser = ref<User | null>(null)

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
  onChange: (keys: number[], rows: User[]) => {
    selectedRowKeys.value = keys
    selectedRows.value = rows
  }
}))

// ==================== 数据加载 ====================

/**
 * 加载用户列表
 */
const loadData = async () => {
  loading.value = true
  try {
    const { data } = await userApi.list({
      pageNumber: queryParams.pageNumber,
      pageSize: queryParams.pageSize,
      username: queryParams.username || undefined,
      role: queryParams.role || undefined
    })
    
    const result = data as PageResult<User>
    tableData.value = result.records
    total.value = result.totalRow
  } catch (error) {
    console.error('加载用户列表失败:', error)
    message.error('加载用户列表失败')
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
  queryParams.username = ''
  queryParams.role = undefined
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

// ==================== 用户操作 ====================

/**
 * 新增用户
 */
const handleCreate = () => {
  modalMode.value = 'create'
  editingUser.value = null
  modalVisible.value = true
}

/**
 * 编辑用户
 */
const handleEdit = (record: User) => {
  modalMode.value = 'edit'
  editingUser.value = { ...record }
  modalVisible.value = true
}

/**
 * 删除用户
 */
const handleDelete = async (record: User) => {
  try {
    await userApi.delete(record.id)
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
    message.warning('请选择要删除的用户')
    return
  }
  
  try {
    await userApi.batchDelete(selectedRowKeys.value)
    message.success('批量删除成功')
    selectedRowKeys.value = []
    selectedRows.value = []
    loadData()
  } catch (error) {
    console.error('批量删除失败:', error)
  }
}

/**
 * 弹窗提交成功
 */
const handleModalSuccess = () => {
  modalVisible.value = false
  loadData()
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadData()
})
</script>

<template>
  <a-layout class="user-list">
    <a-layout-content>
      <!-- 搜索区域 -->
      <a-card :bordered="false" class="search-card">
        <a-form layout="inline" :model="queryParams">
          <a-form-item label="用户名">
            <a-input
              v-model:value="queryParams.username"
              placeholder="请输入用户名"
              allow-clear
              style="width: 200px"
              @press-enter="handleSearch"
            />
          </a-form-item>
          
          <a-form-item label="角色">
            <a-select
              v-model:value="queryParams.role"
              placeholder="请选择角色"
              allow-clear
              style="width: 150px"
              :options="RoleOptions"
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
              新增用户
            </a-button>
            <a-popconfirm
              title="确定要删除选中的用户吗？"
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
          :row-key="(record: User) => record.id"
          :scroll="{ x: 1100 }"
          @change="handleTableChange"
        >
          <!-- 角色列 -->
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'role'">
              <a-tag :color="getRoleInfo(record.role).color">
                {{ getRoleInfo(record.role).label }}
              </a-tag>
            </template>

            <!-- 操作列 -->
            <template v-if="column.key === 'action'">
              <a-space>
                <a-button type="link" size="small" @click="handleEdit(record)">
                  <template #icon><EditOutlined /></template>
                  编辑
                </a-button>
                <a-popconfirm
                  :title="`确定要删除用户 '${record.username}' 吗？`"
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

      <!-- 用户表单弹窗 -->
      <UserFormModal
        v-model:visible="modalVisible"
        :mode="modalMode"
        :user="editingUser"
        @success="handleModalSuccess"
      />
    </a-layout-content>
  </a-layout>
</template>

<style scoped lang="less">
.user-list {
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
