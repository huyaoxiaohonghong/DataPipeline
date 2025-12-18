<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import { message } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { userApi } from '@/api/user'
import type { User, UserCreateRequest } from '@/types/user'
import { RoleOptions, UserRole } from '@/types/user'
import { formRules, createAsyncValidator } from '@/utils/formRules'

// ==================== Props & Emits ====================

interface Props {
  visible: boolean
  mode: 'create' | 'edit'
  user: User | null
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
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  role: UserRole.USER
})

// ==================== 计算属性 ====================

const isEdit = computed(() => props.mode === 'edit')
const modalTitle = computed(() => isEdit.value ? '编辑用户' : '新增用户')

// ==================== 表单校验规则 ====================

// 用户名唯一性校验
const checkUsernameUnique = createAsyncValidator(
  async (value: string) => {
    if (isEdit.value && props.user?.username === value) return true
    const { data } = await userApi.checkUsername(value)
    return data === true
  },
  '用户名已存在'
)

// 邮箱唯一性校验
const checkEmailUnique = createAsyncValidator(
  async (value: string) => {
    if (!value) return true
    if (isEdit.value && props.user?.email === value) return true
    const { data } = await userApi.checkEmail(value)
    return data === true
  },
  '邮箱已存在'
)

// 新增模式校验规则
const createRules: Record<string, Rule[]> = {
  username: [
    ...formRules.username(),
    checkUsernameUnique
  ],
  password: formRules.password(),
  confirmPassword: formRules.confirmPassword(() => formState.password),
  email: [
    formRules.email(),
    formRules.maxLength(100),
    checkEmailUnique
  ],
  role: [formRules.requiredSelect('请选择角色')]
}

// 编辑模式校验规则（密码非必填）
const editRules: Record<string, Rule[]> = {
  username: [
    formRules.required('请输入用户名'),
    formRules.length(3, 20, '用户名长度必须在 3-20 个字符之间'),
    formRules.pattern(/^[a-zA-Z0-9_]+$/, '用户名只能包含字母、数字和下划线')
  ],
  email: [
    formRules.email(),
    formRules.maxLength(100),
    checkEmailUnique
  ],
  role: [formRules.requiredSelect('请选择角色')]
}

// 当前使用的规则
const currentRules = computed(() => isEdit.value ? editRules : createRules)

// ==================== 监听弹窗显示 ====================

watch(() => props.visible, (val) => {
  if (val) {
    if (props.mode === 'edit' && props.user) {
      // 编辑模式：填充数据
      formState.username = props.user.username
      formState.password = ''
      formState.confirmPassword = ''
      formState.email = props.user.email || ''
      formState.role = props.user.role as UserRole
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
  formState.username = ''
  formState.password = ''
  formState.confirmPassword = ''
  formState.email = ''
  formState.role = UserRole.USER
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

    if (isEdit.value && props.user) {
      // 编辑用户
      await userApi.update(props.user.id, {
        username: formState.username,
        email: formState.email || undefined,
        role: formState.role
      })
      message.success('用户更新成功')
    } else {
      // 新增用户
      const createData: UserCreateRequest = {
        username: formState.username,
        password: formState.password,
        email: formState.email || undefined,
        role: formState.role
      }
      await userApi.create(createData)
      message.success('用户创建成功')
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
      :rules="currentRules"
      :label-col="{ span: 5 }"
      :wrapper-col="{ span: 18 }"
      autocomplete="off"
    >
      <a-form-item label="用户名" name="username">
        <a-input
          v-model:value="formState.username"
          placeholder="请输入用户名 (3-20位字母数字下划线)"
          :maxlength="20"
          :disabled="isEdit"
          show-count
        />
      </a-form-item>

      <a-form-item 
        v-if="!isEdit" 
        label="密码" 
        name="password"
      >
        <a-input-password
          v-model:value="formState.password"
          placeholder="请输入密码 (6-20位)"
          :maxlength="20"
        />
      </a-form-item>

      <a-form-item 
        v-if="!isEdit" 
        label="确认密码" 
        name="confirmPassword"
      >
        <a-input-password
          v-model:value="formState.confirmPassword"
          placeholder="请再次输入密码"
          :maxlength="20"
        />
      </a-form-item>

      <a-form-item label="邮箱" name="email">
        <a-input
          v-model:value="formState.email"
          placeholder="请输入邮箱 (选填)"
          :maxlength="100"
        />
      </a-form-item>

      <a-form-item label="角色" name="role">
        <a-select
          v-model:value="formState.role"
          placeholder="请选择角色"
          :options="RoleOptions"
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
