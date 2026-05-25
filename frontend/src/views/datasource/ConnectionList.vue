<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { datasourceApi, type DbConnection } from '@/api/datasource'
import { message, Modal } from 'ant-design-vue'
import {
  DatabaseOutlined,
  PlusOutlined,
  ApiOutlined,
  EditOutlined,
  DeleteOutlined,
  GlobalOutlined,
  KeyOutlined,
  EnvironmentOutlined
} from '@ant-design/icons-vue'

const loading = ref(false)
const testingConnection = ref<number | null>(null)
const testingForm = ref(false)
const connections = ref<DbConnection[]>([])
const dbTypes = ref<string[]>([])

// 分页与搜索
const queryParams = ref({
  current: 1,
  size: 12,
  name: '',
  dbType: undefined as string | undefined
})
const total = ref(0)

// 模态框状态
const isModalOpen = ref(false)
const modalTitle = ref('新增数据库连接')
const editingId = ref<number | null>(null)

const formModel = ref({
  name: '',
  dbType: 'MYSQL',
  host: 'localhost',
  port: 3306,
  databaseName: '',
  username: 'root',
  password: '',
  description: ''
})

// 获取数据列表
const fetchConnections = async () => {
  loading.value = true
  try {
    const res = await datasourceApi.list(queryParams.value)
    if (res.code === 200) {
      connections.value = res.data.records || []
      total.value = res.data.totalRow || 0
    }
  } catch (error) {
    console.error('获取连接列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取支持的数据库类型
const fetchDbTypes = async () => {
  try {
    const res = await datasourceApi.getTypes()
    if (res.code === 200) {
      dbTypes.value = res.data || []
    }
  } catch (error) {
    console.error('获取数据库类型失败:', error)
  }
}

// 搜索与重置
const handleSearch = () => {
  queryParams.value.current = 1
  fetchConnections()
}

const handleReset = () => {
  queryParams.value.name = ''
  queryParams.value.dbType = undefined
  queryParams.value.current = 1
  fetchConnections()
}

// 根据类型获取端口默认值
const handleTypeChange = (val: any) => {
  if (val === 'MYSQL') {
    formModel.value.port = 3306
    if (formModel.value.username === 'postgres') formModel.value.username = 'root'
  } else if (val === 'POSTGRESQL') {
    formModel.value.port = 5432
    if (formModel.value.username === 'root') formModel.value.username = 'postgres'
  } else if (val === 'ORACLE') {
    formModel.value.port = 1521
    formModel.value.username = 'system'
  }
}

// 打开新增
const handleAdd = () => {
  editingId.value = null
  modalTitle.value = '新增数据库连接'
  formModel.value = {
    name: '',
    dbType: 'MYSQL',
    host: 'localhost',
    port: 3306,
    databaseName: '',
    username: 'root',
    password: '',
    description: ''
  }
  isModalOpen.value = true
}

// 打开编辑
const handleEdit = (record: DbConnection) => {
  editingId.value = record.id
  modalTitle.value = '编辑数据库连接'
  formModel.value = {
    name: record.name,
    dbType: record.dbType,
    host: record.host,
    port: record.port,
    databaseName: record.databaseName,
    username: record.username,
    password: '', // 默认置空，不修改则不填
    description: record.description || ''
  } as any
  isModalOpen.value = true
}

// 测试表单中的连接
const testFormConnection = async () => {
  testingForm.value = true
  try {
    const res = await datasourceApi.test(formModel.value)
    if (res.code === 200 && res.data) {
      message.success('连接测试成功，配置正确！')
    } else {
      message.error(res.message || '连接测试失败，请检查配置')
    }
  } catch (error: any) {
    message.error(error.message || '连接测试失败')
  } finally {
    testingForm.value = false
  }
}

// 测试已存在的卡片连接
const testCardConnection = async (record: DbConnection) => {
  testingConnection.value = record.id
  try {
    const res = await datasourceApi.test({
      dbType: record.dbType,
      host: record.host,
      port: record.port,
      databaseName: record.databaseName,
      username: record.username,
      password: record.password || '' // 后端不传输密码时可能为空，此处可能报错，但能测已存的
    } as any)
    if (res.code === 200 && res.data) {
      message.success(`数据源 [${record.name}] 连接正常！`)
    } else {
      message.error(`数据源 [${record.name}] 连接失败`)
    }
  } catch (error: any) {
    message.error(error.message || '连接测试失败')
  } finally {
    testingConnection.value = null
  }
}

// 删除数据源
const handleDelete = (id: number) => {
  Modal.confirm({
    title: '确定要删除此数据库连接吗？',
    icon: null,
    content: '删除后，使用此连接的所有同步配置将不可用！',
    okText: '确定删除',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      try {
        const res = await datasourceApi.delete(id)
        if (res.code === 200) {
          message.success('删除成功')
          fetchConnections()
        }
      } catch (error) {
        console.error(error)
      }
    }
  })
}

// 提交表单
const handleModalOk = async () => {
  try {
    if (!formModel.value.name.trim()) return message.warning('请输入连接名称')
    if (!formModel.value.host.trim()) return message.warning('请输入服务器主机')
    if (!formModel.value.databaseName.trim()) return message.warning('请输入数据库名称')
    if (!formModel.value.username.trim()) return message.warning('请输入登录用户名')

    if (editingId.value) {
      // 编辑
      const res = await datasourceApi.update(editingId.value, formModel.value)
      if (res.code === 200) {
        message.success('数据源连接更新成功！')
        isModalOpen.value = false
        fetchConnections()
      }
    } else {
      // 创建
      if (!formModel.value.password) return message.warning('创建新连接必须填写密码')
      const res = await datasourceApi.create(formModel.value)
      if (res.code === 200) {
        message.success('数据源连接创建成功！')
        isModalOpen.value = false
        fetchConnections()
      }
    }
  } catch (error) {
    console.error(error)
  }
}

// 状态卡片颜色
const getDbTypeColor = (type: string) => {
  switch (type.toUpperCase()) {
    case 'MYSQL': return '#00758F'
    case 'POSTGRESQL': return '#336791'
    case 'ORACLE': return '#F80000'
    default: return '#64748B'
  }
}

onMounted(() => {
  fetchConnections()
  fetchDbTypes()
})
</script>

<template>
  <div class="datasource-manager">
    <!-- 页面标题 -->
    <div class="page-header flex-between">
      <div>
        <h1 class="page-title"><DatabaseOutlined class="mr-8" />数据源连接管理</h1>
        <p class="page-desc">配置并维护用于 SeaTunnel 进行数据同步的目标和源数据库连接</p>
      </div>
      <a-button type="primary" @click="handleAdd">
        <template #icon><PlusOutlined /></template>新增连接
      </a-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-form flex-between">
      <div class="flex" style="gap: 16px; flex: 1;">
        <a-input
          v-model:value="queryParams.name"
          placeholder="搜索连接名称"
          style="width: 260px;"
          @pressEnter="handleSearch"
        />
        <a-select
          v-model:value="queryParams.dbType"
          placeholder="筛选数据库类型"
          style="width: 180px;"
          allow-clear
        >
          <a-select-option v-for="type in dbTypes" :key="type" :value="type">
            {{ type }}
          </a-select-option>
        </a-select>
      </div>
      <div>
        <a-button type="primary" class="mr-8" @click="handleSearch">搜索</a-button>
        <a-button type="default" @click="handleReset">重置</a-button>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="connections.length === 0 && !loading" class="page-card text-center p-24">
      <a-empty description="暂无配置的数据库连接" />
      <a-button type="primary" class="mt-16" @click="handleAdd">
        <template #icon><PlusOutlined /></template>创建首个连接
      </a-button>
    </div>

    <!-- 数据源连接列表网格 -->
    <div class="connection-grid" v-else>
      <div
        v-for="conn in connections"
        :key="conn.id"
        class="page-card connection-card"
      >
        <!-- 数据库类型标头 -->
        <div class="card-db-header" :style="{ background: `linear-gradient(135deg, ${getDbTypeColor(conn.dbType)}aa, ${getDbTypeColor(conn.dbType)})` }">
          <DatabaseOutlined class="icon-db" />
          <span class="db-type-label">{{ conn.dbType }}</span>
        </div>

        <div class="card-content">
          <!-- 连接名称 -->
          <h3 class="connection-name text-ellipsis">{{ conn.name }}</h3>
          
          <!-- 连接物理信息 -->
          <div class="meta-item">
            <EnvironmentOutlined class="meta-icon" />
            <span class="meta-value">{{ conn.host }}:{{ conn.port }}</span>
          </div>
          <div class="meta-item">
            <GlobalOutlined class="meta-icon" />
            <span class="meta-value">DB: {{ conn.databaseName }}</span>
          </div>
          <div class="meta-item">
            <KeyOutlined class="meta-icon" />
            <span class="meta-value">User: {{ conn.username }}</span>
          </div>

          <p class="description text-ellipsis mt-8">
            {{ conn.description || '暂无描述' }}
          </p>
        </div>

        <!-- 底部物理拟物按钮排 -->
        <div class="card-actions flex">
          <button
            class="card-btn btn-test"
            :disabled="testingConnection === conn.id"
            @click="testCardConnection(conn)"
          >
            <ApiOutlined :spin="testingConnection === conn.id" />
            {{ testingConnection === conn.id ? '测试中' : '测试连接' }}
          </button>
          <button class="card-btn btn-edit" @click="handleEdit(conn)">
            <EditOutlined />
            编辑
          </button>
          <button class="card-btn btn-delete" @click="handleDelete(conn.id)">
            <DeleteOutlined />
            删除
          </button>
        </div>
      </div>
    </div>

    <!-- 数据源表单模态框 -->
    <a-modal
      v-model:open="isModalOpen"
      :title="modalTitle"
      @ok="handleModalOk"
      ok-text="确认保存"
      cancel-text="取消"
      width="600px"
    >
      <a-form :model="formModel" layout="vertical" class="mt-16">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="连接名称" required>
              <a-input v-model:value="formModel.name" placeholder="例如：主数据库_MySQL" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="数据库类型" required>
              <a-select v-model:value="formModel.dbType" @change="handleTypeChange">
                <a-select-option v-for="type in dbTypes" :key="type" :value="type">
                  {{ type }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="16">
            <a-form-item label="主机地址" required>
              <a-input v-model:value="formModel.host" placeholder="例如：192.168.1.100 或 localhost" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="端口" required>
              <a-input-number v-model:value="formModel.port" :min="1" :max="65535" style="width: 100%;" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="数据库名" required>
              <a-input v-model:value="formModel.databaseName" placeholder="数据库 Schema 名称" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="登录用户名" required>
              <a-input v-model:value="formModel.username" placeholder="数据库登录账户" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item :label="editingId ? '密码 (留空则不修改)' : '密码'" :required="!editingId">
          <a-input-password v-model:value="formModel.password" placeholder="请输入数据库访问口令" />
        </a-form-item>

        <a-form-item label="描述">
          <a-textarea v-model:value="formModel.description" :rows="3" placeholder="描述此连接的用途" />
        </a-form-item>
      </a-form>

      <template #footer>
        <div class="flex-between">
          <a-button type="dashed" danger :loading="testingForm" @click="testFormConnection">
            <template #icon><ApiOutlined /></template>测试连接配置
          </a-button>
          <div>
            <a-button class="mr-8" @click="isModalOpen = false">取消</a-button>
            <a-button type="primary" @click="handleModalOk">确认保存</a-button>
          </div>
        </div>
      </template>
    </a-modal>
  </div>
</template>

<style scoped lang="less">
.datasource-manager {
  perspective: 2000px;
}

.connection-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 28px;
}

.connection-card {
  height: 310px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  overflow: hidden;
  padding: 0;

  .card-db-header {
    height: 54px;
    padding: 0 20px;
    display: flex;
    align-items: center;
    color: #ffffff;
    font-weight: 700;
    font-size: 16px;
    letter-spacing: 0.05em;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);

    .icon-db {
      font-size: 18px;
      margin-right: 10px;
    }

    .db-type-label {
      text-transform: uppercase;
      font-size: 15px;
    }
  }

  .card-content {
    padding: 20px;
    flex-grow: 1;

    .connection-name {
      font-size: 18px;
      font-weight: 700;
      color: var(--heading-color);
      margin-bottom: 12px;
      margin-top: 0;
    }

    .meta-item {
      display: flex;
      align-items: center;
      margin-bottom: 6px;
      font-size: 13.5px;
      color: var(--text-color-secondary);
      font-weight: 500;

      .meta-icon {
        font-size: 14px;
        margin-right: 8px;
        color: var(--primary-color);
      }

      .meta-value {
        font-family: SFMono-Regular, Consolas, monospace;
      }
    }

    .description {
      font-size: 13px;
      color: var(--text-color-secondary);
      margin-bottom: 0;
      line-height: 1.5;
    }
  }

  .card-actions {
    border-top: var(--card-border);
    background: rgba(148, 163, 184, 0.04);
    height: 52px;

    .card-btn {
      flex: 1;
      border: none;
      background: transparent;
      color: var(--text-color-secondary);
      font-size: 13.5px;
      font-weight: 600;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
      border-right: var(--card-border);

      &:last-child {
        border-right: none;
      }

      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }

      &.btn-test {
        &:hover:not(:disabled) {
          color: var(--success-color);
          background: rgba(16, 185, 129, 0.08);
        }
      }

      &.btn-edit {
        &:hover {
          color: var(--primary-color);
          background: rgba(37, 99, 235, 0.08);
        }
      }

      &.btn-delete {
        &:hover {
          color: var(--error-color);
          background: rgba(239, 68, 68, 0.08);
        }
      }
    }
  }
}
</style>
