<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  PlusOutlined,
  ReloadOutlined,
  DeleteOutlined,
  EditOutlined,
  ExclamationCircleOutlined
} from '@ant-design/icons-vue'
import { permissionApi } from '@/api/permission'
import type { Permission } from '@/types/permission'
import { PermissionType, PermissionTypeMap, PermissionTypeColorMap } from '@/types/permission'
import PermissionFormModal from './components/PermissionFormModal.vue'

// ==================== 状态 ====================

const loading = ref(false)
const tableData = ref<Permission[]>([])
const expandedRowKeys = ref<number[]>([])

// 弹窗状态
const modalVisible = ref(false)
const modalMode = ref<'create' | 'edit'>('create')
const currentPermission = ref<Permission | null>(null)
const parentIdForCreate = ref<number | undefined>(undefined)

// ==================== 表格列配置 ====================

const columns = [
  {
    title: '权限名称',
    dataIndex: 'name',
    key: 'name',
    width: 250
  },
  {
    title: '类型',
    dataIndex: 'type',
    key: 'type',
    width: 100,
    align: 'center' as const
  },
  {
    title: '权限编码',
    dataIndex: 'code',
    key: 'code',
    width: 200
  },
  {
    title: '路径',
    dataIndex: 'path',
    key: 'path',
    ellipsis: true
  },
  {
    title: '图标',
    dataIndex: 'icon',
    key: 'icon',
    width: 150
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
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right' as const
  }
]

// ==================== 方法 ====================

/**
 * 获取权限树
 */
const fetchPermissions = async () => {
  loading.value = true
  try {
    const { data } = await permissionApi.getTree()
    tableData.value = data
  } catch (error) {
    console.error('获取权限列表失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 刷新
 */
const handleRefresh = () => {
  fetchPermissions()
}

/**
 * 新增权限（顶级）
 */
const handleAdd = () => {
  modalMode.value = 'create'
  currentPermission.value = null
  parentIdForCreate.value = 0
  modalVisible.value = true
}

/**
 * 新增子权限
 */
const handleAddChild = (record: Permission) => {
  modalMode.value = 'create'
  currentPermission.value = null
  parentIdForCreate.value = record.id
  modalVisible.value = true
}

/**
 * 编辑权限
 */
const handleEdit = (record: Permission) => {
  modalMode.value = 'edit'
  currentPermission.value = record
  parentIdForCreate.value = undefined
  modalVisible.value = true
}

/**
 * 切换权限状态
 */
const handleToggleEnabled = async (record: Permission) => {
  try {
    await permissionApi.updateEnabled(record.id, !record.enabled)
    message.success(record.enabled ? '已禁用' : '已启用')
    fetchPermissions()
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

/**
 * 删除权限
 */
const handleDelete = (record: Permission) => {
  Modal.confirm({
    title: '确认删除',
    icon: ExclamationCircleOutlined,
    content: `确定要删除权限"${record.name}"吗？这将同时删除所有子权限！`,
    okText: '确定',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      try {
        await permissionApi.delete(record.id)
        message.success('删除成功')
        fetchPermissions()
      } catch (error) {
        console.error('删除失败:', error)
      }
    }
  })
}

/**
 * 弹窗成功回调
 */
const handleModalSuccess = () => {
  fetchPermissions()
}

/**
 * 展开/折叠行
 */
const onExpand = (expanded: boolean, record: Permission) => {
  if (expanded) {
    expandedRowKeys.value.push(record.id)
  } else {
    expandedRowKeys.value = expandedRowKeys.value.filter(k => k !== record.id)
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  fetchPermissions()
})
</script>

<template>
  <div class="permission-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">权限管理</h1>
      <p class="page-desc">管理系统菜单、按钮和接口权限</p>
    </div>

    <!-- 表格 -->
    <a-card :bordered="false" class="table-card">
      <!-- 工具栏 -->
      <div class="table-toolbar">
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            新增顶级权限
          </a-button>
          <a-button @click="handleRefresh">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
        </a-space>
      </div>

      <!-- 表格 -->
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="false"
        :row-key="(record: Permission) => record.id"
        :scroll="{ x: 1200 }"
        v-model:expandedRowKeys="expandedRowKeys"
        @expand="onExpand"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'type'">
            <a-tag :color="PermissionTypeColorMap[record.type]">
              {{ PermissionTypeMap[record.type] }}
            </a-tag>
          </template>
          
          <template v-else-if="column.key === 'icon'">
            <component 
              v-if="record.icon && record.type === PermissionType.MENU" 
              :is="record.icon" 
              style="margin-right: 8px"
            />
            <span>{{ record.icon }}</span>
          </template>
          
          <template v-else-if="column.key === 'enabled'">
            <a-switch
              :checked="record.enabled"
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
                @click="handleAddChild(record)"
              >
                <template #icon><PlusOutlined /></template>
                添加下级
              </a-button>
              <a-button
                type="link"
                size="small"
                @click="handleEdit(record)"
              >
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
              <a-popconfirm
                title="确定要删除吗？"
                @confirm="handleDelete(record)"
              >
                <a-button
                  type="link"
                  size="small"
                  danger
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
    <PermissionFormModal
      v-model:visible="modalVisible"
      :mode="modalMode"
      :permission="currentPermission"
      :parent-id="parentIdForCreate"
      :tree-data="tableData"
      @success="handleModalSuccess"
    />
  </div>
</template>

<style scoped lang="less">
.permission-list {
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
  
  .table-card {
    border-radius: 8px;
    
    .table-toolbar {
      margin-bottom: 16px;
    }
  }
}
</style>
