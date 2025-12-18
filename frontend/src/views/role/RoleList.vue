<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  SearchOutlined,
  ReloadOutlined,
  DeleteOutlined,
  EditOutlined,
  ExclamationCircleOutlined,
  SafetyCertificateOutlined
} from '@ant-design/icons-vue'
import { roleApi } from '@/api/role'
import type { Role, RoleQueryParams } from '@/types/role'
import RoleFormModal from './components/RoleFormModal.vue'
import PermissionAssignModal from './components/PermissionAssignModal.vue'

// ==================== 状态 ====================

const loading = ref(false)
const tableData = ref<Role[]>([])
const selectedRowKeys = ref<number[]>([])
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

// 搜索条件
const searchForm = reactive<RoleQueryParams>({
  name: '',
  code: '',
  enabled: undefined
})

// 弹窗状态
const modalVisible = ref(false)
const modalMode = ref<'create' | 'edit'>('create')
const currentRole = ref<Role | null>(null)

// 权限分配弹窗
const permModalVisible = ref(false)
const processingRole = ref<Role | null>(null)


// ==================== 表格列配置 ====================

const columns = [
  {
    title: '角色编码',
    dataIndex: 'code',
    key: 'code',
    width: 150
  },
  {
    title: '角色名称',
    dataIndex: 'name',
    key: 'name',
    width: 150
  },
  {
    title: '描述',
    dataIndex: 'description',
    key: 'description',
    ellipsis: true
  },
  {
    title: '排序',
    dataIndex: 'sort',
    key: 'sort',
    width: 80,
    align: 'center' as const
  },
  {
    title: '状态',
    dataIndex: 'enabled',
    key: 'enabled',
    width: 100,
    align: 'center' as const
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
    width: 250,
    fixed: 'right' as const
  }
]


// ==================== 方法 ====================

/**
 * 获取角色列表
 */
const fetchRoles = async () => {
  loading.value = true
  try {
    const { data } = await roleApi.getPage({
      pageNumber: pagination.current,
      pageSize: pagination.pageSize,
      ...searchForm
    })
    tableData.value = data.records
    pagination.total = data.totalRow
  } catch (error) {
    console.error('获取角色列表失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  pagination.current = 1
  fetchRoles()
}

/**
 * 重置搜索
 */
const handleReset = () => {
  searchForm.name = ''
  searchForm.code = ''
  searchForm.enabled = undefined
  pagination.current = 1
  fetchRoles()
}

/**
 * 表格分页变化
 */
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchRoles()
}

/**
 * 新增角色
 */
const handleAdd = () => {
  modalMode.value = 'create'
  currentRole.value = null
  modalVisible.value = true
}

/**
 * 编辑角色
 */
const handleEdit = (record: Role) => {
  modalMode.value = 'edit'
  currentRole.value = record
  modalVisible.value = true
}

/**
 * 分配权限
 */
const handleAssignPermission = (record: Role) => {
  processingRole.value = record
  permModalVisible.value = true
}

/**
 * 切换角色状态
 */
const handleToggleEnabled = async (record: Role) => {
  try {
    await roleApi.updateEnabled(record.id, !record.enabled)
    message.success(record.enabled ? '已禁用' : '已启用')
    fetchRoles()
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

/**
 * 删除角色
 */
const handleDelete = (record: Role) => {
  Modal.confirm({
    title: '确认删除',
    icon: ExclamationCircleOutlined,
    content: `确定要删除角色"${record.name}"吗？`,
    okText: '确定',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      try {
        await roleApi.delete(record.id)
        message.success('删除成功')
        fetchRoles()
      } catch (error) {
        console.error('删除失败:', error)
      }
    }
  })
}

/**
 * 批量删除
 */
const handleBatchDelete = () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要删除的角色')
    return
  }

  Modal.confirm({
    title: '确认删除',
    icon: ExclamationCircleOutlined,
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个角色吗？`,
    okText: '确定',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      try {
        await roleApi.batchDelete(selectedRowKeys.value)
        message.success('批量删除成功')
        selectedRowKeys.value = []
        fetchRoles()
      } catch (error) {
        console.error('批量删除失败:', error)
      }
    }
  })
}

/**
 * 弹窗成功回调
 */
const handleModalSuccess = () => {
  fetchRoles()
}

/**
 * 判断是否为内置角色
 */
const isBuiltInRole = (code: string) => {
  return ['ADMIN', 'USER', 'GUEST'].includes(code)
}

/**
 * 选择变化
 */
const onSelectChange = (keys: number[]) => {
  selectedRowKeys.value = keys
}

// ==================== 生命周期 ====================

onMounted(() => {
  fetchRoles()
})
</script>

<template>
  <div class="role-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">角色管理</h1>
      <p class="page-desc">管理系统角色及其权限配置</p>
    </div>

    <!-- 搜索栏 -->
    <a-card :bordered="false" class="search-card">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="角色名称">
          <a-input
            v-model:value="searchForm.name"
            placeholder="请输入角色名称"
            allow-clear
            style="width: 200px"
            @press-enter="handleSearch"
          />
        </a-form-item>
        <a-form-item label="角色编码">
          <a-input
            v-model:value="searchForm.code"
            placeholder="请输入角色编码"
            allow-clear
            style="width: 200px"
            @press-enter="handleSearch"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.enabled"
            placeholder="全部"
            allow-clear
            style="width: 120px"
          >
            <a-select-option :value="true">启用</a-select-option>
            <a-select-option :value="false">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 表格 -->
    <a-card :bordered="false" class="table-card">
      <!-- 工具栏 -->
      <div class="table-toolbar">
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            新增角色
          </a-button>
          <a-button danger :disabled="selectedRowKeys.length === 0" @click="handleBatchDelete">
            <template #icon><DeleteOutlined /></template>
            批量删除
          </a-button>
        </a-space>
      </div>

      <!-- 表格 -->
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        :row-selection="{
          selectedRowKeys,
          onChange: onSelectChange,
          getCheckboxProps: (record: Role) => ({
            disabled: isBuiltInRole(record.code)
          })
        }"
        :row-key="(record: Role) => record.id"
        :scroll="{ x: 1000 }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'enabled'">
            <a-switch
              :checked="record.enabled"
              :disabled="isBuiltInRole(record.code)"
              checked-children="启用"
              un-checked-children="禁用"
              @change="() => handleToggleEnabled(record)"
            />
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                type="link"
                size="small"
                :disabled="isBuiltInRole(record.code)"
                @click="handleEdit(record)"
              >
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
              <a-button
                type="link"
                size="small"
                @click="handleAssignPermission(record)"
              >
                <template #icon><SafetyCertificateOutlined /></template>
                权限
              </a-button>
              <a-popconfirm
                title="确定要删除此角色吗？"
                :disabled="isBuiltInRole(record.code)"
                @confirm="handleDelete(record)"
              >
                <a-button
                  type="link"
                  size="small"
                  danger
                  :disabled="isBuiltInRole(record.code)"
                >
                  <template #icon><DeleteOutlined /></template>
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑弹窗 -->
    <RoleFormModal
      v-model:visible="modalVisible"
      :mode="modalMode"
      :role="currentRole"
      @success="handleModalSuccess"
    />

    <!-- 权限分配弹窗 -->
    <PermissionAssignModal
      v-model:visible="permModalVisible"
      :role-id="processingRole?.id || null"
      @success="handleModalSuccess"
    />
  </div>
</template>

<style scoped lang="less">
.role-list {
  .page-header {
    margin-bottom: 24px;
    
    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: rgba(0, 0, 0, 0.85);
      margin: 0 0 8px 0;
    }
    
    .page-desc {
      color: rgba(0, 0, 0, 0.45);
      margin: 0;
    }
  }
  
  .search-card {
    margin-bottom: 16px;
    border-radius: 8px;
  }
  
  .table-card {
    border-radius: 8px;
    
    .table-toolbar {
      margin-bottom: 16px;
    }
  }
}
</style>
