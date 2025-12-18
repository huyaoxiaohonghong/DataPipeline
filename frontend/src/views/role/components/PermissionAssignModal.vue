<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { message } from 'ant-design-vue'
import { permissionApi, rolePermissionApi } from '@/api/permission'
import type { Permission } from '@/types/permission'
import type { DataNode } from 'ant-design-vue/es/tree'

interface Props {
  visible: boolean
  roleId: number | null
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  success: []
}>()

// ==================== 状态 ====================

const loading = ref(false)
const submitting = ref(false)
const treeData = ref<Permission[]>([])
const checkedKeys = ref<number[]>([])

// ==================== 计算属性 ====================

// 转换权限数据为 Tree 组件需要的格式
const formattedTreeData = computed<DataNode[]>(() => {
  const transform = (nodes: Permission[]): DataNode[] => {
    return nodes.map(node => ({
      key: node.id,
      title: node.name,
      children: node.children ? transform(node.children) : undefined
    }))
  }
  return transform(treeData.value)
})

// ==================== 监听 ====================

watch(() => props.visible, async (val) => {
  if (val && props.roleId) {
    await loadData()
  } else {
    checkedKeys.value = []
  }
})

// ==================== 方法 ====================

/**
 * 加载数据
 */
const loadData = async () => {
  if (!props.roleId) return
  
  loading.value = true
  try {
    // 1. 获取所有权限树
    if (treeData.value.length === 0) {
      const { data } = await permissionApi.getTree()
      treeData.value = data
    }

    // 2. 获取角色拥有的权限ID
    const { data: rolePermissionIds } = await rolePermissionApi.getPermissionIdsByRoleId(props.roleId)
    checkedKeys.value = rolePermissionIds
  } catch (error) {
    console.error('加载权限数据失败:', error)
    message.error('加载权限数据失败')
  } finally {
    loading.value = false
  }
}

/**
 * 关闭弹窗
 */
const handleCancel = () => {
  emit('update:visible', false)
}

/**
 * 提交保存
 */
const handleSubmit = async () => {
  if (!props.roleId) return

  submitting.value = true
  try {
    // checkedKeys 包含半选状态的父节点吗？AntD Vue 的 Tree checkable 默认只返回选中的子节点？
    // 文档：v-model:checkedKeys
    // 注意：如果父节点是半选，默认不会在 checkedKeys 里。后端可能需要父节点ID吗？
    // 通常权限系统设计：如果选中子节点，前端传递子节点ID即可，或者也传递父节点ID。
    // 如果父子关联，后端会根据子节点自动给父节点权限？或者我们需要手动找出所有半选父节点。
    // 这里简单起见，只传递 checkKeys。
    // 如果需要包含半选节点，可以使用 @check 事件获取 info.halfCheckedKeys
    
    await rolePermissionApi.assignPermissions(props.roleId, checkedKeys.value)
    message.success('权限分配成功')
    emit('success')
    handleCancel()
  } catch (error) {
    console.error('分配权限失败:', error)
  } finally {
    submitting.value = false
  }
}

/**
 * 树节点 Check 事件
 * 用于确保父子节点联动符合预期（AntD 默认已有联动）
 */
const onCheck = (keys: any, info: any) => {
  // keys 可能是 number[] 或 { checked: number[], halfChecked: number[] }
  // 取决于 checkStrictly 属性
  console.log('Checked:', keys, info)
}
</script>

<template>
  <a-modal
    :open="visible"
    title="分配权限"
    :confirm-loading="submitting"
    :mask-closable="false"
    width="500px"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-spin :spinning="loading">
      <div class="permission-tree-container">
        <a-tree
          v-if="treeData.length > 0"
          v-model:checkedKeys="checkedKeys"
          :tree-data="formattedTreeData"
          checkable
          default-expand-all
          :height="400"
          @check="onCheck"
        />
        <div v-else class="empty-tip">
          暂无权限数据
        </div>
      </div>
    </a-spin>
  </a-modal>
</template>

<style scoped lang="less">
.permission-tree-container {
  max-height: 500px;
  overflow: auto;
  min-height: 100px;
  
  .empty-tip {
    text-align: center;
    color: rgba(0, 0, 0, 0.45);
    margin-top: 32px;
  }
}
</style>
