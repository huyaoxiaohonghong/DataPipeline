<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  SearchOutlined,
  ReloadOutlined,
  EditOutlined,
  DeleteOutlined,
  ExclamationCircleOutlined
} from '@ant-design/icons-vue'
import { userApi } from '@/api'
import type { UserInfo, PageResult } from '@/types'
import UserFormModal from './components/UserFormModal.vue'

// 表格列定义
const columns = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: '用户名', dataIndex: 'username', width: 150 },
  { title: '邮箱', dataIndex: 'email', width: 200 },
  { title: '角色', dataIndex: 'role', width: 120 },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' as const }
]

// 角色选项
const roleOptions = [
  { label: '管理员', value: 'ADMIN' },
  { label: '普通用户', value: 'USER' },
  { label: '访客', value: 'GUEST' }
]

// 搜索表单
const searchForm = reactive({
  username: '',
  role: undefined as string | undefined
})

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 表格数据
const tableData = ref<UserInfo[]>([])
const loading = ref(false)
const selectedRowKeys = ref<number[]>([])

// 弹窗
const modalVisible = ref(false)
const modalTitle = ref('新增用户')
const editingUser = ref<UserInfo | null>(null)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const { data } = await userApi.list({
      pageNumber: pagination.current,
      pageSize: pagination.pageSize,
      username: searchForm.username || undefined,
      role: searchForm.role
    })
    
    const result = data as PageResult<UserInfo>
    tableData.value = result.records
    pagination.total = result.totalRow
  } catch (error) {
    console.error('加载用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置搜索
const handleReset = () => {
  searchForm.username = ''
  searchForm.role = undefined
  pagination.current = 1
  loadData()
}

// 分页变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
}

// 新增用户
const handleAdd = () => {
  modalTitle.value = '新增用户'
  editingUser.value = null
  modalVisible.value = true
}

// 编辑用户
const handleEdit = (record: UserInfo) => {
  modalTitle.value = '编辑用户'
  editingUser.value = { ...record }
  modalVisible.value = true
}

// 删除用户
const handleDelete = (record: UserInfo) => {
  Modal.confirm({
    title: '确认删除',
    icon: h(ExclamationCircleOutlined),
    content: `确定要删除用户 "${record.username}" 吗？`,
    okText: '确定',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      try {
        await userApi.delete(record.id)
        message.success('删除成功')
        loadData()
      } catch (error) {
        console.error('删除失败:', error)
      }
    }
  })
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要删除的用户')
    return
  }

  Modal.confirm({
    title: '确认批量删除',
    icon: h(ExclamationCircleOutlined),
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个用户吗？`,
    okText: '确定',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      try {
        await userApi.batchDelete(selectedRowKeys.value)
        message.success('批量删除成功')
        selectedRowKeys.value = []
        loadData()
      } catch (error) {
        console.error('批量删除失败:', error)
      }
    }
  })
}

// 表单提交成功
const handleModalSuccess = () => {
  modalVisible.value = false
  loadData()
}

// 行选择配置
const rowSelection = {
  selectedRowKeys: selectedRowKeys,
  onChange: (keys: number[]) => {
    selectedRowKeys.value = keys
  }
}

// 获取角色标签颜色
const getRoleColor = (role: string) => {
  const colorMap: Record<string, string> = {
    ADMIN: 'red',
    USER: 'blue',
    GUEST: 'default'
  }
  return colorMap[role] || 'default'
}

// 获取角色标签文本
const getRoleLabel = (role: string) => {
  const item = roleOptions.find((r) => r.value === role)
  return item?.label || role
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="user-management">
    <!-- 搜索区域 -->
    <a-card :bordered="false" class="search-card">
      <a-form layout="inline">
        <a-form-item label="用户名">
          <a-input
            v-model:value="searchForm.username"
            placeholder="请输入用户名"
            allow-clear
            @press-enter="handleSearch"
          />
        </a-form-item>
        <a-form-item label="角色">
          <a-select
            v-model:value="searchForm.role"
            placeholder="请选择角色"
            :options="roleOptions"
            allow-clear
            style="width: 150px"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <SearchOutlined />
              搜索
            </a-button>
            <a-button @click="handleReset">
              <ReloadOutlined />
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
          <a-button type="primary" @click="handleAdd">
            <PlusOutlined />
            新增用户
          </a-button>
          <a-button danger :disabled="selectedRowKeys.length === 0" @click="handleBatchDelete">
            <DeleteOutlined />
            批量删除
          </a-button>
        </a-space>
        <a-button @click="loadData">
          <ReloadOutlined />
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
        :row-key="(record: UserInfo) => record.id"
        :scroll="{ x: 1000 }"
        @change="handleTableChange"
      >
        <!-- 角色列 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'role'">
            <a-tag :color="getRoleColor(record.role)">
              {{ getRoleLabel(record.role) }}
            </a-tag>
          </template>

          <!-- 操作列 -->
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">
                <EditOutlined />
                编辑
              </a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)">
                <DeleteOutlined />
                删除
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 用户表单弹窗 -->
    <UserFormModal
      v-model:visible="modalVisible"
      :title="modalTitle"
      :user="editingUser"
      @success="handleModalSuccess"
    />
  </div>
</template>

<style scoped lang="less">
.user-management {
  .search-card {
    margin-bottom: 16px;
  }

  .table-card {
    .table-toolbar {
      display: flex;
      justify-content: space-between;
      margin-bottom: 16px;
    }
  }
}
</style>
