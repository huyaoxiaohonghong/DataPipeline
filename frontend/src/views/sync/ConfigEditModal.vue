<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { datasourceApi, type DbConnection, type ColumnMetadata } from '@/api/datasource'
import { syncApi, type FieldMappingDTO } from '@/api/sync'
import { message } from 'ant-design-vue'
import {
  InfoCircleOutlined,
  ArrowRightOutlined,
  PlusOutlined,
  DeleteOutlined,
  ExperimentOutlined,
  SlidersOutlined
} from '@ant-design/icons-vue'

const props = defineProps<{
  open: boolean
  configId: number | null
}>()

const emit = defineEmits(['update:open', 'success'])

const visible = computed({
  get: () => props.open,
  set: (val) => emit('update:open', val)
})

const loading = ref(false)
const saving = ref(false)
const currentStep = ref(0)

// 数据库连接列表
const dbConnections = ref<DbConnection[]>([])
const sourceTables = ref<string[]>([])
const targetTables = ref<string[]>([])
const sourceColumns = ref<ColumnMetadata[]>([])
const targetColumns = ref<ColumnMetadata[]>([])

// 表单模型
const formModel = ref({
  name: '',
  description: '',
  syncMode: 'FULL' as 'FULL' | 'INCREMENTAL',
  incrementalField: '',
  sourceDbId: undefined as number | undefined,
  sourceTable: '',
  targetDbId: undefined as number | undefined,
  targetTable: '',
  fieldMappings: [] as FieldMappingDTO[]
})

// 监听打开状态
watch(visible, async (isOpen) => {
  if (isOpen) {
    currentStep.value = 0
    await fetchDbConnections()
    if (props.configId) {
      await fetchConfigDetail(props.configId)
    } else {
      resetForm()
    }
  }
})

// 重置表单
const resetForm = () => {
  formModel.value = {
    name: '',
    description: '',
    syncMode: 'FULL',
    incrementalField: '',
    sourceDbId: undefined,
    sourceTable: '',
    targetDbId: undefined,
    targetTable: '',
    fieldMappings: []
  }
  sourceTables.value = []
  targetTables.value = []
  sourceColumns.value = []
  targetColumns.value = []
}

// 获取配置详情 (编辑)
const fetchConfigDetail = async (id: number) => {
  loading.value = true
  try {
    const res = await syncApi.getConfigById(id)
    if (res.code === 200 && res.data) {
      const config = res.data
      formModel.value = {
        name: config.name,
        description: config.description || '',
        syncMode: config.syncMode,
        incrementalField: config.incrementalField || '',
        sourceDbId: config.sourceDbId,
        sourceTable: config.sourceTable,
        targetDbId: config.targetDbId,
        targetTable: config.targetTable,
        fieldMappings: config.fieldMappings || []
      }
      
      // 加载对应的表和字段
      if (config.sourceDbId) {
        await handleSourceDbChange(config.sourceDbId, false)
        if (config.sourceTable) {
          await handleSourceTableChange(config.sourceTable, false)
        }
      }
      if (config.targetDbId) {
        await handleTargetDbChange(config.targetDbId, false)
        if (config.targetTable) {
          await handleTargetTableChange(config.targetTable, false)
        }
      }
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 获取数据源连接列表
const fetchDbConnections = async () => {
  try {
    const res = await datasourceApi.list({ current: 1, size: 100 })
    if (res.code === 200) {
      dbConnections.value = res.data.records || []
    }
  } catch (error) {
    console.error(error)
  }
}

// 源 DB 改变
const handleSourceDbChange = async (val: number, clearTable = true) => {
  if (clearTable) {
    formModel.value.sourceTable = ''
    formModel.value.incrementalField = ''
    sourceColumns.value = []
  }
  try {
    const res = await datasourceApi.getTables(val)
    if (res.code === 200) {
      sourceTables.value = res.data || []
    }
  } catch (error) {
    console.error(error)
  }
}

// 源表改变
const handleSourceTableChange = async (val: string, clearColumns = true) => {
  if (!formModel.value.sourceDbId) return
  if (clearColumns) {
    sourceColumns.value = []
    formModel.value.incrementalField = ''
  }
  try {
    const res = await datasourceApi.getColumns(formModel.value.sourceDbId, val)
    if (res.code === 200) {
      sourceColumns.value = res.data || []
    }
  } catch (error) {
    console.error(error)
  }
}

// 目标 DB 改变
const handleTargetDbChange = async (val: number, clearTable = true) => {
  if (clearTable) {
    formModel.value.targetTable = ''
    targetColumns.value = []
  }
  try {
    const res = await datasourceApi.getTables(val)
    if (res.code === 200) {
      targetTables.value = res.data || []
    }
  } catch (error) {
    console.error(error)
  }
}

// 目标表改变
const handleTargetTableChange = async (val: string, clearColumns = true) => {
  if (!formModel.value.targetDbId) return
  if (clearColumns) {
    targetColumns.value = []
  }
  try {
    const res = await datasourceApi.getColumns(formModel.value.targetDbId, val)
    if (res.code === 200) {
      targetColumns.value = res.data || []
    }
  } catch (error) {
    console.error(error)
  }
}

// 自动匹配字段映射
const autoMatchFields = () => {
  if (sourceColumns.value.length === 0 || targetColumns.value.length === 0) {
    return message.warning('请先确保源表和目标表均已选择且加载成功！')
  }

  const mappings: FieldMappingDTO[] = []
  let count = 0

  sourceColumns.value.forEach((src) => {
    if (!src || !src.columnName) return
    // 匹配同名且大小写无关的字段
    const matched = targetColumns.value.find(
      (tgt) => tgt && tgt.columnName && tgt.columnName.toLowerCase() === src.columnName.toLowerCase()
    )
    if (matched) {
      mappings.push({
        sourceField: src.columnName,
        sourceType: src.dataType,
        targetField: matched.columnName,
        targetType: matched.dataType,
        transformExpr: '',
        sortOrder: count++
      })
    }
  })

  formModel.value.fieldMappings = mappings
  message.success(`智能分析完成：自动匹配了 ${mappings.length} 个同名字段！`)
}

// 手动增加一行映射
const addMappingRow = () => {
  formModel.value.fieldMappings.push({
    sourceField: '',
    sourceType: '',
    targetField: '',
    targetType: '',
    transformExpr: '',
    sortOrder: formModel.value.fieldMappings.length
  })
}

// 手动删除一行映射
const removeMappingRow = (index: number) => {
  formModel.value.fieldMappings.splice(index, 1)
}

// 映射字段修改时自动填充类型
const handleSourceFieldSelect = (val: string, index: number) => {
  const col = sourceColumns.value.find(c => c && c.columnName === val)
  if (col) {
    formModel.value.fieldMappings[index].sourceType = col.dataType
    // 如果目标字段是空的，且目标表中有同名字段，自动匹配过去
    if (!formModel.value.fieldMappings[index].targetField) {
      const tgt = targetColumns.value.find(c => c && c.columnName && val && c.columnName.toLowerCase() === val.toLowerCase())
      if (tgt) {
        formModel.value.fieldMappings[index].targetField = tgt.columnName
        formModel.value.fieldMappings[index].targetType = tgt.dataType
      }
    }
  }
}

const handleTargetFieldSelect = (val: string, index: number) => {
  const col = targetColumns.value.find(c => c && c.columnName === val)
  if (col) {
    formModel.value.fieldMappings[index].targetType = col.dataType
  }
}

// 下一步
const nextStep = () => {
  if (currentStep.value === 0) {
    if (!formModel.value.name.trim()) return message.warning('请输入同步配置名称')
    if (formModel.value.syncMode === 'INCREMENTAL' && !formModel.value.incrementalField) {
      return message.warning('增量模式下必须指定增量字段！')
    }
  } else if (currentStep.value === 1) {
    if (!formModel.value.sourceDbId) return message.warning('请选择源数据库')
    if (!formModel.value.sourceTable) return message.warning('请选择源数据表')
  } else if (currentStep.value === 2) {
    if (!formModel.value.targetDbId) return message.warning('请选择目标数据库')
    if (!formModel.value.targetTable) return message.warning('请选择目标数据表')
    
    // 进入映射页面时，如果映射为空，进行一次自动匹配
    if (formModel.value.fieldMappings.length === 0) {
      autoMatchFields()
    }
  }
  currentStep.value++
}

const prevStep = () => {
  currentStep.value--
}

// 保存配置
const handleSave = async () => {
  if (formModel.value.fieldMappings.length === 0) {
    return message.warning('请配置至少一个字段映射！')
  }

  // 校验映射中是否包含空字段
  const hasInvalid = formModel.value.fieldMappings.some(m => !m.sourceField || !m.targetField)
  if (hasInvalid) {
    return message.warning('映射字段列表中源字段或目标字段不能为空')
  }

  saving.value = true
  try {
    if (props.configId) {
      const res = await syncApi.updateConfig(props.configId, formModel.value as any)
      if (res.code === 200) {
        message.success('更新同步配置成功！')
        visible.value = false
        emit('success')
      }
    } else {
      const res = await syncApi.createConfig(formModel.value as any)
      if (res.code === 200) {
        message.success('创建同步配置成功！')
        visible.value = false
        emit('success')
      }
    }
  } catch (error) {
    console.error(error)
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <a-modal
    v-model:open="visible"
    :title="props.configId ? '编辑 SeaTunnel 数据同步配置' : '新建 SeaTunnel 数据同步配置'"
    width="880px"
    :footer="null"
    class="wizard-modal"
  >
    <!-- 步骤条 -->
    <a-steps :current="currentStep" size="small" class="p-16">
      <a-step title="基本设置" />
      <a-step title="源数据源" />
      <a-step title="目标数据源" />
      <a-step title="字段映射" />
    </a-steps>

    <a-spin :spinning="loading">
      <div class="wizard-content p-24">
        <!-- 步骤 0: 基本配置 -->
        <div v-if="currentStep === 0">
          <a-form layout="vertical">
            <a-form-item label="同步配置名称" required>
              <a-input v-model:value="formModel.name" placeholder="起一个好记的名称，例如：用户表每日增量同步" />
            </a-form-item>
            <a-form-item label="描述">
              <a-textarea v-model:value="formModel.description" :rows="3" placeholder="配置的功能、作者等说明信息" />
            </a-form-item>
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item label="同步模式" required>
                  <a-select v-model:value="formModel.syncMode">
                    <a-select-option value="FULL">FULL (全量同步)</a-select-option>
                    <a-select-option value="INCREMENTAL">INCREMENTAL (增量同步)</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="12" v-if="formModel.syncMode === 'INCREMENTAL'">
                <a-form-item label="增量字段 (如 updated_at, id)" required>
                  <a-select
                    v-model:value="formModel.incrementalField"
                    placeholder="请选择增量过滤字段 (步骤二加载后可用)"
                    show-search
                  >
                    <a-select-option v-for="col in sourceColumns" :key="col.columnName" :value="col.columnName">
                      {{ col.columnName }} ({{ col.dataType }})
                    </a-select-option>
                  </a-select>
                  <template #extra>
                    <span class="step-info"><InfoCircleOutlined class="mr-8" />仅支持数值、时间类型的字段进行增量过滤</span>
                  </template>
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </div>

        <!-- 步骤 1: 源数据源 -->
        <div v-if="currentStep === 1">
          <a-form layout="vertical">
            <a-form-item label="源数据库连接" required>
              <a-select
                v-model:value="formModel.sourceDbId"
                placeholder="请选择源端数据源"
                @change="(val: any) => handleSourceDbChange(val)"
              >
                <a-select-option v-for="db in dbConnections" :key="db.id" :value="db.id">
                  {{ db.name }} ({{ db.dbType }}) - {{ db.host }}
                </a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="源数据表" required>
              <a-select
                v-model:value="formModel.sourceTable"
                placeholder="请选择源表"
                show-search
                :disabled="!formModel.sourceDbId"
                @change="(val: any) => handleSourceTableChange(val)"
              >
                <a-select-option v-for="tbl in sourceTables" :key="tbl" :value="tbl">
                  {{ tbl }}
                </a-select-option>
              </a-select>
            </a-form-item>

            <div class="page-card schema-preview mt-16" v-if="sourceColumns.length > 0">
              <h4>源表结构信息 (共 {{ sourceColumns.length }} 个字段)</h4>
              <div class="schema-tags">
                <a-tag v-for="col in sourceColumns" :key="col.columnName" :color="col.isPrimaryKey ? 'orange' : 'default'">
                  {{ col.columnName }} <span class="datatype">({{ col.dataType }})</span>
                </a-tag>
              </div>
            </div>
          </a-form>
        </div>

        <!-- 步骤 2: 目标数据源 -->
        <div v-if="currentStep === 2">
          <a-form layout="vertical">
            <a-form-item label="目标数据库连接" required>
              <a-select
                v-model:value="formModel.targetDbId"
                placeholder="请选择写入的目标数据源"
                @change="(val: any) => handleTargetDbChange(val)"
              >
                <a-select-option v-for="db in dbConnections" :key="db.id" :value="db.id">
                  {{ db.name }} ({{ db.dbType }}) - {{ db.host }}
                </a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="目标数据表" required>
              <a-select
                v-model:value="formModel.targetTable"
                placeholder="请选择写入目标表"
                show-search
                :disabled="!formModel.targetDbId"
                @change="(val: any) => handleTargetTableChange(val)"
              >
                <a-select-option v-for="tbl in targetTables" :key="tbl" :value="tbl">
                  {{ tbl }}
                </a-select-option>
              </a-select>
            </a-form-item>

            <div class="page-card schema-preview mt-16" v-if="targetColumns.length > 0">
              <h4>目标表结构信息 (共 {{ targetColumns.length }} 个字段)</h4>
              <div class="schema-tags">
                <a-tag v-for="col in targetColumns" :key="col.columnName" :color="col.isPrimaryKey ? 'blue' : 'default'">
                  {{ col.columnName }} <span class="datatype">({{ col.dataType }})</span>
                </a-tag>
              </div>
            </div>
          </a-form>
        </div>

        <!-- 步骤 3: 字段映射配置 -->
        <div v-if="currentStep === 3">
          <div class="mapping-toolbar flex-between mb-16">
            <div class="left flex">
              <span class="title"><SlidersOutlined class="mr-8 text-primary" />映射字段矩阵</span>
              <a-tag color="cyan" class="ml-8">已配置: {{ formModel.fieldMappings.length }} 个映射</a-tag>
            </div>
            <div class="right">
              <a-button type="dashed" class="mr-8" @click="autoMatchFields">
                <template #icon><ExperimentOutlined /></template>智能同名映射
              </a-button>
              <a-button type="primary" size="small" @click="addMappingRow">
                <template #icon><PlusOutlined /></template>增加一行
              </a-button>
            </div>
          </div>

          <!-- 字段映射表格容器 -->
          <div class="mapping-table-container">
            <div class="mapping-table-header flex">
              <div class="col-field">源表字段 (Source)</div>
              <div class="col-arrow"></div>
              <div class="col-field">目标表字段 (Target)</div>
              <div class="col-expr">转换表达式 (Expression)</div>
              <div class="col-action"></div>
            </div>

            <div class="mapping-rows">
              <div
                v-for="(mapping, index) in formModel.fieldMappings"
                :key="index"
                class="mapping-row flex"
              >
                <!-- 源字段选择 -->
                <div class="col-field">
                  <a-select
                    v-model:value="mapping.sourceField"
                    placeholder="源字段"
                    show-search
                    style="width: 100%;"
                    @change="(val: any) => handleSourceFieldSelect(val, index)"
                  >
                    <a-select-option v-for="col in sourceColumns" :key="col.columnName" :value="col.columnName">
                      {{ col.columnName }} <span class="type-tag">{{ col.dataType }}</span>
                    </a-select-option>
                  </a-select>
                </div>

                <!-- 关系箭头 -->
                <div class="col-arrow flex-center">
                  <ArrowRightOutlined class="text-primary" />
                </div>

                <!-- 目标字段选择 -->
                <div class="col-field">
                  <a-select
                    v-model:value="mapping.targetField"
                    placeholder="目标字段"
                    show-search
                    style="width: 100%;"
                    @change="(val: any) => handleTargetFieldSelect(val, index)"
                  >
                    <a-select-option v-for="col in targetColumns" :key="col.columnName" :value="col.columnName">
                      {{ col.columnName }} <span class="type-tag">{{ col.dataType }}</span>
                    </a-select-option>
                  </a-select>
                </div>

                <!-- 转换表达式 -->
                <div class="col-expr">
                  <a-input
                    v-model:value="mapping.transformExpr"
                    placeholder="如: cast(col as string) 或留空"
                  />
                </div>

                <!-- 行删除 -->
                <div class="col-action flex-center">
                  <a-button type="text" danger @click="removeMappingRow(index)">
                    <template #icon><DeleteOutlined /></template>
                  </a-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部控制按钮排 -->
      <div class="wizard-footer flex-between p-24">
        <div>
          <a-button :disabled="currentStep === 0" @click="prevStep">上一步</a-button>
        </div>
        <div>
          <a-button class="mr-8" @click="visible = false">取消</a-button>
          <a-button v-if="currentStep < 3" type="primary" @click="nextStep">下一步</a-button>
          <a-button
            v-else
            type="primary"
            :loading="saving"
            @click="handleSave"
          >
            保存并部署
          </a-button>
        </div>
      </div>
    </a-spin>
  </a-modal>
</template>

<style scoped lang="less">
.wizard-modal {
  :deep(.ant-modal-body) {
    padding: 0;
  }
}

.wizard-content {
  min-height: 380px;
  max-height: 520px;
  overflow-y: auto;
  border-top: 1px solid rgba(148, 163, 184, 0.1);
  border-bottom: 1px solid rgba(148, 163, 184, 0.1);
  background: var(--body-bg);
}

.step-info {
  font-size: 12.5px;
  color: var(--text-color-secondary);
}

.schema-preview {
  padding: 16px;
  margin-top: 20px;

  h4 {
    margin-top: 0;
    margin-bottom: 12px;
    font-size: 14px;
    font-weight: 700;
    color: var(--heading-color);
  }

  .schema-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    .datatype {
      font-size: 11px;
      opacity: 0.7;
    }
  }
}

.mapping-toolbar {
  .title {
    font-size: 15px;
    font-weight: 700;
    color: var(--heading-color);
  }
}

.mapping-table-container {
  display: flex;
  flex-direction: column;
  border: var(--card-border);
  border-radius: 12px;
  overflow: hidden;
  background: var(--card-bg);

  .mapping-table-header {
    background: var(--table-header-bg);
    padding: 10px 16px;
    font-weight: 700;
    font-size: 13.5px;
    color: var(--heading-color);
    border-bottom: var(--card-border);
  }

  .mapping-rows {
    max-height: 280px;
    overflow-y: auto;
  }

  .mapping-row {
    padding: 8px 16px;
    align-items: center;
    border-bottom: 1px solid rgba(148, 163, 184, 0.08);

    &:last-child {
      border-bottom: none;
    }
  }

  .col-field {
    flex: 2;
    min-width: 150px;
  }

  .col-arrow {
    width: 50px;
    font-size: 16px;
  }

  .col-expr {
    flex: 3;
    margin-left: 12px;
  }

  .col-action {
    width: 44px;
    margin-left: 8px;
  }

  .type-tag {
    float: right;
    font-size: 11px;
    color: var(--text-color-secondary);
    background: rgba(148, 163, 184, 0.15);
    padding: 1px 4px;
    border-radius: 4px;
  }
}

.wizard-footer {
  border-top: 1px solid rgba(148, 163, 184, 0.08);
}
</style>
