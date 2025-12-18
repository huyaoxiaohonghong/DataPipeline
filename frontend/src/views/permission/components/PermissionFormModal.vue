<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import { message } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { permissionApi } from '@/api/permission'
import type { Permission, PermissionCreateRequest } from '@/types/permission'
import { PermissionType } from '@/types/permission'
import { formRules, createAsyncValidator } from '@/utils/formRules'

// ==================== Props & Emits ====================

interface Props {
  visible: boolean
  mode: 'create' | 'edit'
  permission: Permission | null
  parentId?: number
  treeData: Permission[]
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  success: []
}>()

// ==================== 状态 ====================

const formRef = ref()
const submitting = ref(false)

// 表单数据
const formState = reactive({
  type: PermissionType.MENU,
  parentId: 0,
  name: '',
  code: '',
  path: '',
  icon: '',
  sort: 0,
  enabled: true
})

// ==================== 计算属性 ====================

const isEdit = computed(() => props.mode === 'edit')
const modalTitle = computed(() => isEdit.value ? '编辑权限' : '新增权限')

// 上级权限选项（用于 TreeSelect）
const treeSelectData = computed(() => {
  const root = {
    id: 0,
    name: '顶级权限',
    children: props.treeData
  }
  return [root]
})

// ==================== 表单校验规则 ====================

// 编码唯一性校验
const checkCodeUnique = createAsyncValidator(
  async (value: string) => {
    if (isEdit.value) return true // 编辑模式不校验编码
    const { data } = await permissionApi.checkCode(value)
    return data === true
  },
  '权限编码已存在'
)

const rules = computed<Record<string, Rule[]>>(() => {
  const baseRules: Record<string, Rule[]> = {
    name: [
      formRules.required('请输入权限名称'),
      formRules.length(2, 50, '权限名称长度必须在 2-50 个字符之间')
    ],
    code: [
      formRules.required('请输入权限编码'),
      formRules.length(2, 50, '权限编码长度必须在 2-50 个字符之间'),
      formRules.pattern(/^[A-Z][A-Z0-9_:]*$/, '权限编码必须以大写字母开头，只能包含大写字母、数字、下划线和冒号'),
      checkCodeUnique
    ],
    type: [
      formRules.required('请选择权限类型')
    ],
    sort: [
      formRules.required('请输入排序号')
    ]
  }
  
  // 根据类型添加特定规则
  if (formState.type === PermissionType.MENU || formState.type === PermissionType.API) {
    if (formState.type === PermissionType.API) {
      baseRules.path = [formRules.required('请输入接口路径')]
    }
  }
  
  return baseRules
})

// ==================== 监听弹窗显示 ====================

watch(() => props.visible, (val) => {
  if (val) {
    if (props.mode === 'edit' && props.permission) {
      // 编辑模式
      formState.type = props.permission.type
      formState.parentId = props.permission.parentId
      formState.name = props.permission.name
      formState.code = props.permission.code
      formState.path = props.permission.path || ''
      formState.icon = props.permission.icon || ''
      formState.sort = props.permission.sort
      formState.enabled = props.permission.enabled
    } else {
      // 新增模式
      resetForm()
      // 如果指定了父级ID，则默认选中
      if (props.parentId !== undefined) {
        formState.parentId = props.parentId
      }
      // 根据父级类型自动选择类型
      if (props.parentId && props.parentId > 0) {
        // 查找父级
        const findParent = (list: Permission[]): Permission | undefined => {
          for (const item of list) {
            if (item.id === props.parentId) return item
            if (item.children) {
              const found = findParent(item.children)
              if (found) return found
            }
          }
          return undefined
        }
        const parent = findParent(props.treeData)
        if (parent) {
          if (parent.type === PermissionType.MENU) {
            // 菜单下通常是菜单或按钮
            formState.type = PermissionType.BUTTON // 默认为按钮，用户可改为菜单
          }
        }
      }
    }
  }
})

// ==================== 方法 ====================

const resetForm = () => {
  formState.type = PermissionType.MENU
  formState.parentId = 0
  formState.name = ''
  formState.code = ''
  formState.path = ''
  formState.icon = ''
  formState.sort = 0
  formState.enabled = true
  formRef.value?.clearValidate()
}

const handleCancel = () => {
  emit('update:visible', false)
  resetForm()
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitting.value = true

    if (isEdit.value && props.permission) {
      // 编辑
      await permissionApi.update(props.permission.id, {
        name: formState.name,
        parentId: formState.parentId,
        path: formState.path || undefined,
        icon: formState.icon || undefined,
        sort: formState.sort,
        enabled: formState.enabled
      })
      message.success('权限更新成功')
    } else {
      // 新增
      const createData: PermissionCreateRequest = {
        code: formState.code,
        name: formState.name,
        type: formState.type,
        parentId: formState.parentId,
        path: formState.path || undefined,
        icon: formState.icon || undefined,
        sort: formState.sort,
        enabled: formState.enabled
      }
      await permissionApi.create(createData)
      message.success('权限创建成功')
    }

    emit('success')
    handleCancel()
  } catch (error: any) {
    if (!error.errorFields) {
      console.error('提交失败:', error)
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <a-modal
    :open="visible"
    :title="modalTitle"
    :confirm-loading="submitting"
    :mask-closable="false"
    :destroy-on-close="true"
    width="600px"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formState"
      :rules="rules"
      :label-col="{ span: 5 }"
      :wrapper-col="{ span: 18 }"
      autocomplete="off"
    >
      <a-form-item label="上级权限" name="parentId">
        <a-tree-select
          v-model:value="formState.parentId"
          :tree-data="treeSelectData"
          :field-names="{ children: 'children', label: 'name', value: 'id' }"
          show-search
          style="width: 100%"
          :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
          placeholder="请选择上级权限"
          allow-clear
          tree-default-expand-all
        />
      </a-form-item>

      <a-form-item label="权限类型" name="type">
        <a-radio-group v-model:value="formState.type" :disabled="isEdit">
          <a-radio-button :value="PermissionType.MENU">菜单</a-radio-button>
          <a-radio-button :value="PermissionType.BUTTON">按钮</a-radio-button>
          <a-radio-button :value="PermissionType.API">接口</a-radio-button>
        </a-radio-group>
      </a-form-item>

      <a-form-item label="权限名称" name="name">
        <a-input
          v-model:value="formState.name"
          placeholder="请输入权限名称"
          :maxlength="50"
          show-count
        />
      </a-form-item>

      <a-form-item label="权限编码" name="code">
        <a-input
          v-model:value="formState.code"
          placeholder="请输入权限编码（如：MENU:USER）"
          :maxlength="50"
          :disabled="isEdit"
          show-count
        />
      </a-form-item>

      <template v-if="formState.type === PermissionType.MENU || formState.type === PermissionType.API">
        <a-form-item 
          :label="formState.type === PermissionType.API ? '接口路径' : '路由路径'" 
          name="path"
        >
          <a-input
            v-model:value="formState.path"
            :placeholder="formState.type === PermissionType.API ? '/api/v1/users/**' : '/users'"
            :maxlength="200"
          />
        </a-form-item>
      </template>

      <template v-if="formState.type === PermissionType.MENU">
        <a-form-item label="图标" name="icon">
          <a-input
            v-model:value="formState.icon"
            placeholder="请输入 Ant Design 图标名称（如：UserOutlined）"
            :maxlength="50"
          />
        </a-form-item>
      </template>

      <a-form-item label="排序" name="sort">
        <a-input-number
          v-model:value="formState.sort"
          :min="0"
          :max="999"
          style="width: 100%"
        />
      </a-form-item>

      <a-form-item label="状态" name="enabled">
        <a-switch
          v-model:checked="formState.enabled"
          checked-children="启用"
          un-checked-children="禁用"
        />
      </a-form-item>
    </a-form>

    <template #footer>
      <a-button @click="handleCancel">取消</a-button>
      <a-button type="primary" :loading="submitting" @click="handleSubmit">
        {{ isEdit ? '保存' : '创建' }}
      </a-button>
    </template>
  </a-modal>
</template>
