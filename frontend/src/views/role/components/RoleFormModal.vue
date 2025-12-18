<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import { message } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { roleApi } from '@/api/role'
import type { Role, RoleCreateRequest } from '@/types/role'
import { formRules, createAsyncValidator } from '@/utils/formRules'

// ==================== Props & Emits ====================

interface Props {
  visible: boolean
  mode: 'create' | 'edit'
  role: Role | null
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
  code: '',
  name: '',
  description: '',
  sort: 0,
  enabled: true
})

// ==================== 计算属性 ====================

const isEdit = computed(() => props.mode === 'edit')
const modalTitle = computed(() => isEdit.value ? '编辑角色' : '新增角色')

// ==================== 表单校验规则 ====================

// 编码唯一性校验
const checkCodeUnique = createAsyncValidator(
  async (value: string) => {
    if (isEdit.value) return true // 编辑模式不校验编码
    const { data } = await roleApi.checkCode(value)
    return data === true
  },
  '角色编码已存在'
)

const rules: Record<string, Rule[]> = {
  code: [
    formRules.required('请输入角色编码'),
    formRules.length(2, 30, '角色编码长度必须在 2-30 个字符之间'),
    formRules.pattern(/^[A-Z][A-Z0-9_]*$/, '角色编码必须以大写字母开头，只能包含大写字母、数字和下划线'),
    checkCodeUnique
  ],
  name: [
    formRules.required('请输入角色名称'),
    formRules.length(2, 50, '角色名称长度必须在 2-50 个字符之间')
  ],
  description: [
    formRules.maxLength(200, '描述长度不能超过 200 个字符')
  ],
  sort: [
    formRules.required('请输入排序号')
  ]
}

// ==================== 监听弹窗显示 ====================

watch(() => props.visible, (val) => {
  if (val) {
    if (props.mode === 'edit' && props.role) {
      // 编辑模式：填充数据
      formState.code = props.role.code
      formState.name = props.role.name
      formState.description = props.role.description || ''
      formState.sort = props.role.sort
      formState.enabled = props.role.enabled
    } else {
      // 新增模式：重置表单
      resetForm()
    }
  }
})

// ==================== 方法 ====================

/**
 * 重置表单
 */
const resetForm = () => {
  formState.code = ''
  formState.name = ''
  formState.description = ''
  formState.sort = 0
  formState.enabled = true
  formRef.value?.clearValidate()
}

/**
 * 关闭弹窗
 */
const handleCancel = () => {
  emit('update:visible', false)
  resetForm()
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitting.value = true

    if (isEdit.value && props.role) {
      // 编辑角色
      await roleApi.update(props.role.id, {
        name: formState.name,
        description: formState.description || undefined,
        sort: formState.sort,
        enabled: formState.enabled
      })
      message.success('角色更新成功')
    } else {
      // 新增角色
      const createData: RoleCreateRequest = {
        code: formState.code,
        name: formState.name,
        description: formState.description || undefined,
        sort: formState.sort,
        enabled: formState.enabled
      }
      await roleApi.create(createData)
      message.success('角色创建成功')
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
    width="500px"
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
      <a-form-item label="角色编码" name="code">
        <a-input
          v-model:value="formState.code"
          placeholder="请输入角色编码（如：MANAGER）"
          :maxlength="30"
          :disabled="isEdit"
          show-count
        />
      </a-form-item>

      <a-form-item label="角色名称" name="name">
        <a-input
          v-model:value="formState.name"
          placeholder="请输入角色名称"
          :maxlength="50"
          show-count
        />
      </a-form-item>

      <a-form-item label="描述" name="description">
        <a-textarea
          v-model:value="formState.description"
          placeholder="请输入角色描述（选填）"
          :maxlength="200"
          :rows="3"
          show-count
        />
      </a-form-item>

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
