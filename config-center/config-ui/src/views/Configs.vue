<template>
  <div class="configs">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>配置管理</span>
          <el-button type="primary" @click="showCreateDialog = true">
            <el-icon><Plus /></el-icon>
            创建配置
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="6">
          <el-select v-model="appFilter" placeholder="选择应用" clearable @change="handleSearch">
            <el-option
              v-for="app in applications"
              :key="app.id"
              :label="app.appName"
              :value="app.id"
            />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="envFilter" placeholder="选择环境" clearable @change="handleSearch">
            <el-option
              v-for="env in environments"
              :key="env.id"
              :label="env.envName"
              :value="env.id"
            />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索配置键"
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="handleSearch">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-col>
      </el-row>

      <!-- 配置列表 -->
      <el-table :data="configs" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="configKey" label="配置键" width="200" />
        <el-table-column prop="configValue" label="配置值" width="300" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="scope.row.isEncrypted === 1">***加密***</span>
            <span v-else>{{ scope.row.configValue }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="configType" label="类型" width="100">
          <template #default="scope">
            <el-tag :type="getConfigTypeColor(scope.row.configType)">
              {{ getConfigTypeName(scope.row.configType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isRequired" label="必填" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.isRequired === 1 ? 'danger' : 'info'">
              {{ scope.row.isRequired === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isEncrypted" label="加密" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.isEncrypted === 1 ? 'warning' : 'info'">
              {{ scope.row.isEncrypted === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="handleDelete(scope.row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="margin-top: 20px; text-align: right;">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingConfig ? '编辑配置' : '创建配置'"
      width="800px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="应用" prop="appId">
              <el-select v-model="form.appId" placeholder="选择应用" style="width: 100%">
                <el-option
                  v-for="app in applications"
                  :key="app.id"
                  :label="app.appName"
                  :value="app.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="环境" prop="envId">
              <el-select v-model="form.envId" placeholder="选择环境" style="width: 100%">
                <el-option
                  v-for="env in environments"
                  :key="env.id"
                  :label="env.envName"
                  :value="env.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="配置键" prop="configKey">
          <el-input v-model="form.configKey" placeholder="请输入配置键" />
        </el-form-item>
        
        <el-form-item label="配置值" prop="configValue">
          <div style="height: 300px; border: 1px solid #dcdfe6; border-radius: 4px;">
            <MonacoEditor
              v-model="form.configValue"
              :language="getMonacoLanguage(form.configType)"
              :options="monacoOptions"
            />
          </div>
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="配置类型" prop="configType">
              <el-select v-model="form.configType" placeholder="选择类型" style="width: 100%">
                <el-option label="字符串" :value="1" />
                <el-option label="数字" :value="2" />
                <el-option label="布尔" :value="3" />
                <el-option label="JSON" :value="4" />
                <el-option label="YAML" :value="5" />
                <el-option label="Properties" :value="6" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否必填" prop="isRequired">
              <el-switch v-model="form.isRequired" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否加密" prop="isEncrypted">
              <el-switch v-model="form.isEncrypted" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="默认值" prop="defaultValue">
          <el-input
            v-model="form.defaultValue"
            type="textarea"
            :rows="3"
            placeholder="请输入默认值"
          />
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { configApi, applicationApi, environmentApi } from '../api'
import MonacoEditor from '../components/MonacoEditor.vue'

const loading = ref(false)
const configs = ref([])
const applications = ref([])
const environments = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const appFilter = ref('')
const envFilter = ref('')
const searchKeyword = ref('')
const statusFilter = ref('')
const showCreateDialog = ref(false)
const editingConfig = ref(null)
const formRef = ref()

const form = reactive({
  appId: '',
  envId: '',
  configKey: '',
  configValue: '',
  configType: 1,
  isRequired: false,
  isEncrypted: false,
  defaultValue: '',
  description: '',
  status: 1
})

const rules = {
  appId: [
    { required: true, message: '请选择应用', trigger: 'change' }
  ],
  envId: [
    { required: true, message: '请选择环境', trigger: 'change' }
  ],
  configKey: [
    { required: true, message: '请输入配置键', trigger: 'blur' },
    { min: 1, max: 128, message: '长度在 1 到 128 个字符', trigger: 'blur' }
  ]
}

const monacoOptions = {
  theme: 'vs-dark',
  fontSize: 14,
  minimap: { enabled: false },
  scrollBeyondLastLine: false,
  automaticLayout: true
}

const getConfigTypeName = (type) => {
  const types = {
    1: '字符串',
    2: '数字',
    3: '布尔',
    4: 'JSON',
    5: 'YAML',
    6: 'Properties'
  }
  return types[type] || '未知'
}

const getConfigTypeColor = (type) => {
  const colors = {
    1: '',
    2: 'success',
    3: 'warning',
    4: 'primary',
    5: 'info',
    6: 'danger'
  }
  return colors[type] || ''
}

const getMonacoLanguage = (type) => {
  const languages = {
    1: 'plaintext',
    2: 'plaintext',
    3: 'plaintext',
    4: 'json',
    5: 'yaml',
    6: 'properties'
  }
  return languages[type] || 'plaintext'
}

const loadConfigs = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      appId: appFilter.value,
      envId: envFilter.value,
      keyword: searchKeyword.value,
      status: statusFilter.value
    }
    const response = await configApi.getConfigs(params)
    configs.value = response.data.content || []
    total.value = response.data.totalElements || 0
  } catch (error) {
    console.error('加载配置列表失败:', error)
  } finally {
    loading.value = false
  }
}

const loadApplications = async () => {
  try {
    const response = await applicationApi.getApplications({ size: 1000 })
    applications.value = response.data.content || []
  } catch (error) {
    console.error('加载应用列表失败:', error)
  }
}

const loadEnvironments = async () => {
  try {
    const response = await environmentApi.getEnvironments({ size: 1000 })
    environments.value = response.data.content || []
  } catch (error) {
    console.error('加载环境列表失败:', error)
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadConfigs()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadConfigs()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadConfigs()
}

const handleEdit = (row) => {
  editingConfig.value = row
  Object.assign(form, {
    ...row,
    isRequired: row.isRequired === 1,
    isEncrypted: row.isEncrypted === 1
  })
  showCreateDialog.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除配置 "${row.configKey}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await configApi.deleteConfig(row.id)
    ElMessage.success('删除成功')
    loadConfigs()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    const submitData = {
      ...form,
      isRequired: form.isRequired ? 1 : 0,
      isEncrypted: form.isEncrypted ? 1 : 0
    }
    
    if (editingConfig.value) {
      await configApi.updateConfig(editingConfig.value.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await configApi.createConfig(submitData)
      ElMessage.success('创建成功')
    }
    
    showCreateDialog.value = false
    resetForm()
    loadConfigs()
  } catch (error) {
    console.error('提交失败:', error)
  }
}

const resetForm = () => {
  editingConfig.value = null
  Object.assign(form, {
    appId: '',
    envId: '',
    configKey: '',
    configValue: '',
    configType: 1,
    isRequired: false,
    isEncrypted: false,
    defaultValue: '',
    description: '',
    status: 1
  })
  formRef.value?.resetFields()
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString()
}

onMounted(() => {
  loadConfigs()
  loadApplications()
  loadEnvironments()
})
</script>

<style scoped>
.configs {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 