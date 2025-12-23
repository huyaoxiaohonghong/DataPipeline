<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import { message } from 'ant-design-vue'
import { ApiOutlined, CheckCircleOutlined, CloseCircleOutlined } from '@ant-design/icons-vue'
import { dbConnectionApi, DbTypeOptions } from '@/api/db'
import type { DbConnection, DbConnectionCreateRequest, DbConnectionUpdateRequest, DbMonitorInfo } from '@/api/db'

// ==================== Props & Emits ====================

const props = defineProps<{
  visible: boolean
  mode: 'create' | 'edit'
  connection: DbConnection | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}>()

// ==================== 状态定义 ====================

const formRef = ref()
const loading = ref(false)
const testLoading = ref(false)
const testResult = ref<DbMonitorInfo | null>(null)

const formData = reactive({
  name: '',
  dbType: 'MYSQL',
  host: '',
  port: 3306,
  databaseName: '',
  username: '',
  password: '',
  description: ''
})

// ==================== 计算属性 ====================

const modalTitle = computed(() => props.mode === 'create' ? '新增数据库连接' : '编辑数据库连接')

const isVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

// 默认端口映射
const defaultPorts: Record<string, number> = {
  MYSQL: 3306,
  POSTGRESQL: 5432,
  ORACLE: 1521,
  SQLSERVER: 1433
}

// ==================== 表单验证规则 ====================

const rules = {
  name: [{ required: true, message: '请输入连接名称', trigger: 'blur' as const, type: 'string' as const }],
  dbType: [{ required: true, message: '请选择数据库类型', trigger: 'change' as const, type: 'string' as const }],
  host: [{ required: true, message: '请输入主机地址', trigger: 'blur' as const, type: 'string' as const }],
  port: [{ required: true, message: '请输入端口号', trigger: 'blur' as const, type: 'number' as const }],
  databaseName: [{ required: true, message: '请输入数据库名', trigger: 'blur' as const, type: 'string' as const }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' as const, type: 'string' as const }],
  password: [{ required: props.mode === 'create', message: '请输入密码', trigger: 'blur' as const, type: 'string' as const }]
}

// ==================== 监听 ====================

watch(() => props.visible, (val) => {
  if (val) {
    testResult.value = null
    if (props.mode === 'edit' && props.connection) {
      // 编辑模式：填充数据
      formData.name = props.connection.name
      formData.dbType = props.connection.dbType
      formData.host = props.connection.host
      formData.port = props.connection.port
      formData.databaseName = props.connection.databaseName
      formData.username = props.connection.username
      formData.password = ''  // 密码不回显
      formData.description = props.connection.description || ''
    } else {
      // 创建模式：重置表单
      Object.assign(formData, {
        name: '',
        dbType: 'MYSQL',
        host: '',
        port: 3306,
        databaseName: '',
        username: '',
        password: '',
        description: ''
      })
    }
  }
})

// 数据库类型变化时更新默认端口
watch(() => formData.dbType, (newType) => {
  if (props.mode === 'create') {
    formData.port = defaultPorts[newType] || 3306
  }
})

// ==================== 方法 ====================

/**
 * 测试连接
 */
const handleTest = async () => {
  try {
    await formRef.value.validate(['dbType', 'host', 'port', 'databaseName', 'username', 'password'])
  } catch {
    message.warning('请先填写完整的连接信息')
    return
  }

  testLoading.value = true
  testResult.value = null
  try {
    const { data } = await dbConnectionApi.test({
      dbType: formData.dbType,
      host: formData.host,
      port: formData.port,
      databaseName: formData.databaseName,
      username: formData.username,
      password: formData.password
    })
    testResult.value = data as DbMonitorInfo
    if (testResult.value.status === 'CONNECTED') {
      message.success('连接测试成功！')
    }
  } catch (error: any) {
    testResult.value = {
      status: 'ERROR',
      errorMessage: error.message || '连接测试失败'
    }
  } finally {
    testLoading.value = false
  }
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    if (props.mode === 'create') {
      const createData: DbConnectionCreateRequest = {
        name: formData.name,
        dbType: formData.dbType,
        host: formData.host,
        port: formData.port,
        databaseName: formData.databaseName,
        username: formData.username,
        password: formData.password,
        description: formData.description || undefined
      }
      await dbConnectionApi.create(createData)
      message.success('创建成功')
    } else {
      const updateData: DbConnectionUpdateRequest = {
        name: formData.name,
        dbType: formData.dbType,
        host: formData.host,
        port: formData.port,
        databaseName: formData.databaseName,
        username: formData.username,
        password: formData.password || undefined,
        description: formData.description || undefined
      }
      await dbConnectionApi.update(props.connection!.id, updateData)
      message.success('更新成功')
    }
    emit('success')
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 关闭弹窗
 */
const handleCancel = () => {
  isVisible.value = false
}
</script>

<template>
  <a-modal
    v-model:open="isVisible"
    :title="modalTitle"
    :width="600"
    :confirm-loading="loading"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      :label-col="{ span: 5 }"
      :wrapper-col="{ span: 18 }"
    >
      <a-form-item label="连接名称" name="name">
        <a-input v-model:value="formData.name" placeholder="请输入连接名称" />
      </a-form-item>

      <a-form-item label="数据库类型" name="dbType">
        <a-select
          v-model:value="formData.dbType"
          placeholder="请选择数据库类型"
          :options="DbTypeOptions"
        />
      </a-form-item>

      <a-form-item label="主机地址" name="host">
        <a-input v-model:value="formData.host" placeholder="请输入主机地址，如 localhost 或 IP" />
      </a-form-item>

      <a-form-item label="端口号" name="port">
        <a-input-number
          v-model:value="formData.port"
          :min="1"
          :max="65535"
          style="width: 100%"
          placeholder="请输入端口号"
        />
      </a-form-item>

      <a-form-item label="数据库名" name="databaseName">
        <a-input v-model:value="formData.databaseName" placeholder="请输入数据库名" />
      </a-form-item>

      <a-form-item label="用户名" name="username">
        <a-input v-model:value="formData.username" placeholder="请输入用户名" />
      </a-form-item>

      <a-form-item label="密码" name="password">
        <a-input-password
          v-model:value="formData.password"
          :placeholder="mode === 'edit' ? '留空则不修改密码' : '请输入密码'"
        />
      </a-form-item>

      <a-form-item label="描述" name="description">
        <a-textarea
          v-model:value="formData.description"
          placeholder="请输入描述信息"
          :rows="3"
        />
      </a-form-item>

      <!-- 测试连接 -->
      <a-form-item :wrapper-col="{ offset: 5, span: 18 }">
        <a-space>
          <a-button :loading="testLoading" @click="handleTest">
            <template #icon><ApiOutlined /></template>
            测试连接
          </a-button>
          <template v-if="testResult">
            <a-tag v-if="testResult.status === 'CONNECTED'" color="success">
              <CheckCircleOutlined /> 连接成功
              <span v-if="testResult.version">（{{ testResult.version }}）</span>
            </a-tag>
            <a-tag v-else color="error">
              <CloseCircleOutlined /> {{ testResult.errorMessage }}
            </a-tag>
          </template>
        </a-space>
      </a-form-item>
    </a-form>
  </a-modal>
</template>
